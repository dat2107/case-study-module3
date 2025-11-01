package com.bank.service.impl;

import com.bank.dto.AccountDTO;
import com.bank.dto.BalanceDTO;
import com.bank.dto.CardDTO;
import com.bank.dto.UserLevelDTO;
import com.bank.dto.res.AccountRes;
import com.bank.enums.TransactionStatus;
import com.bank.model.*;
import com.bank.repository.*;
import com.bank.service.AccountService;
import com.bank.service.EmailService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;
    private BalanceRepository balanceRepository;
    private CardRepository cardRepository;
    private UserRepository userRepository;
    private UserLevelRepository userLevelRepository;
    private TransactionRepository transactionRepository;
    private EmailService emailService;

    public AccountServiceImpl(AccountRepository accountRepository, BalanceRepository balanceRepository, UserRepository userRepository, CardRepository cardRepository, UserLevelRepository userLevelRepository, TransactionRepository transactionRepository, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.userLevelRepository = userLevelRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @Override
    public AccountRes create(AccountDTO accountDTO) {
        if (userRepository.findByUsername(accountDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        User user = new User();
        user.setUsername(accountDTO.getUsername());
        user.setPassword(encodePassword(accountDTO.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        if (accountRepository.findByEmail(accountDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Account account = new Account();
        account.setCustomerName(accountDTO.getCustomerName());
        account.setEmail(accountDTO.getEmail());
        account.setPhoneNumber(accountDTO.getPhoneNumber());

        UserLevel normalLevel = userLevelRepository.findByLevelName("Normal")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Level Normal"));
        account.setUserLevel(normalLevel);
        account.setUser(user);

        Balance balance = new Balance();
        balance.setAvailableBalance(BigDecimal.ZERO);
        balance.setHoldBalance(BigDecimal.ZERO);
        balance.setAccount(account);
        account.setBalance(balance);

        String token = UUID.randomUUID().toString();
        account.setVerificationToken(token);
        account.setTokenExpiry(java.time.LocalDateTime.now().plusHours(24));
        account.setEmailVerified(false);

        Account saved = accountRepository.save(account);

        String link = "http://localhost:8080/api/auth/verify?token=" + token;
        emailService.sendEmail(
                accountDTO.getEmail(),
                "Xác thực tài khoản",
                "<p>Nhấn vào link để xác thực tài khoản:</p>"
                        + "<a href='" + link + "'>Xác thực ngay</a>"
        );

        return mapToDTO(saved);
    }

    @Override
    public AccountRes update(Long id, AccountDTO accountDTO){
        Account existing = accountRepository.findByAccountId(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy account với id: " + id));

        if (accountDTO.getCustomerName() != null && !accountDTO.getCustomerName().isEmpty()){
            existing.setCustomerName(accountDTO.getCustomerName());
        }
        if (accountDTO.getPhoneNumber() != null && !accountDTO.getPhoneNumber().isEmpty()){
            existing.setPhoneNumber(accountDTO.getPhoneNumber());
        }
        if (accountDTO.getUserLevelId() != null) {
            UserLevel level = userLevelRepository.findById(accountDTO.getUserLevelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Level với id: " + accountDTO.getUserLevelId()));
            existing.setUserLevel(level);
        }

        Account saved = accountRepository.save(existing);
        return mapToDTO(saved);
    }

    @Override
    public void delete(Long accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        List<Card> cards = cardRepository.findByAccount_AccountId(accountId);
        Balance balance = balanceRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy số dư tài khoản!"));

        if (balance.getAvailableBalance().compareTo(BigDecimal.ZERO) > 0 ||
                balance.getHoldBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Không thể xóa tài khoản: số dư khác 0");
        }
        if (!cards.isEmpty()) {
            throw new RuntimeException("Không thể xóa tài khoản: còn liên kết thẻ");
        }

        User user = account.getUser();
        accountRepository.delete(account);
        if (user != null) userRepository.delete(user);
    }

    @Override
    public AccountRes getAccountById(Long accountId) {
        Account acc = accountRepository.findByIdWithCards(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToDTO(acc);
    }

    @Override
    public List<AccountRes> getAllAccount() {
        return accountRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AccountRes mapToDTO(Account acc) {
        AccountRes dto = new AccountRes();
        dto.setAccountId(acc.getAccountId());
        dto.setCustomerName(acc.getCustomerName());
        dto.setEmail(acc.getEmail());
        dto.setPhoneNumber(acc.getPhoneNumber());

        if (acc.getBalance() != null) {
            BalanceDTO balanceDTO = new BalanceDTO();
            balanceDTO.setAccountId(acc.getAccountId());
            balanceDTO.setAvailableBalance(acc.getBalance().getAvailableBalance());
            balanceDTO.setHoldBalance(acc.getBalance().getHoldBalance());
            dto.setBalance(balanceDTO);
        }

        if (acc.getCards() != null) {
            List<CardDTO> cardDTOs = acc.getCards().stream().map(card -> {
                CardDTO c = new CardDTO();
                //c.setAccountId(acc.getAccountId());
                c.setCardId(card.getCardId());
                c.setCardNumber(card.getCardNumber());
                c.setCardType(card.getCardType());
                c.setExpiryDate(card.getExpiryDate());
                c.setStatus(card.getStatus());
                BigDecimal holdBalance = transactionRepository
                        .findByFromCardAndStatus(card, TransactionStatus.WAITING_APPROVAL)
                        .stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                c.setHoldBalance(holdBalance);
                return c;
            }).collect(Collectors.toList());
            dto.setCards(cardDTOs);
        }

        if (acc.getUserLevel() != null) {
            UserLevelDTO lvl = new UserLevelDTO();
            lvl.setLevelName(acc.getUserLevel().getLevelName());
            lvl.setCardLimit(acc.getUserLevel().getCardLimit());
            lvl.setDailyTransferLimit(acc.getUserLevel().getDailyTransferLimit());
            dto.setUserLevel(lvl);
        }

        return dto;
    }

    private String encodePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không thể mã hóa mật khẩu", e);
        }
    }
}
