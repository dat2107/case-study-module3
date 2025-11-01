package com.bank.dto.res;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CardRes {
    private Long cardId;
    private String cardNumber;
    private String cardType;
    private String status;
    private LocalDate expiryDate;

    private AccountRes account;
}
