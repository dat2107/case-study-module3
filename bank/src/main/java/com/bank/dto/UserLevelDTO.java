package com.bank.dto;

import lombok.Data;


import java.math.BigDecimal;

@Data
public class UserLevelDTO {
    private String levelName;
    private Integer cardLimit;
    private BigDecimal dailyTransferLimit;
}
