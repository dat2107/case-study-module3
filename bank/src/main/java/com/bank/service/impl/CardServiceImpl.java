package com.bank.service.impl;

import com.bank.dto.CardDTO;
import com.bank.dto.res.AccountRes;
import com.bank.dto.res.CardRes;
import com.bank.enums.TransactionStatus;
import com.bank.model.*;
import com.bank.repository.*;
import com.bank.security.JwtUtil;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Transactional
public class CardServiceImpl {
    private CardRepository cardRepository;
    private AccountRepository accountRepository;
    private BalanceRepository balanceRepository;
    private TransactionRepository transactionRepository;
    private UserLevelRepository userLevelRepository;
    private JwtUtil jwtUtil;
    private AccountServiceImpl accountServiceImpl;

    public CardServiceImpl(CardRepository cardRepository,
                           AccountRepository accountRepository,
                           BalanceRepository balanceRepository,
                           TransactionRepository transactionRepository,
                           UserLevelRepository userLevelRepository,
                           JwtUtil jwtUtil,
                           AccountServiceImpl accountServiceImpl) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
        this.userLevelRepository = userLevelRepository;
        this.jwtUtil = jwtUtil;
        this.accountServiceImpl = accountServiceImpl;
    }


    public CardRes create(CardDTO cardDTO, String token) {
        Long accountId ;
        //Nếu cardDTO có accountId -> admin đang tạo thẻ cho user
        if (cardDTO.getAccountId() != null) {
            accountId = cardDTO.getAccountId();
        } else {
            //User tự tạo thẻ -> lấy accountId từ token
            accountId = jwtUtil.extractAccountId(token);
        }
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        int currentCards = cardRepository.countByAccount_AccountId(accountId);

        UserLevel userLevel = account.getUserLevel();

        if (userLevel.getCardLimit() != -1 && currentCards >= userLevel.getCardLimit()) {
            throw new RuntimeException("Đã đạt giới hạn số thẻ cho cấp độ này");
        }

        Card card = new Card();
        card.setAccount(account);
        card.setCardType(cardDTO.getCardType());
        card.setExpiryDate(cardDTO.getExpiryDate());
        card.setStatus(cardDTO.getStatus());
        card.setStatus(Card.Status.ACTIVE);
        String cardNumber = generateCardNumber();
        card.setCardNumber(cardNumber);

        cardRepository.save(card);
        return mapToDTO(card);
    }

    private String generateCardNumber() {
        String bin = "411111"; // 6 số đầu: BIN giả định của ngân hàng
        String accountPart = String.format("%09d", new java.util.Random().nextInt(1_000_000_000));
        String partial = bin + accountPart; // 15 số
        return partial + calculateLuhnCheckDigit(partial); // thêm số kiểm tra cuối
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true; // bắt đầu từ số cuối
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1; // cộng lại hai chữ số
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    public List<CardRes> getByAccountId(Long accountId){
        return cardRepository.findByAccount_AccountId(accountId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<CardRes> getAllCard(){
        return cardRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    public CardRes getById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ"));
        return mapToDTO(card); //khớp kiểu dữ liệu
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ"));

        Balance balance = balanceRepository.findByAccount_AccountId(card.getAccount().getAccountId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy số dư cho account"));

        BigDecimal holdBalanceForCard = calculateCardHoldBalance(cardId);

        if (holdBalanceForCard.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Không thể xóa thẻ vì có số dư đang chờ xử lý (" + holdBalanceForCard + ")");
        }

        cardRepository.delete(card);
    }

    // 🔹 1. Tìm thẻ theo số thẻ
    public CardRes getByCardNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với số: " + cardNumber));
        return mapToDTO(card);
    }

    public CardRes updateStatus(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với id = " + cardId));

        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            if (card.getStatus() != Card.Status.INACTIVE) {
                card.setStatus(Card.Status.INACTIVE);
                cardRepository.save(card);
            }
            throw new RuntimeException("❌ Thẻ đã hết hạn, không thể kích hoạt lại.");
        }

        if (card.getStatus() == Card.Status.ACTIVE) {
            card.setStatus(Card.Status.INACTIVE);
        } else {
            card.setStatus(Card.Status.ACTIVE);
        }
        cardRepository.save(card);
        return mapToDTO(card);
    }

    private CardRes mapToDTO(Card card) {
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            if (card.getStatus() == Card.Status.ACTIVE) {
                card.setStatus(Card.Status.INACTIVE);
                cardRepository.save(card);
            }
        }

        CardRes dto = new CardRes();
        dto.setCardId(card.getCardId());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardType(card.getCardType().name());
        dto.setStatus(card.getStatus().name());
        dto.setExpiryDate(card.getExpiryDate());


        AccountRes accDto = accountServiceImpl.mapToDTO(card.getAccount());
        dto.setAccount(accDto);

        return dto;
    }

    private BigDecimal calculateCardHoldBalance(Long cardId) {
        // Lấy tất cả giao dịch liên quan đến thẻ nguồn đang ở trạng thái PENDING hoặc WAITING_APPROVAL
        List<Transaction> pendingTx = transactionRepository
                .findByFromCard_CardIdAndStatusIn(
                        cardId,
                        List.of(TransactionStatus.PENDING, TransactionStatus.WAITING_APPROVAL)
                );

        return pendingTx.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
