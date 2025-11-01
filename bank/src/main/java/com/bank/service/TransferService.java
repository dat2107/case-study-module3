package com.bank.service;


import com.bank.dto.OtpConfirmDTO;
import com.bank.dto.TransactionDTO;
import com.bank.dto.TransferDTO;
import com.bank.dto.req.PaymentReq;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.model.*;
import com.bank.repository.*;
import com.bank.service.impl.AccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Random;

@Transactional
public class TransferService {
    private TransactionRepository transactionRepo;
    private OtpTransactionRepository otpRepo;
    private CardRepository cardRepo;
    private EmailService emailService;
    private BalanceRepository balanceRepository;
    private AccountRepository accountRepository;
    private AccountServiceImpl accountService;

    //Bước 1: Tạo giao dịch và sinh OTP
    public Transaction createTransferRequest(TransferDTO dto) {
        Card fromCard = cardRepo.findById(dto.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ nguồn"));

        Card toCard = cardRepo.findByCardNumber(dto.getToCardNumber())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ nhận"));

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền không hợp lệ");
        }

        if (fromCard.getCardId().equals(toCard.getCardId())) {
            throw new RuntimeException("Không thể chuyển khoản sang cùng một thẻ");
        }

        if (fromCard.getAccount().getAccountId().equals(toCard.getAccount().getAccountId())) {
            throw new RuntimeException("Không thể chuyển khoản giữa các thẻ trong cùng một tài khoản");
        }

        if (fromCard.getAccount().getBalance().getAvailableBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Số dư không đủ");
        }

        if (fromCard.getStatus() == Card.Status.INACTIVE) {
            throw new RuntimeException("Thẻ nguồn đã bị vô hiệu hóa, không thể chuyển khoản");
        }

        if (toCard.getStatus() == Card.Status.INACTIVE) {
            throw new RuntimeException("Thẻ nhận đã bị vô hiệu hóa, không thể chuyển khoản");
        }

        // Kiểm tra giới hạn giao dịch hằng ngày (Daily Transfer Limit)
        Account fromAccount = fromCard.getAccount();

        // Tính tổng số tiền đã chuyển thành công trong ngày hôm nay
        BigDecimal todayTotal = transactionRepo
                .findByFromCard_Account_AccountIdAndStatusAndCreatedAtBetween(
                        fromAccount.getAccountId(),
                        TransactionStatus.SUCCESS,
                        LocalDateTime.now().toLocalDate().atStartOfDay(),
                        LocalDateTime.now().toLocalDate().atTime(23, 59, 59)
                )
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dailyLimit = fromAccount.getUserLevel().getDailyTransferLimit();
        if (todayTotal.add(dto.getAmount()).compareTo(dailyLimit) > 0) {
            throw new RuntimeException("Vượt quá hạn mức chuyển khoản trong ngày (" + dailyLimit + ")");
        }

        // Tạo transaction
        Transaction tx = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(dto.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepo.save(tx);

        // Sinh OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        OtpTransaction otpTx = OtpTransaction.builder()
                .transaction(tx)
                .otpCode(otp)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        otpRepo.save(otpTx);

        // Gửi OTP qua email
        emailService.sendEmail(dto.getEmail(),
                "Mã OTP xác nhận giao dịch",
                "<p>Mã OTP của bạn là: <b>" + otp + "</b> (hiệu lực 5 phút).</p>");

        return tx;
    }

    //Xác nhận OTP -> chuyển trạng thái sang WAITING_APPROVAL
    @Transactional
    public TransactionDTO confirmOtp(OtpConfirmDTO dto) {
        OtpTransaction otpTx = otpRepo.findByTransaction_TransactionId(dto.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy OTP"));
        if (otpTx == null) throw new RuntimeException("Không tìm thấy OTP");

        if (otpTx.isVerified() || otpTx.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP hết hạn hoặc đã được dùng");
        }
        if (!otpTx.getOtpCode().equals(dto.getOtp())) {
            throw new RuntimeException("OTP không đúng");
        }

        System.out.println("OTP trong DB: " + otpTx.getOtpCode());
        System.out.println("expireAt: " + otpTx.getExpireAt());
        System.out.println("now: " + LocalDateTime.now());
        System.out.println("verified: " + otpTx.isVerified());


        otpTx.setVerified(true);
        otpRepo.save(otpTx);

        Transaction tx = otpTx.getTransaction();
        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Trạng thái giao dịch không hợp lệ");
        }
        Card fromCard = tx.getFromCard();
        BigDecimal amount = tx.getAmount();

        // Trừ availableBalance người gửi
        fromCard.getAccount().getBalance().setAvailableBalance(
                fromCard.getAccount().getBalance().getAvailableBalance().subtract(amount)
        );

        // Cộng vào holdBalance người gửi
        fromCard.getAccount().getBalance().setHoldBalance(
                fromCard.getAccount().getBalance().getHoldBalance().add(amount)
        );

        tx.setStatus(TransactionStatus.WAITING_APPROVAL);
        transactionRepo.save(tx);

        return toDto(tx);
    }

    //Admin duyệt giao dịch
    @Transactional
    public TransactionDTO approveTransaction(Long transactionId) {
        Transaction tx = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (tx.getStatus() != TransactionStatus.WAITING_APPROVAL) {
            throw new RuntimeException("Giao dịch không hợp lệ để duyệt");
        }

        Card fromCard = tx.getFromCard();
        Card toCard = tx.getToCard();
        BigDecimal amount = tx.getAmount();

        // Giảm holdBalance người gửi
        Balance fromBalance = fromCard.getAccount().getBalance();
        System.out.println("Trước khi duyệt - FROM balance: available="
                + fromBalance.getAvailableBalance()
                + ", hold=" + fromBalance.getHoldBalance());

        fromBalance.setHoldBalance(fromBalance.getHoldBalance().subtract(amount));

        System.out.println("Sau khi trừ holdBalance - FROM balance: available="
                + fromBalance.getAvailableBalance()
                + ", hold=" + fromBalance.getHoldBalance());

        // Cộng vào availableBalance người nhận
        Balance toBalance = toCard.getAccount().getBalance();
        System.out.println("Trước khi duyệt - TO balance: available="
                + toBalance.getAvailableBalance()
                + ", hold=" + toBalance.getHoldBalance());

        toBalance.setAvailableBalance(toBalance.getAvailableBalance().add(amount));

        System.out.println("Sau khi cộng availableBalance - TO balance: available="
                + toBalance.getAvailableBalance()
                + ", hold=" + toBalance.getHoldBalance());

        // Lưu account
        accountRepository.save(fromCard.getAccount());
        accountRepository.save(toCard.getAccount());

        System.out.println(">>> Đã save cả 2 account vào DB");

        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepo.save(tx);

        // Gửi message sang queue / service khác
        PaymentReq payment = new PaymentReq(
                tx.getTransactionId().toString(),
                fromCard.getAccount().getAccountId(),
                toCard.getAccount().getAccountId(),
                amount,
                "VND"
        );

        payment.setSenderEmail(fromCard.getAccount().getEmail());

        try {
            URL url = new URL("http://payment-service:8080/api/payments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Gửi JSON qua output stream
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payment);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Status: " + responseCode);

            // Đọc phản hồi (tùy chọn)
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response body: " + response);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi khi gọi Payment Service: " + e.getMessage());
        }

        return toDto(tx);
    }

