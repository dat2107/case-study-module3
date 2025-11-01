package com.bank.controller;

import com.bank.dto.TransactionDTO;
import com.bank.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/transaction/*")
public class TransactionController extends HttpServlet {

    private TransactionService transactionService;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        this.transactionService = (TransactionService) context.getAttribute("transactionService");
        this.mapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    // ===== GET: danh sách và chi tiết =====
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo(); // null, /account/1, /card/2, /5, ...

        try {
            if (path == null || "/".equals(path)) {
                // /transaction?page=0&size=7&status=SUCCESS
                int page = parseInt(req.getParameter("page"), 0);
                int size = parseInt(req.getParameter("size"), 7);
                String status = req.getParameter("status");

                Object result = transactionService.getAll(status, page, size);
                resp.getWriter().write(mapper.writeValueAsString(result));

            } else if (path.startsWith("/account/")) {
                Long accountId = Long.parseLong(path.substring("/account/".length()));
                List<TransactionDTO> transactions = transactionService.findByAccountId(accountId);
                resp.getWriter().write(mapper.writeValueAsString(transactions));

            } else if (path.startsWith("/card/")) {
                Long cardId = Long.parseLong(path.substring("/card/".length()));
                int page = parseInt(req.getParameter("page"), 0);
                int size = parseInt(req.getParameter("size"), 7);

                Object result = transactionService.getByCard(cardId, page, size);
                resp.getWriter().write(mapper.writeValueAsString(result));

            }
//            else {
//                // /transaction/{id}
//                Long id = extractId(path);
//                TransactionDTO dto = transactionService.getTransactionById(id);
//                if (dto != null) {
//                    resp.getWriter().write(mapper.writeValueAsString(dto));
//                } else {
//                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                    resp.getWriter().write("{\"error\":\"Không tìm thấy giao dịch với id = " + id + "\"}");
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ===== PUT: cập nhật trạng thái =====
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo(); // /{id}/status

        if (path == null || !path.endsWith("/status")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ (phải là /{id}/status)\"}");
            return;
        }

        try {
            Long id = extractId(path);
            TransactionDTO body = parseRequestBody(req, TransactionDTO.class);
            TransactionDTO updated = transactionService.updateStatus(id, body.getStatus());
            resp.getWriter().write(mapper.writeValueAsString(updated));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

    private int parseInt(String value, int defaultVal) {
        try {
            return value == null ? defaultVal : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Long extractId(String path) {
        String[] parts = path.split("/");
        for (String p : parts) {
            if (p.matches("\\d+")) return Long.parseLong(p);
        }
        throw new IllegalArgumentException("Không tìm thấy ID trong path: " + path);
    }
}
