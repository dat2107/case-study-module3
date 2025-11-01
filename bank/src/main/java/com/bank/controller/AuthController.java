package com.bank.controller;

import com.bank.dto.AccountDTO;
import com.bank.dto.UserDTO;
import com.bank.dto.res.AccountRes;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet {

    private UserService userService;
    private AccountService accountService;
    private AccountRepository accountRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        userService = (UserService) getServletContext().getAttribute("userService");
        accountService = (AccountService) getServletContext().getAttribute("accountService");
        accountRepository = (AccountRepository) getServletContext().getAttribute("accountRepository");

        System.out.println("🔍 [AuthController] userService = " + userService);
        System.out.println("🔍 [AuthController] accountService = " + accountService);
        System.out.println("🔍 [AuthController] accountRepository = " + accountRepository);

        if (userService == null || accountService == null || accountRepository == null) {
            throw new ServletException("❌ Dịch vụ chưa được khởi tạo trong AppInitializer! Kiểm tra web.xml hoặc context.");
        }

        System.out.println("✅ AuthController initialized successfully with all dependencies.");
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo();

        // Đọc body JSON
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        String body = sb.toString();

        try {
            if (path.equals("/register")) {
                AccountDTO dto = mapper.readValue(body, AccountDTO.class);
                AccountRes account = accountService.create(dto);

                Map<String, Object> res = Map.of(
                        "message", "Đăng ký thành công!",
                        "customerName", account.getCustomerName()
                );
                resp.getWriter().write(mapper.writeValueAsString(res));

            } else if (path.equals("/login")) {
                UserDTO dto = mapper.readValue(body, UserDTO.class);
                String token = userService.login(dto.getUsername(), dto.getPassword());
                resp.getWriter().write(mapper.writeValueAsString(Map.of("token", token)));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/me".equals(path)) {
                String header = req.getHeader("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\":\"Thiếu token hoặc chưa đăng nhập\"}");
                    return;
                }

                // Loại bỏ ngoặc kép nếu token bị bao bởi frontend
                String token = header.substring(7).replace("\"", "").trim();

                var userInfo = userService.getUserInfoFromToken(token);
                Map<String, Object> result = new HashMap<>();
                result.put("username", userInfo.getUsername());
                result.put("role", userInfo.getRole());

                resp.getWriter().write(mapper.writeValueAsString(result));

            } else if (path.equals("/verify")) {
                String token = req.getParameter("token");
                Account acc = accountRepository.findByVerificationToken(token)
                        .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

                if (acc.getTokenExpiry() != null && acc.getTokenExpiry().isBefore(LocalDateTime.now())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Token đã hết hạn\"}");
                    return;
                }

                acc.setEmailVerified(true);
                acc.setVerificationToken(null);
                acc.setTokenExpiry(null);
                accountRepository.save(acc);

                resp.getWriter().write("{\"message\":\"Xác thực email thành công!\"}");

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}
