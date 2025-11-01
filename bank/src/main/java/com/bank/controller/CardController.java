package com.bank.controller;

import com.bank.config.JsonConfig;
import com.bank.dto.CardDTO;
import com.bank.dto.res.CardRes;
import com.bank.repository.*;
import com.bank.security.JwtUtil;
import com.bank.service.CardService;
import com.bank.service.impl.AccountServiceImpl;
import com.bank.service.impl.CardServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/card/*")
public class CardController extends HttpServlet {

    private CardServiceImpl cardService;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        this.cardService = (CardServiceImpl) context.getAttribute("cardService");
        this.mapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    // ========== Tạo thẻ ==========
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // ví dụ: null hoặc "/123/status"

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // ---- Tạo mới thẻ ----
                String authHeader = req.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\":\"Thiếu token hoặc chưa đăng nhập\"}");
                    return;
                }
                String token = authHeader.substring(7);

                CardDTO dto = parseRequestBody(req, CardDTO.class);
                CardRes card = cardService.create(dto, token);
                resp.getWriter().write(mapper.writeValueAsString(card));

            } else if (pathInfo.endsWith("/status")) {
                // ---- Cập nhật trạng thái ----
                Long cardId = extractId(pathInfo);
                CardRes card = cardService.updateStatus(cardId);
                resp.getWriter().write(mapper.writeValueAsString(card));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ========== Lấy danh sách thẻ / chi tiết ==========
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // null, /123, /account/5, /number/411111...

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<CardRes> cards = cardService.getAllCard();
                resp.getWriter().write(mapper.writeValueAsString(cards));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2) { // /{cardId}
                Long cardId = Long.parseLong(parts[1]);
                CardRes card = cardService.getById(cardId);
                resp.getWriter().write(mapper.writeValueAsString(card));

            } else if (parts.length == 3 && "account".equalsIgnoreCase(parts[1])) {
                Long accountId = Long.parseLong(parts[2]);
                List<CardRes> cards = cardService.getByAccountId(accountId);
                if (cards.isEmpty()) {
                    resp.getWriter().write("{\"message\":\"Không có thẻ nào cho accountId=" + accountId + "\"}");
                } else {
                    resp.getWriter().write(mapper.writeValueAsString(cards));
                }

            } else if (parts.length == 3 && "number".equalsIgnoreCase(parts[1])) {
                String cardNumber = parts[2];
                CardRes card = cardService.getByCardNumber(cardNumber);
                resp.getWriter().write(mapper.writeValueAsString(card));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ========== Xóa thẻ ==========
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // /{cardId}

        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu cardId\"}");
            return;
        }

        try {
            Long cardId = extractId(pathInfo);
            cardService.deleteCard(cardId);
            resp.getWriter().write(mapper.writeValueAsString(Map.of("message", "Xóa thẻ thành công")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ---------- Helpers ----------
    private <T> T parseRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return mapper.readValue(sb.toString(), clazz);
    }

    private Long extractId(String pathInfo) {
        String[] parts = pathInfo.split("/");
        for (String p : parts) {
            if (p.matches("\\d+")) return Long.parseLong(p);
        }
        throw new IllegalArgumentException("Không tìm thấy ID trong đường dẫn: " + pathInfo);
    }
}
