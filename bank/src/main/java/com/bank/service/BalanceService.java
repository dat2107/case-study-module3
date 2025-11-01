package com.bank.service;

import com.bank.model.Balance;

import java.math.BigDecimal;

public interface BalanceService {
    Balance getBalance(Long accountId);
    Balance deposit(Long accountId, BigDecimal amount, Long toCardId);
    Balance withdraw(Long accountId, BigDecimal amount, Long fromCardId);
}
