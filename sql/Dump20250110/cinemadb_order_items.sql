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
) ENGINE=InnoDB AUTO_INCREMENT=552 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (413,82,'product',NULL,NULL,0,NULL,NULL,3,4,3.99),(414,82,'ticket',24,30,1,'321312','31321',NULL,1,16.50),(415,83,'ticket',24,19,1,'ahaha','ahahah',NULL,1,16.50),(416,83,'ticket',24,20,1,'ahaha','ahahah',NULL,1,16.50),(417,83,'ticket',24,21,1,'ahaha','ahahah',NULL,1,16.50),(418,83,'ticket',24,22,1,'ahaha','ahahah',NULL,1,16.50),(419,83,'product',NULL,NULL,0,NULL,NULL,1,5,35.00),(420,83,'product',NULL,NULL,0,NULL,NULL,2,5,3.99),(421,84,'product',NULL,NULL,0,NULL,NULL,4,4,2.99),(422,84,'product',NULL,NULL,0,NULL,NULL,3,3,3.99),(423,84,'product',NULL,NULL,0,NULL,NULL,7,3,5.99),(424,84,'ticket',24,37,0,'Volkan','Erdogan',NULL,1,33.00),(425,84,'ticket',24,36,0,'Volkan','Erdogan',NULL,1,33.00),(426,85,'product',NULL,NULL,0,NULL,NULL,3,3,3.99),(427,85,'ticket',24,29,1,'haha','haha',NULL,1,16.50),(428,86,'ticket',24,14,0,'3213','213213',NULL,1,33.00),(429,87,'ticket',24,31,1,'123','123',NULL,1,16.50),(430,87,'product',NULL,NULL,0,NULL,NULL,16,3,150.00),(431,88,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(432,88,'ticket',24,38,1,'c','fsdf',NULL,1,16.50),(433,89,'ticket',24,24,1,'adsa','dasdas',NULL,1,16.50),(434,90,'ticket',24,1,0,'Ozan','Nurcan',NULL,1,33.00),(435,90,'ticket',24,2,0,'Ozan','Nurcan',NULL,1,33.00),(436,90,'product',NULL,NULL,0,NULL,NULL,22,3,150.00),(437,90,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(438,91,'product',NULL,NULL,0,NULL,NULL,7,4,5.99),(439,91,'ticket',24,12,1,'Volkan','Erdoga',NULL,1,16.50),(440,91,'ticket',24,13,1,'Volkan','Erdoga',NULL,1,16.50),(441,92,'ticket',24,13,1,'vava','vava',NULL,1,16.50),(442,93,'product',NULL,NULL,0,NULL,NULL,1,3,35.00),(443,93,'ticket',24,39,1,'Volkan','Erdogan',NULL,1,16.50),(444,94,'product',NULL,NULL,0,NULL,NULL,14,2,4.99),(445,94,'ticket',24,37,1,'Volkan','Erdogan',NULL,1,16.50),(446,95,'ticket',24,28,1,'vv','vv',NULL,1,16.50),(447,96,'ticket',24,30,1,'Volkan','eRDOGAN',NULL,1,16.50),(448,96,'product',NULL,NULL,0,NULL,NULL,16,2,150.00),(449,97,'ticket',24,21,1,'Volkan','Erdogan',NULL,1,16.50),(450,97,'ticket',24,27,1,'Volkan','Erdogan',NULL,1,16.50),(451,97,'ticket',24,28,0,'Volkan','Erdogan',NULL,1,33.00),(452,97,'product',NULL,NULL,0,NULL,NULL,2,3,3.99),(453,98,'ticket',24,28,1,'123213','2131321',NULL,1,16.50),(454,99,'ticket',24,19,1,'Volkan','Erdogan',NULL,1,16.50),(455,99,'ticket',24,20,1,'Volkan','Erdogan',NULL,1,16.50),(456,99,'ticket',24,27,0,'Volkan','Erdogan',NULL,1,33.00),(457,99,'product',NULL,NULL,0,NULL,NULL,6,2,6.99),(458,100,'ticket',25,12,0,'15','15',NULL,1,25.00),(459,101,'ticket',24,21,1,'Volkan','Erdogan',NULL,1,25.00),(460,101,'ticket',24,22,1,'Volkan','Erdogan',NULL,1,25.00),(461,101,'product',NULL,NULL,0,NULL,NULL,16,3,150.00),(462,101,'product',NULL,NULL,0,NULL,NULL,5,3,3.99),(463,102,'product',NULL,NULL,0,NULL,NULL,1,1,35.00),(464,102,'ticket',24,12,1,'Volkan','erd',NULL,1,25.00),(465,103,'product',NULL,NULL,0,NULL,NULL,4,3,2.99),(466,103,'ticket',24,31,0,'adas','dsadas',NULL,1,50.00),(467,104,'product',NULL,NULL,0,NULL,NULL,23,1,1222.00),(468,104,'ticket',24,30,1,'volkan','volkan',NULL,1,25.00),(469,105,'product',NULL,NULL,0,NULL,NULL,2,2,3.99),(470,105,'ticket',24,26,1,'asdas','sadas',NULL,1,25.00),(471,106,'ticket',24,32,1,'haha','haha',NULL,1,25.00),(472,107,'product',NULL,NULL,0,NULL,NULL,2,1,3.99),(473,107,'ticket',24,37,1,'Volkan','Erdogan',NULL,1,25.00),(474,108,'product',NULL,NULL,0,NULL,NULL,24,4,11.00),(475,108,'ticket',25,6,1,'emir o','zen',NULL,1,12.50),(476,108,'ticket',25,7,1,'emir o','zen',NULL,1,12.50),(477,108,'ticket',25,10,0,'emir o','zen',NULL,1,25.00),(478,108,'ticket',25,11,0,'emir o','zen',NULL,1,25.00),(479,109,'ticket',24,23,1,'Volkan','Volkan',NULL,1,25.00),(480,110,'product',NULL,NULL,0,NULL,NULL,3,3,3.99),(481,110,'product',NULL,NULL,0,NULL,NULL,5,1,3.99),(482,110,'ticket',24,38,1,'Volkan','Erdogan',NULL,1,25.00),(483,111,'product',NULL,NULL,0,NULL,NULL,4,8,2.99),(484,111,'ticket',24,36,1,'asd','asd',NULL,1,25.00),(485,112,'product',NULL,NULL,0,NULL,NULL,4,10,2.99),(486,112,'ticket',24,48,0,'ah','ah',NULL,1,50.00),(487,113,'ticket',24,25,1,'123','asd',NULL,1,25.00),(488,113,'product',NULL,NULL,0,NULL,NULL,2,4,3.99),(489,114,'ticket',24,18,1,'volkan','volkan',NULL,1,25.00),(490,114,'product',NULL,NULL,0,NULL,NULL,4,3,2.99),(491,115,'ticket',24,17,1,'hah','hah',NULL,1,25.00),(492,115,'product',NULL,NULL,0,NULL,NULL,4,5,2.99),(493,116,'ticket',24,14,1,'asd','asd',NULL,1,25.00),(494,116,'product',NULL,NULL,0,NULL,NULL,4,5,2.99),(495,117,'product',NULL,NULL,0,NULL,NULL,2,5,3.99),(496,117,'ticket',24,15,0,'haha','haha',NULL,1,50.00),(497,118,'product',NULL,NULL,0,NULL,NULL,7,2,5.99),(498,118,'product',NULL,NULL,0,NULL,NULL,14,1,4.99),(499,118,'ticket',25,8,1,'haha','hahaha',NULL,1,12.50),(500,119,'ticket',25,16,1,'haha','haha',NULL,1,12.50),(501,120,'product',NULL,NULL,0,NULL,NULL,24,7,11.00),(502,120,'ticket',25,16,1,'vv','vvvvvv',NULL,1,12.50),(503,121,'ticket',24,45,0,'asda','asd',NULL,1,50.00),(504,121,'product',NULL,NULL,0,NULL,NULL,22,1,150.00),(505,122,'ticket',24,46,0,'Volk','VOlk',NULL,1,50.00),(506,122,'ticket',24,47,0,'Volk','VOlk',NULL,1,50.00),(507,122,'product',NULL,NULL,0,NULL,NULL,22,2,150.00),(508,123,'product',NULL,NULL,0,NULL,NULL,1,7,35.00),(509,123,'product',NULL,NULL,0,NULL,NULL,16,1,150.00),(510,123,'product',NULL,NULL,0,NULL,NULL,7,1,5.99),(511,123,'product',NULL,NULL,0,NULL,NULL,14,1,4.99),(512,123,'ticket',28,11,1,'vklsdjfds','flkdsjfsklfds',NULL,1,12.50),(513,123,'ticket',28,12,0,'vklsdjfds','flkdsjfsklfds',NULL,1,25.00),(514,123,'ticket',28,15,0,'vklsdjfds','flkdsjfsklfds',NULL,1,25.00),(515,123,'ticket',28,16,0,'vklsdjfds','flkdsjfsklfds',NULL,1,25.00),(516,124,'product',NULL,NULL,0,NULL,NULL,22,1,150.00),(517,124,'product',NULL,NULL,0,NULL,NULL,15,2,8.99),(518,124,'product',NULL,NULL,0,NULL,NULL,14,2,4.99),(519,124,'ticket',24,7,0,'asda','sdsa',NULL,1,50.00),(520,125,'ticket',24,40,1,'asda','sd',NULL,1,25.00),(521,125,'ticket',24,46,1,'asda','sd',NULL,1,25.00),(522,125,'ticket',24,47,0,'asda','sd',NULL,1,50.00),(523,125,'product',NULL,NULL,0,NULL,NULL,5,1,3.99),(524,125,'product',NULL,NULL,0,NULL,NULL,4,1,2.99),(525,125,'product',NULL,NULL,0,NULL,NULL,3,1,3.99),(526,125,'product',NULL,NULL,0,NULL,NULL,7,40,5.99),(527,125,'product',NULL,NULL,0,NULL,NULL,14,7,4.99),(528,125,'product',NULL,NULL,0,NULL,NULL,15,7,8.99),(529,126,'ticket',24,6,1,'asd','asd',NULL,1,25.00),(530,126,'ticket',24,7,1,'asd','asd',NULL,1,25.00),(531,126,'ticket',24,8,0,'asd','asd',NULL,1,50.00),(532,127,'ticket',25,15,1,'saddas','asdas',NULL,1,15.50),(533,127,'ticket',25,16,0,'saddas','asdas',NULL,1,31.00),(534,128,'ticket',25,3,1,'asd','asd',NULL,1,15.50),(535,128,'ticket',25,4,0,'asd','asd',NULL,1,31.00),(536,129,'ticket',24,1,1,'asd','asd',NULL,1,25.00),(537,129,'ticket',24,2,1,'asd','asd',NULL,1,25.00),(538,129,'ticket',24,9,0,'asd','asd',NULL,1,50.00),(539,129,'ticket',24,10,0,'asd','asd',NULL,1,50.00),(540,129,'product',NULL,NULL,0,NULL,NULL,22,2,150.00),(541,130,'product',NULL,NULL,0,NULL,NULL,1,1,35.00),(542,130,'product',NULL,NULL,0,NULL,NULL,14,1,4.99),(543,130,'ticket',29,6,0,'asdasd','asdasd',NULL,1,31.00),(544,130,'ticket',29,7,0,'asdasd','asdasd',NULL,1,31.00),(545,130,'ticket',29,10,0,'asdasd','asdasd',NULL,1,31.00),(546,130,'ticket',29,11,0,'asdasd','asdasd',NULL,1,31.00),(547,131,'product',NULL,NULL,0,NULL,NULL,1,2,35.00),(548,131,'product',NULL,NULL,0,NULL,NULL,2,2,3.99),(549,131,'ticket',29,14,1,'VOlkan','erdoga',NULL,1,15.50),(550,131,'ticket',29,15,0,'VOlkan','erdoga',NULL,1,31.00),(551,131,'product',NULL,NULL,0,NULL,NULL,24,1,11.00);
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

-- Dump completed on 2025-01-10 21:04:12
