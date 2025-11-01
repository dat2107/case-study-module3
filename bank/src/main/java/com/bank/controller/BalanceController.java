package com.bank.controller;

import com.bank.repository.BalanceRepository;
import com.bank.service.BalanceService;
import com.bank.service.impl.BalanceServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/balance/*")
public class BalanceController extends HttpServlet {

    private BalanceService balanceService;
    private BalanceRepository balanceRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        // ⚙️ Lấy service từ ServletContext (hoặc tự new tạm)
        ServletContext context = getServletContext();
        balanceService = (BalanceService) context.getAttribute("balanceService");
        balanceRepository = (BalanceRepository) context.getAttribute("balanceRepository");

        this.balanceService = (BalanceServiceImpl) getServletContext().getAttribute("balanceService");
        if (this.balanceService == null || balanceRepository == null) {
            System.err.println("❌ BalanceController: balanceService hoặc balanceRepository chưa được khởi tạo!");
            throw new ServletException("Services not found in ServletContext");
        }

        System.out.println("✅ BalanceController initialized successfully!");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();  // ví dụ: /123
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu accountId\"}");
            return;
        }

        try {
            Long accountId = Long.parseLong(pathInfo.substring(1));
            Object balance = balanceService.getBalance(accountId);
            resp.getWriter().write(mapper.writeValueAsString(balance));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();  // ví dụ: /123/deposit hoặc /123/withdraw
        if (pathInfo == null || pathInfo.split("/").length < 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ\"}");
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            Long accountId = Long.parseLong(parts[1]);
            String action = parts[2];  // "deposit" hoặc "withdraw"

            BigDecimal amount = new BigDecimal(req.getParameter("amount"));
            Long cardId = req.getParameter("toCardId") != null ?
                    Long.parseLong(req.getParameter("toCardId")) :
                    Long.parseLong(req.getParameter("fromCardId"));

            Object result;
            if ("deposit".equalsIgnoreCase(action)) {
                result = balanceService.deposit(accountId, amount, cardId);
            } else if ("withdraw".equalsIgnoreCase(action)) {
                result = balanceService.withdraw(accountId, amount, cardId);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Hành động không hợp lệ\"}");
                return;
            }

            resp.getWriter().write(mapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
