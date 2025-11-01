package com.bank.dto;

import com.bank.model.Card;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDTO {
    private Long accountId;
    private Long cardId;
    private String cardNumber;
    private Card.Type cardType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private Card.Status status;
    private BigDecimal holdBalance = BigDecimal.ZERO;
}
