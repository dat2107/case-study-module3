package com.bank.controller;

import com.bank.config.JsonConfig;
import com.bank.dto.AccountDTO;
import com.bank.dto.res.AccountRes;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.service.impl.AccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "accountController", urlPatterns = {"/api/account/*"})
public class AccountController extends HttpServlet {

    private AccountServiceImpl accountService;
    private AccountRepository accountRepository;
    private final ObjectMapper mapper = JsonConfig.getMapper();

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        accountService = (AccountServiceImpl) context.getAttribute("accountService");
        accountRepository = (AccountRepository) context.getAttribute("accountRepository");

        if (accountService == null || accountRepository == null) {
            System.err.println("❌ AccountController: accountService hoặc accountRepository chưa được khởi tạo!");
            throw new ServletException("Services not found in ServletContext");
        }

        System.out.println("✅ AccountController initialized successfully!");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        AccountDTO dto = mapper.readValue(req.getInputStream(), AccountDTO.class);
        AccountRes account = accountService.create(dto);

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(account));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        // /api/account → lấy tất cả
        if (pathInfo == null || "/".equals(pathInfo)) {
            List<AccountRes> list = accountService.getAllAccount();
            resp.getWriter().write(mapper.writeValueAsString(list));
            return;
        }

        // /api/account/{id} hoặc /api/account/{id}/email
        String[] parts = pathInfo.split("/");
        if (parts.length >= 2) {
            try {
                Long id = Long.parseLong(parts[1]);
                if (parts.length == 3 && "email".equals(parts[2])) {
                    Account account = accountRepository.findByAccountId(id)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy account"));
                    resp.getWriter().write(mapper.writeValueAsString(account.getEmail()));
                } else {
                    AccountRes account = accountService.getAccountById(id);
                    resp.getWriter().write(mapper.writeValueAsString(account));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID không hợp lệ\"}");
            } catch (RuntimeException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Cần truyền ID\"}");
            return;
        }

        Long id = Long.parseLong(pathInfo.substring(1));
        AccountDTO dto = mapper.readValue(req.getInputStream(), AccountDTO.class);
        AccountRes updated = accountService.update(id, dto);

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(updated));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Cần truyền ID\"}");
            return;
        }

        Long id = Long.parseLong(pathInfo.substring(1));
        accountService.delete(id);

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"message\":\"Xóa thành công " + id + "\"}");
    }
}
