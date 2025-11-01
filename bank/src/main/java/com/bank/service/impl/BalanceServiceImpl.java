package com.bank.service.impl;

import com.bank.dto.BalanceDTO;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.model.Balance;
import com.bank.model.Card;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.BalanceRepository;
import com.bank.repository.CardRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.BalanceService;
import com.bank.service.EmailService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Transactional
public class BalanceServiceImpl implements BalanceService {
    private BalanceRepository balanceRepository;
    private AccountRepository accountRepository;
    private CardRepository cardRepository;
    private TransactionRepository transactionRepository;
    private EmailService emailService;

    public BalanceServiceImpl(BalanceRepository balanceRepository, AccountRepository accountRepository, CardRepository cardRepository, TransactionRepository transactionRepository, EmailService emailService) {
        this.balanceRepository = balanceRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @Override
    public Balance getBalance(Long accountId) {
        return balanceRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy số dư cho accountId = " + accountId));
    }

    @Override
    @Transactional
    public Balance deposit(Long accountId, BigDecimal amount, Long toCardId) {
        Balance balance = getBalance(accountId);
        balance.setAvailableBalance(balance.getAvailableBalance().add(amount));
        Balance saved = balanceRepository.save(balance);

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ nhận"));

        if (toCard.getStatus() == Card.Status.INACTIVE) {
            throw new RuntimeException("Thẻ đã bị vô hiệu hóa, không thể giao dịch");
        }

        Transaction tx = Transaction.builder()
                .amount(amount)
                .toCard(toCard)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // Gửi mail
        String email = toCard.getAccount().getEmail();
        if (email != null) {
            emailService.sendEmail(
                    email,
                    "Nạp tiền thành công",
                    "<p>Bạn đã nạp <b>" + amount + " VND</b> vào thẻ "
                            + toCard.getCardNumber() + ".</p>"
                            + "<p>Số dư hiện tại: " + balance.getAvailableBalance() + " VND</p>"
            );
        }

        return saved;
    }

    @Override
    @Transactional
    public Balance withdraw(Long accountId, BigDecimal amount, Long fromCardId) {
        Balance balance = balanceRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy số dư cho accountId = " + accountId));
        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư không đủ để rút tiền");
        }
        balance.setAvailableBalance(balance.getAvailableBalance().subtract(amount));
        Balance saved = balanceRepository.save(balance);

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ nguồn"));

        if (fromCard.getStatus() == Card.Status.INACTIVE) {
            throw new RuntimeException("Thẻ đã bị vô hiệu hóa, không thể rút tiền");
        }
        Transaction tx = Transaction.builder()
                .amount(amount)
                .fromCard(fromCard)
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // Gửi mail
        String email = fromCard.getAccount().getEmail();
        if (email != null) {
            emailService.sendEmail(
                    email,
                    "Rút tiền thành công",
                    "<p>Bạn đã rút <b>" + amount + " VND</b> từ thẻ "
                            + fromCard.getCardNumber() + ".</p>"
                            + "<p>Số dư hiện tại: " + balance.getAvailableBalance() + " VND</p>"
            );
        }

        return saved;
    }

    public BalanceDTO mapToDTO(Balance balance) {
        BalanceDTO dto = new BalanceDTO();
        dto.setBalanceId(balance.getBalanceId());
        dto.setAccountId(balance.getAccount().getAccountId());
        dto.setAvailableBalance(balance.getAvailableBalance());
        dto.setHoldBalance(balance.getHoldBalance());
        dto.setLastUpdated(balance.getLastUpdated());
        return dto;
    }

}
