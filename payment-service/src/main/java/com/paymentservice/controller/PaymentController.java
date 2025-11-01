package com.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.service.PaymentService;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class PaymentController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        PaymentRequest request = mapper.readValue(req.getInputStream(), PaymentRequest.class);
        System.out.println("📨 [PaymentController] Received PaymentRequest: " + request.getPaymentId());

        new PaymentService().processPayment(request);

        resp.getWriter().write("{\"status\":\"sent_to_queue\"}");
    }
}
