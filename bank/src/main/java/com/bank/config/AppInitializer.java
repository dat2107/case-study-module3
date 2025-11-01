package com.bank.config;

import com.bank.repository.*;
import com.bank.security.JwtUtil;
import com.bank.service.*;
import com.bank.service.impl.AccountServiceImpl;
import com.bank.service.impl.BalanceServiceImpl;
import com.bank.service.impl.CardServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("🚀 Initializing Application Context...");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("bankPU");
        EntityManager em = emf.createEntityManager();

        // ✅ Khởi tạo repository
        UserRepository userRepository = new UserRepository(emf);
        AccountRepository accountRepository = new AccountRepository(emf);
        TransactionRepository transactionRepository = new TransactionRepository(emf);
        BalanceRepository balanceRepository = new BalanceRepository(emf);
        UserLevelRepository userLevelRepository = new UserLevelRepository(emf);
        CardRepository cardRepository  = new CardRepository(emf);
        OtpTransactionRepository otpReposi = new OtpTransactionRepository(emf);

        // ✅ Khởi tạo JwtUtil
        JwtUtil jwtUtil = new JwtUtil();

        // ✅ Tạo service và gắn repository thủ công
        UserService userService = new UserService(userRepository,accountRepository, jwtUtil);
        EmailService emailService = new EmailService();
        AccountServiceImpl accountService = new AccountServiceImpl(
                accountRepository, balanceRepository, userRepository, cardRepository,
                userLevelRepository, transactionRepository, emailService
        );
        CardServiceImpl cardService = new CardServiceImpl(
                cardRepository,
                accountRepository,
                balanceRepository,
                transactionRepository,
                userLevelRepository,
                jwtUtil,
                accountService
        );
        BalanceServiceImpl balanceService = new BalanceServiceImpl(
                balanceRepository,
                accountRepository,
                cardRepository,
                transactionRepository,
                emailService
        );

        TransactionService transactionService = new TransactionService(transactionRepository);
        UserLevelService userLevelService = new UserLevelService(userLevelRepository);
        TransferService transferService = new TransferService(
                transactionRepository,
                otpReposi,
                cardRepository,
                emailService,
                balanceRepository,
                accountRepository,
                accountService
        );

        // ✅ Đặt vào context
        ServletContext context = sce.getServletContext();
        context.setAttribute("userService", userService);
        context.setAttribute("accountService", accountService);
        context.setAttribute("accountRepository", accountRepository);
        context.setAttribute("cardService", cardService);
        context.setAttribute("transactionService", transactionService);
        context.setAttribute("userLevelService", userLevelService);
        context.setAttribute("balanceService", balanceService);
        context.setAttribute("balanceRepository", balanceRepository);
        context.setAttribute("transferService", transferService);

        // ✅ Thêm cấu hình ObjectMapper hỗ trợ LocalDate / LocalDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        context.setAttribute("objectMapper", mapper);

        System.out.println("✅ Services, Repositories & ObjectMapper initialized successfully!");
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("🧹 EntityManagerFactory closed");
        }
    }
}
