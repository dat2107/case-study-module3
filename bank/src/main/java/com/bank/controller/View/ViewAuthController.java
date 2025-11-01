package com.bank.controller.View;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "viewAuthController", urlPatterns = {
        "/login", "/register", "/home", "/dashboard", "/forgot", "/account",
        "/createCard", "/user", "/user-level", "/vip-detail",
        "/updateUser", "/userDetail", "/cardManager", "/cardDetail",
        "/transfer", "/transaction", "/transaction-history"
})
public class ViewAuthController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        String viewPath;

        switch (path) {
            case "/login": viewPath = "/views/login.jsp"; break;
            case "/register": viewPath = "/views/register.jsp"; break;
            case "/home": viewPath = "/views/home.jsp"; break;
            case "/dashboard": viewPath = "/views/dashboard.jsp"; break;
            case "/forgot": viewPath = "/views/forgot.jsp"; break;
            case "/account": viewPath = "/views/account.jsp"; break;
            case "/createCard": viewPath = "/views/createCard.jsp"; break;
            case "/user": viewPath = "/views/user.jsp"; break;
            case "/user-level": viewPath = "/views/user-level.jsp"; break;
            case "/vip-detail": viewPath = "/views/vip-detail.jsp"; break;
            case "/updateUser": viewPath = "/views/updateUser.jsp"; break;
            case "/userDetail": viewPath = "/views/userDetail.jsp"; break;
            case "/cardManager": viewPath = "/views/cardManager.jsp"; break;
            case "/cardDetail": viewPath = "/views/cardDetail.jsp"; break;
            case "/transfer": viewPath = "/views/transfer.jsp"; break;
            case "/transaction": viewPath = "/views/transaction.jsp"; break;
            case "/transaction-history": viewPath = "/views/transaction-history.jsp"; break;
            default: viewPath = "/views/404.jsp"; break;
        }

        req.getRequestDispatcher(viewPath).forward(req, resp);
    }
}
