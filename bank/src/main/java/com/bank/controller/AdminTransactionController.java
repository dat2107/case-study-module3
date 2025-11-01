package com.bank.controller;

import com.bank.config.JsonConfig;
import com.bank.dto.TransactionDTO;
import com.bank.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AdminTransactionController
 * --------------------------
 * Xử lý URL dạng:
 *   POST /api/admin/transactions/{id}/approve
 *   POST /api/admin/transactions/{id}/reject
 */
@WebServlet(name = "AdminTransactionController", urlPatterns = "/api/admin/transactions/*")
public class AdminTransactionController extends HttpServlet {

    private transient TransferService transferService;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        this.transferService = (TransferService) context.getAttribute("transferService");

        if (transferService == null) {
            System.err.println("❌ [AdminTransactionController] transferService chưa được khởi tạo trong ServletContext");
            throw new ServletException("TransferService not found in ServletContext");
        }

        System.out.println("✅ [AdminTransactionController] Initialized successfully.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        ObjectMapper mapper = JsonConfig.getMapper();

        // Lấy phần path sau /api/admin/transactions/
        // Ví dụ: "5/approve" hoặc "8/reject"
        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu thông tin id hoặc hành động\"}");
            return;
        }

        String[] parts = path.split("/");
        // parts[0] = "" (vì bắt đầu bằng '/')
        // parts[1] = "5"
        // parts[2] = "approve"
        if (parts.length < 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ. Đúng định dạng: /api/admin/transactions/{id}/approve\"}");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID không hợp lệ\"}");
            return;
        }

        String action = parts[2];
        TransactionDTO tx;

        try {
            if ("approve".equalsIgnoreCase(action)) {
                tx = transferService.approveTransaction(id);
            } else if ("reject".equalsIgnoreCase(action)) {
                tx = transferService.rejectTransaction(id);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Hành động không hợp lệ (phải là approve hoặc reject)\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(mapper.writeValueAsString(tx));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}
