package com.bank.service;

import com.bank.dto.TransactionDTO;
import com.bank.enums.TransactionStatus;
import com.bank.model.Transaction;
import com.bank.repository.TransactionRepository;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
public class TransactionService {

    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> getAll(String status, int page, int size) {
        List<Transaction> transactions;
        long totalRecords;

        if (status != null && !status.isEmpty()) {
            TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
            transactions = transactionRepository.findByStatus(transactionStatus, page, size);
            totalRecords = transactionRepository.countByStatus(transactionStatus);
        } else {
            transactions = transactionRepository.findAllPaginated(page, size);
            totalRecords = transactionRepository.countAll();
        }

        // ✅ Tính tổng số trang
        int totalPages = (int) Math.ceil((double) totalRecords / size);
        if (totalPages == 0) totalPages = 1;

        // ✅ Chuyển sang DTO
        List<TransactionDTO> dtoList = transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // ✅ Gói kết quả theo dạng PageResult
        Map<String, Object> result = new HashMap<>();
        result.put("content", dtoList);
        result.put("totalPages", totalPages);
        result.put("page", page);

        return result;
    }


    public List<TransactionDTO> findByAccountId(Long accountId) {
        List<Transaction> list = transactionRepository
                .findByFromCard_Account_AccountIdOrToCard_Account_AccountId(accountId, accountId);

        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getByCard(Long cardId, int page, int size) {
        List<Transaction> transactions =
                transactionRepository.findByFromCard_CardIdOrToCard_CardId(cardId, cardId, page, size);

        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TransactionDTO updateStatus(Long id, String status) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setStatus(TransactionStatus.valueOf(status.toUpperCase()));
        transactionRepository.save(tx);

        return toDto(tx);
    }

    public TransactionDTO toDto(Transaction tx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(tx.getTransactionId());
        dto.setFromAccountId(tx.getFromCard() != null ? tx.getFromCard().getAccount().getAccountId() : null);
        dto.setToAccountId(tx.getToCard() != null ? tx.getToCard().getAccount().getAccountId() : null);
        dto.setFromCardNumber(tx.getFromCard() != null ? tx.getFromCard().getCardNumber() : null);
        dto.setToCardNumber(tx.getToCard() != null ? tx.getToCard().getCardNumber() : null);
        dto.setAmount(tx.getAmount());
        dto.setStatus(tx.getStatus().name());
        dto.setType(tx.getType().name());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }
}
