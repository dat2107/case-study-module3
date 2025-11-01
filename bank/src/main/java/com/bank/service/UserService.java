package com.bank.service;

import com.bank.dto.UserDTO;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.security.JwtUtil;
import org.apache.commons.codec.digest.DigestUtils;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class UserService {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, AccountRepository accountRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
    }

    private String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }

    public String login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            // Xác thực username/password
            String hashedInput = hashPassword(password);
            if (!hashedInput.equals(user.getPassword())) {
                throw new RuntimeException("Sai mật khẩu");
            }
            // Lấy account gắn với user (1 user = 1 account)
            Account account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy account cho user"));
            // Sinh token chứa accountId + role
            return jwtUtil.generateToken(user.getUsername(),user.getRole(), account);
        } catch (Exception e) {
            System.out.println(">>> Sai mật khẩu hoặc tài khoản không tồn tại");
            throw new RuntimeException("Sai thông tin đăng nhập");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingIgnoreCase(keyword);
    }

    public UserDTO getUserInfoFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setRole(role);
        return dto;
    }

}
