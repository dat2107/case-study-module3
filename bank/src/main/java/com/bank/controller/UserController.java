package com.bank.controller;

import com.bank.model.User;
import com.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {

    private UserService userService;
    private ObjectMapper mapper;


    @Override
    public void init() throws ServletException {
        this.userService = (UserService) getServletContext().getAttribute("userService");
        if (this.userService == null) {
            throw new ServletException("❌ UserService chưa được khởi tạo trong ServletContext. Kiểm tra AppInitializer.");
        }
        this.mapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
        System.out.println("✅ UserController initialized with UserService = " + userService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // null, /me, /list ...

        try {
            // === /api/users/me ===
            if ("/me".equalsIgnoreCase(pathInfo)) {
                String authHeader = req.getHeader("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\":\"Thiếu hoặc sai token\"}");
                    return;
                }

                // ✅ Xóa ngoặc kép, khoảng trắng thừa
                String token = authHeader.substring(7).replace("\"", "").trim();

                var userInfo = userService.getUserInfoFromToken(token);

                if (userInfo == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\":\"Token không hợp lệ hoặc đã hết hạn\"}");
                    return;
                }

                // ✅ Trả về JSON có role, username, message
                Map<String, Object> res = Map.of(
                        "username", userInfo.getUsername(),
                        "role", userInfo.getRole(),
                        "message", "Xin chào, " + userInfo.getUsername() + "!"
                );

                resp.getWriter().write(mapper.writeValueAsString(res));
                return;
            }

            // === /api/users?keyword=... ===
            String keyword = req.getParameter("keyword");
            List<User> users;

            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword);
            } else {
                users = userService.getAllUsers();
            }

            resp.getWriter().write(mapper.writeValueAsString(users));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}
