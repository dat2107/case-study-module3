package com.bank.service;


import com.bank.dto.AccountDTO;
import com.bank.dto.res.AccountRes;

import java.util.List;

public interface AccountService {
    AccountRes create(AccountDTO accountDTO);
    AccountRes update(Long id, AccountDTO accountDTO);
    void delete(Long accountId);
    AccountRes getAccountById(Long accountId);
    List<AccountRes> getAllAccount();
}
