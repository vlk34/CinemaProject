-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: cinemadb
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
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `item_type` enum('ticket','product') NOT NULL,
  `schedule_id` int DEFAULT NULL,
  `seat_number` int DEFAULT NULL,
  `discount_applied` tinyint(1) NOT NULL DEFAULT '0',
  `occupant_first_name` varchar(50) DEFAULT NULL,
  `occupant_last_name` varchar(50) DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `item_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `fk_oitems_orders` (`order_id`),
  KEY `fk_oitems_schedule` (`schedule_id`),
  KEY `fk_oitems_product` (`product_id`),
  CONSTRAINT `fk_oitems_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `fk_oitems_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `fk_oitems_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=746 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (712,162,'product',NULL,NULL,0,NULL,NULL,5,2,3.99),(713,162,'ticket',30,6,0,'asda','asd',NULL,1,51.00),(714,162,'ticket',30,7,0,'asda','asd',NULL,1,51.00),(715,162,'ticket',30,10,0,'asda','asd',NULL,1,51.00),(716,162,'ticket',30,11,0,'asda','asd',NULL,1,51.00),(717,163,'ticket',31,20,0,'asd','asd',NULL,1,50.00),(718,163,'ticket',31,21,0,'asd','asd',NULL,1,50.00),(719,163,'ticket',31,22,0,'asd','asd',NULL,1,50.00),(720,163,'ticket',31,28,0,'asd','asd',NULL,1,50.00),(721,163,'ticket',31,29,0,'asd','asd',NULL,1,50.00),(722,163,'ticket',31,30,0,'asd','asd',NULL,1,50.00),(723,163,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(724,164,'ticket',31,18,1,'Volk','VOlk',NULL,1,25.00),(725,164,'ticket',31,19,0,'Volk','VOlk',NULL,1,50.00),(726,164,'ticket',31,20,0,'Volk','VOlk',NULL,1,50.00),(727,164,'ticket',31,21,0,'Volk','VOlk',NULL,1,50.00),(728,164,'ticket',31,22,0,'Volk','VOlk',NULL,1,50.00),(729,164,'ticket',31,23,0,'Volk','VOlk',NULL,1,50.00),(730,164,'ticket',31,26,0,'Volk','VOlk',NULL,1,50.00),(731,164,'ticket',31,27,0,'Volk','VOlk',NULL,1,50.00),(732,164,'ticket',31,28,0,'Volk','VOlk',NULL,1,50.00),(733,164,'ticket',31,29,0,'Volk','VOlk',NULL,1,50.00),(734,164,'ticket',31,30,0,'Volk','VOlk',NULL,1,50.00),(735,164,'ticket',31,31,0,'Volk','VOlk',NULL,1,50.00),(736,164,'product',NULL,NULL,0,NULL,NULL,16,20,150.00),(737,165,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(738,165,'ticket',33,20,0,'asd','asd',NULL,1,50.00),(739,165,'ticket',33,21,0,'asd','asd',NULL,1,50.00),(740,165,'ticket',33,28,0,'asd','asd',NULL,1,50.00),(741,165,'ticket',33,29,0,'asd','asd',NULL,1,50.00),(742,166,'ticket',31,24,0,'asd','asd',NULL,1,50.00),(743,167,'ticket',31,32,0,'asd','asd',NULL,1,50.00),(744,168,'ticket',31,25,0,'asda','sada',NULL,1,50.00),(745,168,'product',NULL,NULL,0,NULL,NULL,22,2,150.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-01-11 22:35:45
