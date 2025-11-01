package com.bank.dto.res;

import com.bank.dto.BalanceDTO;
import com.bank.dto.CardDTO;
import com.bank.dto.UserLevelDTO;
import lombok.Data;

import java.util.List;

@Data
public class AccountRes {
    private Long accountId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private BalanceDTO balance;
    private List<CardDTO> cards;
    private UserLevelDTO userLevel;
}
