package com.bank.dto;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class AccountDTO {
    private String customerName;
    @Email(message = "Invalid email format")
    private String email;
    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9,10}$",
            message = "Invalid phone number format"
    )
    private String phoneNumber;
    private Long userId;
    private String username;
    private String password;
    private String role;
    private Long userLevelId;
}
