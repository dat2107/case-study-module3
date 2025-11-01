package com.bank.controller;

import com.bank.config.JsonConfig;
import com.bank.dto.OtpConfirmDTO;
import com.bank.dto.TransactionDTO;
import com.bank.dto.TransferDTO;
import com.bank.model.Transaction;
import com.bank.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/transfer/*")
public class TransferController extends HttpServlet {

    private TransferService transferService;
    private final ObjectMapper mapper = JsonConfig.getMapper();

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        this.transferService = (TransferService) context.getAttribute("transferService");


        if (transferService == null) {
            System.err.println("AdminTransactionController: transferService chưa được khởi tạo");
            throw new ServletException("Services not found in ServletContext");
        }

        System.out.printf("AdminTransactionController initialized successfully");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // /request hoặc /confirm

        try {
            if (pathInfo == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");
                return;
            }

            // ---- /transfer/request ----
            if (pathInfo.equals("/request")) {
                TransferDTO dto = parseRequestBody(req, TransferDTO.class);
                Transaction tx = transferService.createTransferRequest(dto);

                Map<String, Object> response = new HashMap<>();
                response.put("transactionId", tx.getTransactionId());
                response.put("message", "Mã OTP đã được gửi đến email của bạn.");

                resp.getWriter().write(mapper.writeValueAsString(response));
                return;
            }

            // ---- /transfer/confirm ----
            if (pathInfo.equals("/confirm")) {
                OtpConfirmDTO dto = parseRequestBody(req, OtpConfirmDTO.class);
                TransactionDTO tx = transferService.confirmOtp(dto);
                resp.getWriter().write(mapper.writeValueAsString(tx));
                return;
            }

            // ---- default ----
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Đường dẫn không hợp lệ\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ===== Helper method đọc JSON request =====
    private <T> T parseRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return mapper.readValue(sb.toString(), clazz);
    }
}
