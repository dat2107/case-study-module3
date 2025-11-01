package com.bank.controller;

import com.bank.dto.TransactionDTO;
import com.bank.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// ✅ Servlet thuần thay cho JAX-RS controller
@WebServlet("/api/admin/transaction/*")
public class AdminTransactionController extends HttpServlet {

    private TransferService transferService;

    @Override
    public void init() throws ServletException {
        // ⚙️ Tự khởi tạo service hoặc lấy từ context
        this.transferService = (TransferService) getServletContext().getAttribute("transferService");
        if (this.transferService == null) {
            this.transferService = new TransferService();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        // Lấy path sau "/admin/transaction/"
        String path = req.getPathInfo(); // ví dụ "/approve" hoặc "/reject"
        String idParam = req.getParameter("id");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu tham số id\"}");
            return;
        }

        Long id = Long.parseLong(idParam);
        TransactionDTO tx;

        try {
            if (path != null && path.contains("approve")) {
                tx = transferService.approveTransaction(id);
            } else if (path != null && path.contains("reject")) {
                tx = transferService.rejectTransaction(id);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(mapper.writeValueAsString(tx));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
