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
) ENGINE=InnoDB AUTO_INCREMENT=453 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (413,82,'product',NULL,NULL,0,NULL,NULL,3,4,3.99),(414,82,'ticket',24,30,1,'321312','31321',NULL,1,16.50),(415,83,'ticket',24,19,1,'ahaha','ahahah',NULL,1,16.50),(416,83,'ticket',24,20,1,'ahaha','ahahah',NULL,1,16.50),(417,83,'ticket',24,21,1,'ahaha','ahahah',NULL,1,16.50),(418,83,'ticket',24,22,1,'ahaha','ahahah',NULL,1,16.50),(419,83,'product',NULL,NULL,0,NULL,NULL,1,5,35.00),(420,83,'product',NULL,NULL,0,NULL,NULL,2,5,3.99),(421,84,'product',NULL,NULL,0,NULL,NULL,4,4,2.99),(422,84,'product',NULL,NULL,0,NULL,NULL,3,3,3.99),(423,84,'product',NULL,NULL,0,NULL,NULL,7,3,5.99),(424,84,'ticket',24,37,0,'Volkan','Erdogan',NULL,1,33.00),(425,84,'ticket',24,36,0,'Volkan','Erdogan',NULL,1,33.00),(426,85,'product',NULL,NULL,0,NULL,NULL,3,3,3.99),(427,85,'ticket',24,29,1,'haha','haha',NULL,1,16.50),(428,86,'ticket',24,14,0,'3213','213213',NULL,1,33.00),(429,87,'ticket',24,31,1,'123','123',NULL,1,16.50),(430,87,'product',NULL,NULL,0,NULL,NULL,16,3,150.00),(431,88,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(432,88,'ticket',24,38,1,'c','fsdf',NULL,1,16.50),(433,89,'ticket',24,24,1,'adsa','dasdas',NULL,1,16.50),(434,90,'ticket',24,1,0,'Ozan','Nurcan',NULL,1,33.00),(435,90,'ticket',24,2,0,'Ozan','Nurcan',NULL,1,33.00),(436,90,'product',NULL,NULL,0,NULL,NULL,22,3,150.00),(437,90,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(438,91,'product',NULL,NULL,0,NULL,NULL,7,4,5.99),(439,91,'ticket',24,12,1,'Volkan','Erdoga',NULL,1,16.50),(440,91,'ticket',24,13,1,'Volkan','Erdoga',NULL,1,16.50),(441,92,'ticket',24,13,1,'vava','vava',NULL,1,16.50),(442,93,'product',NULL,NULL,0,NULL,NULL,1,3,35.00),(443,93,'ticket',24,39,1,'Volkan','Erdogan',NULL,1,16.50),(444,94,'product',NULL,NULL,0,NULL,NULL,14,2,4.99),(445,94,'ticket',24,37,1,'Volkan','Erdogan',NULL,1,16.50),(446,95,'ticket',24,28,1,'vv','vv',NULL,1,16.50),(447,96,'ticket',24,30,1,'Volkan','eRDOGAN',NULL,1,16.50),(448,96,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(449,97,'ticket',24,21,1,'Volkan','Erdogan',NULL,1,16.50),(450,97,'ticket',24,27,1,'Volkan','Erdogan',NULL,1,16.50),(451,97,'ticket',24,28,0,'Volkan','Erdogan',NULL,1,33.00),(452,97,'product',NULL,NULL,0,NULL,NULL,2,3,3.99);
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

-- Dump completed on 2025-01-08 14:07:01