    // Admin từ chối giao dịch
    @Transactional
    public TransactionDTO rejectTransaction(Long transactionId) {
        Transaction tx = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (tx.getStatus() != TransactionStatus.WAITING_APPROVAL) {
            throw new RuntimeException("Giao dịch không hợp lệ để từ chối");
        }

        Card fromCard = tx.getFromCard();
        BigDecimal amount = tx.getAmount();

        // Giảm holdBalance người gửi
        fromCard.getAccount().getBalance().setHoldBalance(
                fromCard.getAccount().getBalance().getHoldBalance().subtract(amount)
        );

        // Hoàn lại vào availableBalance người gửi
        fromCard.getAccount().getBalance().setAvailableBalance(
                fromCard.getAccount().getBalance().getAvailableBalance().add(amount)
        );

        tx.setStatus(TransactionStatus.FAILED);
        transactionRepo.save(tx);

        //Gửi email thông báo bị từ chối (best-effort, không phá giao dịch nếu gửi lỗi)
        try {
            String toEmail = fromCard.getAccount().getEmail();
            if (toEmail != null && !toEmail.isEmpty()) {
                String subject = "Giao dịch chuyển tiền đã bị từ chối";
                String html = """
                <p>Xin chào <b>%s</b>,</p>
                <p>Giao dịch chuyển tiền của bạn đã <b>bị từ chối</b> bởi quản trị viên.</p>
                <ul>
                  <li><b>Mã giao dịch:</b> %s</li>
                  <li><b>Số thẻ nguồn:</b> %s</li>
                  <li><b>Số thẻ nhận:</b> %s</li>
                  <li><b>Số tiền:</b> %s</li>
                  <li><b>Thời điểm:</b> %s</li>
                  <li><b>Trạng thái:</b> %s</li>
                </ul>
                <p>Số tiền đã được hoàn lại vào số dư khả dụng của bạn.</p>
                <p>Nếu bạn không thực hiện yêu cầu này, vui lòng liên hệ hỗ trợ ngay.</p>
                """.formatted(
                        fromCard.getAccount().getCustomerName() != null ? fromCard.getAccount().getCustomerName() : "Quý khách",
                        tx.getTransactionId(),
                        fromCard.getCardNumber(),
                        tx.getToCard() != null ? tx.getToCard().getCardNumber() : "(không xác định)",
                        amount.toPlainString(),
                        LocalDateTime.now(),
                        tx.getStatus().name()
                );

                emailService.sendEmail(toEmail, subject, html);
            }
        } catch (Exception ignore) {

        }

        return toDto(tx);
    }

    public TransactionDTO toDto(Transaction tx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(tx.getTransactionId());
        dto.setFromCardNumber(tx.getFromCard() != null ? tx.getFromCard().getCardNumber() : null);
        dto.setToCardNumber(tx.getToCard() != null ? tx.getToCard().getCardNumber() : null);
        dto.setAmount(tx.getAmount());
        dto.setStatus(tx.getStatus().name());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }
}
