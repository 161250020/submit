-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: mobile_operator_service
-- ------------------------------------------------------
-- Server version	5.7.17-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `base_service`
--

DROP TABLE IF EXISTS `base_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `base_service` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `expense` float NOT NULL,
  `level` int(11) NOT NULL,
  `exit` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_service`
--

LOCK TABLES `base_service` WRITE;
/*!40000 ALTER TABLE `base_service` DISABLE KEYS */;
INSERT INTO `base_service` VALUES (1,'通话',0.5,0,'0.5元/分钟，仅拨打收费，接听免费'),(2,'短信',0.1,0,'0.1元/条'),(3,'本地流量（4G）',2,0,'2元/M，仅考虑4G流量'),(4,'国内流量（4G）',5,0,'5元/M，仅考虑4G流量');
/*!40000 ALTER TABLE `base_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal_content`
--

DROP TABLE IF EXISTS `meal_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meal_content` (
  `meal_id` int(11) NOT NULL,
  `base_service_id` int(11) NOT NULL,
  `amount` float NOT NULL,
  `exit` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal_content`
--

LOCK TABLES `meal_content` WRITE;
/*!40000 ALTER TABLE `meal_content` DISABLE KEYS */;
INSERT INTO `meal_content` VALUES (1,1,100,'套餐1包含基准服务1的数量为100'),(2,2,200,'套餐2包含基准服务2的数量为200'),(3,3,2048,'套餐3包含基准服务3的数量为2048'),(4,4,2048,'套餐4包含基准服务4的数量为2048'),(5,1,100,'套餐5包含基准服务1的数量为100'),(5,2,200,'套餐5包含基准服务2的数量为200'),(6,3,1024,'套餐6包含基准服务3的数量为1024'),(6,4,2048,'套餐6包含基准服务4的数量为2048');
/*!40000 ALTER TABLE `meal_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal_operate_content`
--

DROP TABLE IF EXISTS `meal_operate_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meal_operate_content` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `exit` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal_operate_content`
--

LOCK TABLES `meal_operate_content` WRITE;
/*!40000 ALTER TABLE `meal_operate_content` DISABLE KEYS */;
INSERT INTO `meal_operate_content` VALUES (1,'订购','订购套餐'),(2,'退订立即生效','退订套餐，立即生效，套餐花费按照基准资费计算，退还套餐费'),(3,'退订次月生效','退订套餐，但是本月套餐依旧可以使用，次月套餐被退订');
/*!40000 ALTER TABLE `meal_operate_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal_service`
--

DROP TABLE IF EXISTS `meal_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meal_service` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `expense` float NOT NULL,
  `exit` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal_service`
--

LOCK TABLES `meal_service` WRITE;
/*!40000 ALTER TABLE `meal_service` DISABLE KEYS */;
INSERT INTO `meal_service` VALUES (1,'话费套餐',20,'套餐1为话费套餐，每月20元'),(2,'短信套餐',10,'套餐2为短信套餐，每月10元'),(3,'本地流量套餐',20,'套餐3为本地流量套餐，每月10元'),(4,'国内流量套餐',30,'套餐4为国内流量套餐，每月10元'),(5,'话费短信套餐',25,'套餐5为话费和短信套餐套餐，通话100分钟，200条短信'),(6,'本地国内流量套餐',35,'套餐6为本地和国内流量套餐，1024M本地流量，2048M国内流量');
/*!40000 ALTER TABLE `meal_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_meal_operator`
--

DROP TABLE IF EXISTS `user_meal_operator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_meal_operator` (
  `user_id` int(11) NOT NULL,
  `operate_id` int(11) NOT NULL,
  `meal_id` int(11) NOT NULL,
  `operate_time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_meal_operator`
--

LOCK TABLES `user_meal_operator` WRITE;
/*!40000 ALTER TABLE `user_meal_operator` DISABLE KEYS */;
INSERT INTO `user_meal_operator` VALUES (1,1,1,'2018-01-01 10:10:10');
/*!40000 ALTER TABLE `user_meal_operator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_meal_outside_charge`
--

DROP TABLE IF EXISTS `user_meal_outside_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_meal_outside_charge` (
  `user_id` int(11) NOT NULL,
  `base_service_id` int(11) NOT NULL,
  `consume` float NOT NULL,
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_meal_outside_charge`
--

LOCK TABLES `user_meal_outside_charge` WRITE;
/*!40000 ALTER TABLE `user_meal_outside_charge` DISABLE KEYS */;
INSERT INTO `user_meal_outside_charge` VALUES (1,1,1,'2018-01-01');
/*!40000 ALTER TABLE `user_meal_outside_charge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_meal_surplus`
--

DROP TABLE IF EXISTS `user_meal_surplus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_meal_surplus` (
  `id` varchar(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `meal_id` int(11) NOT NULL,
  `base_service_id` int(11) NOT NULL,
  `surplus` float NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_meal_surplus`
--

LOCK TABLES `user_meal_surplus` WRITE;
/*!40000 ALTER TABLE `user_meal_surplus` DISABLE KEYS */;
INSERT INTO `user_meal_surplus` VALUES ('2018101010101010',1,1,1,90,'2018-10-10');
/*!40000 ALTER TABLE `user_meal_surplus` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-10-29 17:39:16
