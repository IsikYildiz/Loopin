-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: loopin_db
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `chatmessages`
--

DROP TABLE IF EXISTS `chatmessages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatmessages` (
  `chatId` int NOT NULL,
  `messageId` int NOT NULL,
  PRIMARY KEY (`chatId`,`messageId`),
  KEY `fk_chatmessages_message` (`messageId`),
  CONSTRAINT `fk_chatmessages_chat` FOREIGN KEY (`chatId`) REFERENCES `chats` (`chatId`) ON DELETE CASCADE,
  CONSTRAINT `fk_chatmessages_message` FOREIGN KEY (`messageId`) REFERENCES `messages` (`messageId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chatmessages`
--

LOCK TABLES `chatmessages` WRITE;
/*!40000 ALTER TABLE `chatmessages` DISABLE KEYS */;
INSERT INTO `chatmessages` VALUES (2,8);
/*!40000 ALTER TABLE `chatmessages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chats`
--

DROP TABLE IF EXISTS `chats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chats` (
  `chatId` int NOT NULL AUTO_INCREMENT,
  `user1Id` int NOT NULL,
  `user2Id` int NOT NULL,
  PRIMARY KEY (`chatId`),
  KEY `fk_chats_user1` (`user1Id`),
  KEY `fk_chats_user2` (`user2Id`),
  CONSTRAINT `fk_chats_user1` FOREIGN KEY (`user1Id`) REFERENCES `users` (`userId`) ON DELETE CASCADE,
  CONSTRAINT `fk_chats_user2` FOREIGN KEY (`user2Id`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chats`
--

LOCK TABLES `chats` WRITE;
/*!40000 ALTER TABLE `chats` DISABLE KEYS */;
INSERT INTO `chats` VALUES (2,1,2);
/*!40000 ALTER TABLE `chats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eventparticipants`
--

DROP TABLE IF EXISTS `eventparticipants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventparticipants` (
  `userId` int NOT NULL,
  `eventId` int NOT NULL,
  `status` enum('joined','invited','requested','rejected') NOT NULL,
  `joinedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`userId`,`eventId`),
  KEY `fk_eventparticipants_event` (`eventId`),
  CONSTRAINT `fk_eventparticipants_event` FOREIGN KEY (`eventId`) REFERENCES `events` (`eventId`) ON DELETE CASCADE,
  CONSTRAINT `fk_eventparticipants_user` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eventparticipants`
--

LOCK TABLES `eventparticipants` WRITE;
/*!40000 ALTER TABLE `eventparticipants` DISABLE KEYS */;
INSERT INTO `eventparticipants` VALUES (1,1,'joined','2025-06-01 19:10:57'),(1,2,'joined','2025-06-08 02:07:37'),(1,4,'joined','2025-06-08 09:44:02'),(1,6,'joined','2025-06-08 13:47:54'),(2,1,'joined','2025-06-01 19:46:49'),(3,4,'joined','2025-06-08 11:22:49'),(4,6,'joined','2025-06-08 23:49:47'),(5,2,'joined','2025-06-08 23:56:43'),(5,8,'joined','2025-06-08 23:58:09'),(6,8,'joined','2025-06-11 01:37:10'),(6,9,'joined','2025-06-11 01:38:57'),(7,8,'joined','2025-06-11 17:45:51'),(7,10,'joined','2025-06-11 17:48:21');
/*!40000 ALTER TABLE `eventparticipants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `events` (
  `eventId` int NOT NULL AUTO_INCREMENT,
  `creatorId` int NOT NULL,
  `eventName` varchar(100) NOT NULL,
  `eventLocation` varchar(100) DEFAULT NULL,
  `startTime` datetime NOT NULL,
  `endTime` datetime NOT NULL,
  `description` text,
  `createdAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `maxParticipants` int NOT NULL,
  `isPrivate` tinyint(1) NOT NULL DEFAULT '0',
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`eventId`),
  KEY `fk_events_creator` (`creatorId`),
  CONSTRAINT `fk_events_creator` FOREIGN KEY (`creatorId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
INSERT INTO `events` VALUES (1,1,'Dolma yeme (canım dolma çekti)','Antartika','2025-06-06 12:40:00','2025-06-06 13:00:00','Etkinlik woo','2025-06-01 19:10:57',20,0,NULL),(2,1,'Yaris','Dunya','2025-06-17 23:06:38','2025-06-20 22:06:38','Dunyann etrafinda ilk tur atan kazanir','2025-06-08 02:07:37',5,0,NULL),(4,1,'A',NULL,'2025-06-16 01:43:49','2025-06-23 15:43:49','a','2025-06-08 09:44:02',2,0,NULL),(6,1,'C','C','2025-06-16 10:47:37','2025-06-23 17:47:37','a','2025-06-08 13:47:54',3,0,NULL),(8,5,'Yemek','Manisa','2025-06-23 20:57:35','2025-06-30 04:57:35','En hizli kim','2025-06-08 23:58:09',5,0,NULL),(9,6,'Sinema','US','2025-06-18 15:37:28','2025-06-19 15:37:28',NULL,'2025-06-11 01:38:57',3,0,NULL),(10,7,'Yaris','Forked River, NJ 08731, USA','2025-06-17 17:47:17','2025-06-23 05:47:17','....','2025-06-11 17:48:21',10,0,NULL);
/*!40000 ALTER TABLE `events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendships`
--

DROP TABLE IF EXISTS `friendships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendships` (
  `senderId` int NOT NULL,
  `receiverId` int NOT NULL,
  `status` enum('accepted','rejected','pending') NOT NULL DEFAULT 'pending',
  PRIMARY KEY (`senderId`,`receiverId`),
  KEY `fk_friendships_receiver` (`receiverId`),
  CONSTRAINT `fk_friendships_receiver` FOREIGN KEY (`receiverId`) REFERENCES `users` (`userId`) ON DELETE CASCADE,
  CONSTRAINT `fk_friendships_sender` FOREIGN KEY (`senderId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendships`
--

LOCK TABLES `friendships` WRITE;
/*!40000 ALTER TABLE `friendships` DISABLE KEYS */;
INSERT INTO `friendships` VALUES (1,2,'accepted'),(1,4,'rejected'),(3,1,'rejected'),(6,5,'pending'),(7,6,'pending');
/*!40000 ALTER TABLE `friendships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groupmembers`
--

DROP TABLE IF EXISTS `groupmembers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groupmembers` (
  `groupId` int NOT NULL,
  `userId` int NOT NULL,
  `role` enum('admin','member') NOT NULL DEFAULT 'member',
  `joinedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`groupId`,`userId`),
  KEY `fk_groupmembers_user` (`userId`),
  CONSTRAINT `fk_groupmembers_group` FOREIGN KEY (`groupId`) REFERENCES `usergroups` (`groupId`) ON DELETE CASCADE,
  CONSTRAINT `fk_groupmembers_user` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groupmembers`
--

LOCK TABLES `groupmembers` WRITE;
/*!40000 ALTER TABLE `groupmembers` DISABLE KEYS */;
INSERT INTO `groupmembers` VALUES (1,1,'admin','2025-06-08 17:50:56'),(3,1,'admin','2025-06-08 19:22:18'),(3,2,'member','2025-06-08 20:50:49'),(8,1,'admin','2025-07-27 03:58:11'),(9,1,'admin','2025-07-27 03:59:05');
/*!40000 ALTER TABLE `groupmembers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groupmessages`
--

DROP TABLE IF EXISTS `groupmessages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groupmessages` (
  `groupId` int NOT NULL,
  `messageId` int NOT NULL,
  PRIMARY KEY (`groupId`,`messageId`),
  KEY `fk_groupmessages_message` (`messageId`),
  CONSTRAINT `fk_groupmessages_group` FOREIGN KEY (`groupId`) REFERENCES `usergroups` (`groupId`) ON DELETE CASCADE,
  CONSTRAINT `fk_groupmessages_message` FOREIGN KEY (`messageId`) REFERENCES `messages` (`messageId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groupmessages`
--

LOCK TABLES `groupmessages` WRITE;
/*!40000 ALTER TABLE `groupmessages` DISABLE KEYS */;
INSERT INTO `groupmessages` VALUES (1,5);
/*!40000 ALTER TABLE `groupmessages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `messageId` int NOT NULL AUTO_INCREMENT,
  `senderId` int NOT NULL,
  `content` text NOT NULL,
  `sentAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`messageId`),
  KEY `fk_messages_sender` (`senderId`),
  CONSTRAINT `fk_messages_sender` FOREIGN KEY (`senderId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (5,1,'s','2025-06-08 18:04:46'),(7,6,'Hello','2025-06-11 01:43:36'),(8,1,'Merhaba','2025-06-11 17:49:24');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notificationId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `type` enum('friend_request','friend_request_accepted','friend_request_rejected','event_invite','event_invite_accept','event_invite_decline','message','reminder','system') DEFAULT NULL,
  `content` json NOT NULL,
  `isRead` tinyint(1) NOT NULL DEFAULT '0',
  `sentAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notificationId`),
  KEY `fk_notifications_user` (`userId`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'message','{\"chatId\": \"1\", \"message\": \"Hello!\", \"senderId\": \"1\"}',0,'2025-06-01 18:33:53'),(2,2,'friend_request','{\"senderId\": \"1\"}',0,'2025-06-01 18:49:35'),(4,2,'event_invite_decline','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully left event\"}',0,'2025-06-01 19:43:09'),(5,2,'event_invite_accept','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully joined event\"}',0,'2025-06-01 19:43:13'),(6,2,'event_invite_decline','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully left event\"}',0,'2025-06-01 19:43:17'),(7,2,'event_invite_accept','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully joined event\"}',0,'2025-06-01 19:46:49'),(9,1,'friend_request','{\"senderId\": 3}',0,'2025-06-08 10:00:03'),(10,3,'event_invite_accept','{\"userId\": 3, \"eventId\": 4, \"message\": \"Succesfully joined event\"}',0,'2025-06-08 11:22:47'),(11,3,'event_invite_decline','{\"userId\": 3, \"eventId\": 4, \"message\": \"Succesfully left A\"}',0,'2025-06-08 11:22:49'),(12,3,'event_invite_accept','{\"userId\": 3, \"eventId\": 4, \"message\": \"Succesfully joined event\"}',0,'2025-06-08 11:22:49'),(13,3,'friend_request_rejected','{\"receiverId\": 1}',0,'2025-06-08 17:51:24'),(14,2,'message','{\"chatId\": \"1\", \"message\": \"a\", \"senderId\": 1}',0,'2025-06-08 18:04:39'),(15,2,'message','{\"chatId\": \"1\", \"message\": \"s\", \"senderId\": 1}',0,'2025-06-08 18:23:25'),(16,4,'event_invite_accept','{\"userId\": 4, \"eventId\": 6, \"message\": \"Successfully joined event\"}',0,'2025-06-08 23:49:47'),(17,5,'event_invite_accept','{\"userId\": 5, \"eventId\": 2, \"message\": \"Successfully joined event\"}',0,'2025-06-08 23:56:43'),(18,1,'friend_request','{\"senderId\": 5}',0,'2025-06-08 23:56:50'),(19,6,'event_invite_accept','{\"userId\": 6, \"eventId\": 8, \"message\": \"Successfully joined event\"}',0,'2025-06-11 01:37:10'),(20,5,'friend_request','{\"senderId\": 6}',0,'2025-06-11 01:37:17'),(21,5,'friend_request_accepted','{\"receiverId\": 1}',0,'2025-06-11 02:05:56'),(22,4,'friend_request','{\"senderId\": 1}',0,'2025-06-11 02:14:29'),(23,1,'friend_request_rejected','{\"receiverId\": 4}',0,'2025-06-11 02:15:08'),(24,6,'friend_request','{\"senderId\": 7}',0,'2025-06-11 17:46:34'),(25,2,'message','{\"chatId\": \"2\", \"message\": \"Merhaba\", \"senderId\": 1}',0,'2025-06-11 17:49:24');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usergroups`
--

DROP TABLE IF EXISTS `usergroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usergroups` (
  `groupId` int NOT NULL AUTO_INCREMENT,
  `groupName` varchar(100) NOT NULL,
  `groupDescription` text,
  `createdBy` int NOT NULL,
  `createdAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `groupImage` varchar(255) DEFAULT NULL,
  `eventId` int DEFAULT NULL,
  PRIMARY KEY (`groupId`),
  UNIQUE KEY `eventId` (`eventId`),
  KEY `fk_usergroups_creator` (`createdBy`),
  CONSTRAINT `fk_usergroups_creator` FOREIGN KEY (`createdBy`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usergroups`
--

LOCK TABLES `usergroups` WRITE;
/*!40000 ALTER TABLE `usergroups` DISABLE KEYS */;
INSERT INTO `usergroups` VALUES (1,'A Grubu','Bu grup, \'A\' etkinliğinin katılımcıları içindir.',1,'2025-06-08 17:50:56',NULL,7),(3,'Sa',NULL,1,'2025-06-08 19:22:17',NULL,NULL),(8,'Test',NULL,1,'2025-07-27 03:58:11',NULL,NULL),(9,'Tatatat Grubu','Bu grup, \'Tatatat\' etkinliğinin katılımcıları içindir.',1,'2025-07-27 03:59:05',NULL,11);
/*!40000 ALTER TABLE `usergroups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `userId` int NOT NULL AUTO_INCREMENT,
  `fullName` varchar(100) NOT NULL,
  `userName` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `passwordHash` varchar(255) NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `joinDate` date NOT NULL DEFAULT (curdate()),
  `lastLogin` date DEFAULT NULL,
  `profileImage` varchar(255) DEFAULT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  `fcm_token` text,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `userName` (`userName`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Ali Yılmaz','Ali','ali@example.com','$2b$10$cpTjrwy3b4dOLJIXZEklOOhO5O1SYr9B7FTTnbXIPPQhdYb5AXfM2','Mountain View, United States','Merhaba','2025-05-31',NULL,NULL,'5554',NULL),(2,'test','test','test@example.com','$2b$10$.bdyZqi9.LcZ4uooiRRmmOwbp177e2cefov/Ldd3EGRUqbjzNgo92',NULL,NULL,'2025-06-01',NULL,NULL,NULL,NULL),(3,'test','test2','test2@example.com','$2b$10$owtoxr2icWhWdPMxLicyH.7CLbo4sRuVjCiTqgJub/R6Cnjfk6/OS','Mountain View, United States',NULL,'2025-06-06',NULL,NULL,NULL,NULL),(4,'Ahmet Ylmaz','ahmet','ahmet@exapmle.com','$2b$10$yqmWKmsufD1nnMQwGADYLOdd9ZHGWE/lHPLAf5DXMxQ2zMF2BYTHq','Mountain View, United States',NULL,'2025-06-08',NULL,NULL,NULL,NULL),(5,'AsdF','asd','asd@gmail.com','$2b$10$QK8canmP8CytuSETLzkMpu60opK1p.yZ5z3RdE4HsMY1k6pfGeG6G','Lat: 37.4219983, Lon: -122.084','Turkey','2025-06-08',NULL,NULL,'',NULL),(6,'Ryan','Ryan','a@example.com','$2b$10$g7urqKg5bGEwhqYureaG0.G/dVPPt8lOwXaqtIhr1outzZ/OJTbFu','Mountain View, United States','Merhaba','2025-06-11',NULL,NULL,'',NULL),(7,'Asd','aadssa','as@example.com','$2b$10$Q5xMnq62KpYs7kzg9phVGObitgM.l3rKy85GW2/.v52lvKQakJcqe','Mountain View, United States',NULL,'2025-06-11',NULL,NULL,NULL,NULL);
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

-- Dump completed on 2025-07-27  4:40:50
