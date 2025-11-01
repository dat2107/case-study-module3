package com.bank.controller;

import com.bank.dto.UserLevelDTO;
import com.bank.model.UserLevel;
import com.bank.service.UserLevelService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/userlevel/*")
public class UserLevelController extends HttpServlet {

    private UserLevelService userLevelService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        this.userLevelService = (UserLevelService) getServletContext().getAttribute("userLevelService");
        if (this.userLevelService == null) {
            throw new ServletException("❌ UserLevelService chưa được khởi tạo trong AppInitializer");
        }
    }


    /** ✅ Kiểm tra quyền ADMIN từ attribute do JwtFilter gắn vào */
    private boolean isAdmin(HttpServletRequest req) {
        Object roleObj = req.getAttribute("role");
        if (roleObj == null) return false;

        String role = roleObj.toString().toUpperCase();
        return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
    }


    /** ✅ Lấy tất cả hoặc chi tiết 1 cấp độ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Chỉ ADMIN được phép truy cập\"}");
            return;
        }

        String path = req.getPathInfo(); // /{id} hoặc null

        try {
            if (path == null || "/".equals(path)) {
                List<UserLevel> list = userLevelService.getAll();
                resp.getWriter().write(mapper.writeValueAsString(list));
            } else {
                Long id = Long.parseLong(path.substring(1));
                UserLevel level = userLevelService.getById(id);
                resp.getWriter().write(mapper.writeValueAsString(level));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /** ✅ Thêm mới */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Chỉ ADMIN được phép truy cập\"}");
            return;
        }

        try {
            UserLevelDTO dto = parseBody(req, UserLevelDTO.class);
            UserLevel created = userLevelService.insert(dto);
            resp.getWriter().write(mapper.writeValueAsString(created));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /** ✅ Cập nhật */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Chỉ ADMIN được phép truy cập\"}");
            return;
        }

        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu ID trong đường dẫn\"}");
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));
            UserLevelDTO dto = parseBody(req, UserLevelDTO.class);
            UserLevel updated = userLevelService.update(id, dto);
            resp.getWriter().write(mapper.writeValueAsString(updated));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /** ✅ Xóa */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Chỉ ADMIN được phép truy cập\"}");
            return;
        }

        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu ID trong đường dẫn\"}");
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));
            userLevelService.delete(id);
            resp.getWriter().write("{\"message\":\"Xóa cấp độ thành công\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ---- Helper đọc JSON ----
    private <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return mapper.readValue(sb.toString(), clazz);
    }
}
