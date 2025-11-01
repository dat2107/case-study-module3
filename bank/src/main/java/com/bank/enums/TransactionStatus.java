package com.bank.enums;

public enum TransactionStatus {
    PENDING,        // mới tạo, chờ nhập OTP
    WAITING_APPROVAL,
    SUCCESS,        // admin duyệt thành công
    FAILED          // OTP sai/hết hạn hoặc admin từ chối
}
