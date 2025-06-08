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
INSERT INTO `chatmessages` VALUES (1,1),(1,2),(1,3);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chats`
--

LOCK TABLES `chats` WRITE;
/*!40000 ALTER TABLE `chats` DISABLE KEYS */;
INSERT INTO `chats` VALUES (1,1,2);
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
INSERT INTO `eventparticipants` VALUES (1,1,'joined','2025-06-01 19:10:57'),(1,2,'joined','2025-06-08 02:07:37'),(2,1,'joined','2025-06-01 19:46:49');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
INSERT INTO `events` VALUES (1,1,'Dolma yeme (canım dolma çekti)','Antartika','2025-06-06 12:40:00','2025-06-06 13:00:00','Etkinlik woo','2025-06-01 19:10:57',20,0,NULL),(2,1,'Yaris','Dunya','2025-06-17 23:06:38','2025-06-20 22:06:38','Dunyann etrafinda ilk tur atan kazanir','2025-06-08 02:07:37',5,0,NULL);
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
INSERT INTO `friendships` VALUES (1,2,'accepted');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,1,'Hello!','2025-06-01 18:27:07'),(2,1,'Hello!','2025-06-01 18:33:39'),(3,1,'Hello!','2025-06-01 18:33:53');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'message','{\"chatId\": \"1\", \"message\": \"Hello!\", \"senderId\": \"1\"}',0,'2025-06-01 18:33:53'),(2,2,'friend_request','{\"senderId\": \"1\"}',0,'2025-06-01 18:49:35'),(3,1,'friend_request','{\"receiverId\": \"2\"}',0,'2025-06-01 18:49:45'),(4,2,'event_invite_decline','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully left event\"}',0,'2025-06-01 19:43:09'),(5,2,'event_invite_accept','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully joined event\"}',0,'2025-06-01 19:43:13'),(6,2,'event_invite_decline','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully left event\"}',0,'2025-06-01 19:43:17'),(7,2,'event_invite_accept','{\"userId\": 2, \"eventId\": 1, \"message\": \"Succesfully joined event\"}',0,'2025-06-01 19:46:49');
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
  PRIMARY KEY (`groupId`),
  KEY `fk_usergroups_creator` (`createdBy`),
  CONSTRAINT `fk_usergroups_creator` FOREIGN KEY (`createdBy`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usergroups`
--

LOCK TABLES `usergroups` WRITE;
/*!40000 ALTER TABLE `usergroups` DISABLE KEYS */;
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
  PRIMARY KEY (`userId`),
  UNIQUE KEY `userName` (`userName`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Ali Yılmaz','Ali','ali@example.com','$2b$10$cpTjrwy3b4dOLJIXZEklOOhO5O1SYr9B7FTTnbXIPPQhdYb5AXfM2','Mountain View, United States','Merhaba','2025-05-31',NULL,NULL,'5554'),(2,'test','test','test@example.com','$2b$10$.bdyZqi9.LcZ4uooiRRmmOwbp177e2cefov/Ldd3EGRUqbjzNgo92',NULL,NULL,'2025-06-01',NULL,NULL,NULL),(3,'test','test2','test2@example.com','$2b$10$owtoxr2icWhWdPMxLicyH.7CLbo4sRuVjCiTqgJub/R6Cnjfk6/OS',NULL,NULL,'2025-06-06',NULL,NULL,NULL);
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

-- Dump completed on 2025-06-08  5:27:59
