package com.bank.dto;

import lombok.Data;

@Data
public class OtpConfirmDTO {
    private Long transactionId;
    private String otp;
}
