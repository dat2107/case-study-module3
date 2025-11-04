-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: final_module3
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` bigint NOT NULL AUTO_INCREMENT,
  `CUSTOMERNAME` varchar(255) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `EMAILVERIFIED` tinyint(1) NOT NULL DEFAULT '0',
  `PHONENUMBER` varchar(255) DEFAULT NULL,
  `token_expiry` datetime(6) DEFAULT NULL,
  `verification_token` varchar(255) DEFAULT NULL,
  `level_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`account_id`),
  KEY `FK_account_level_id` (`level_id`),
  KEY `FK_account_user_id` (`user_id`),
  CONSTRAINT `FK_account_level_id` FOREIGN KEY (`level_id`) REFERENCES `user_level` (`ID`),
  CONSTRAINT `FK_account_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'Nguyễn Thành Đạt','nguyenthanhdat210l72004@gmail.com',1,'0365467973',NULL,NULL,2,13),(2,'admin','datnguyen2174@gmail.com',1,'0365467977',NULL,NULL,1,14),(4,'Nguyễn Thành Đạt','ntd210704@gmail.com',1,'0987654321',NULL,NULL,1,16);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balance`
--

DROP TABLE IF EXISTS `balance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `balance` (
  `BALANCEID` bigint NOT NULL AUTO_INCREMENT,
  `AVAILABLEBALANCE` decimal(18,2) DEFAULT NULL,
  `HOLDBALANCE` decimal(18,2) DEFAULT NULL,
  `LASTUPDATED` datetime(6) DEFAULT NULL,
  `account_id` bigint NOT NULL,
  PRIMARY KEY (`BALANCEID`),
  KEY `FK_balance_account_id` (`account_id`),
  CONSTRAINT `FK_balance_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balance`
--

LOCK TABLES `balance` WRITE;
/*!40000 ALTER TABLE `balance` DISABLE KEYS */;
INSERT INTO `balance` VALUES (1,1355700.00,0.00,NULL,1),(2,0.00,0.00,NULL,2),(4,10000000.00,-355700.00,NULL,4);
/*!40000 ALTER TABLE `balance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `card`
--

DROP TABLE IF EXISTS `card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card` (
  `card_id` bigint NOT NULL AUTO_INCREMENT,
  `card_number` varchar(255) DEFAULT NULL,
  `card_type` enum('DEBIT','CREDIT') DEFAULT NULL,
  `EXPIRYDATE` date DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  `account_id` bigint NOT NULL,
  PRIMARY KEY (`card_id`),
  KEY `FK_card_account_id` (`account_id`),
  CONSTRAINT `FK_card_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `card`
--

LOCK TABLES `card` WRITE;
/*!40000 ALTER TABLE `card` DISABLE KEYS */;
INSERT INTO `card` VALUES (2,'4111111086338665','DEBIT','2026-01-29','ACTIVE',1),(3,'4111114374935036','DEBIT','2026-01-29','ACTIVE',1),(9,'4111111984257462','DEBIT','2025-11-30','ACTIVE',4);
/*!40000 ALTER TABLE `card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_transaction`
--

DROP TABLE IF EXISTS `otp_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_transaction` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `EXPIREAT` datetime(6) NOT NULL,
  `OTPCODE` varchar(6) NOT NULL,
  `VERIFIED` tinyint(1) DEFAULT '0',
  `transaction_id` bigint NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_otp_transaction_transaction_id` (`transaction_id`),
  CONSTRAINT `FK_otp_transaction_transaction_id` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`TRANSACTIONID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_transaction`
--

LOCK TABLES `otp_transaction` WRITE;
/*!40000 ALTER TABLE `otp_transaction` DISABLE KEYS */;
INSERT INTO `otp_transaction` VALUES (1,'2025-11-01 07:43:51.858387','394689',1,4),(2,'2025-11-01 07:47:49.004402','640234',1,5),(3,'2025-11-01 08:14:10.933380','495210',1,6),(4,'2025-11-01 08:18:41.908152','109342',1,7),(5,'2025-11-01 08:29:15.123496','466677',1,8),(6,'2025-11-01 08:34:09.024918','503528',1,9),(7,'2025-11-01 08:38:13.175578','667559',1,10),(8,'2025-11-01 08:40:45.429525','684836',1,11),(9,'2025-11-01 08:44:13.172040','303944',1,12);
/*!40000 ALTER TABLE `otp_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction` (
  `TRANSACTIONID` bigint NOT NULL AUTO_INCREMENT,
  `AMOUNT` decimal(38,0) NOT NULL,
  `CREATEDAT` datetime(6) DEFAULT NULL,
  `STATUS` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  `from_card_id` bigint DEFAULT NULL,
  `to_card_id` bigint DEFAULT NULL,
  PRIMARY KEY (`TRANSACTIONID`),
  KEY `fk_transaction_to_card` (`to_card_id`),
  KEY `fk_transaction_from_card` (`from_card_id`),
  CONSTRAINT `fk_transaction_from_card` FOREIGN KEY (`from_card_id`) REFERENCES `card` (`card_id`),
  CONSTRAINT `fk_transaction_to_card` FOREIGN KEY (`to_card_id`) REFERENCES `card` (`card_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;
INSERT INTO `transaction` VALUES (1,10000000,'2025-10-28 14:24:04.486411','SUCCESS','WITHDRAW',2,NULL),(2,1000000,'2025-10-28 14:38:52.695486','SUCCESS','DEPOSIT',NULL,2),(3,10000000,'2025-11-01 07:00:12.843701','SUCCESS','DEPOSIT',NULL,9),(4,10000,'2025-11-01 07:38:51.746155','SUCCESS','TRANSFER',9,3),(5,5000,'2025-11-01 07:42:48.905134','SUCCESS','TRANSFER',9,2),(6,20000,'2025-11-01 08:09:10.926785','SUCCESS','TRANSFER',9,2),(7,200000,'2025-11-01 08:13:41.806741','SUCCESS','TRANSFER',9,2),(8,30000,'2025-11-01 08:24:15.025264','SUCCESS','TRANSFER',9,2),(9,50000,'2025-11-01 08:29:08.923296','SUCCESS','TRANSFER',9,2),(10,40000,'2025-11-01 08:33:13.074795','SUCCESS','TRANSFER',9,2),(11,600,'2025-11-01 08:35:45.425310','SUCCESS','TRANSFER',9,2),(12,100,'2025-11-01 08:39:13.091378','SUCCESS','TRANSFER',9,2);
/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_level`
--

DROP TABLE IF EXISTS `user_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_level` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `card_limit` int NOT NULL,
  `daily_transfer_limit` decimal(38,0) NOT NULL,
  `level_name` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `level_name` (`level_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_level`
--

LOCK TABLES `user_level` WRITE;
/*!40000 ALTER TABLE `user_level` DISABLE KEYS */;
INSERT INTO `user_level` VALUES (1,1000,1000000,'Normal'),(2,3,5000000,'Vip1'),(3,5,20000000,'Vip2'),(4,10,100000000,'Vip3'),(5,100,10000000000,'Vip 4');
/*!40000 ALTER TABLE `user_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `PASSWORD` varchar(255) NOT NULL,
  `ROLE` varchar(255) NOT NULL,
  `USERNAME` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `USERNAME` (`USERNAME`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (13,'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f','USER','datnt'),(14,'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f','ADMIN','admin'),(16,'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f','USER','dat');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-04 18:08:39
