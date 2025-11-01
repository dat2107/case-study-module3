package com.bank.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentReq {
    private String paymentId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String currency;
    private String senderEmail;

    public PaymentReq() {}

    public PaymentReq(String paymentId, Long fromAccountId, Long toAccountId, BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
    }
}
