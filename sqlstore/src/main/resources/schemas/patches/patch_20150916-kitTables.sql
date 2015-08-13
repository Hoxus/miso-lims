DROP TABLE IF EXISTS `KitChangeLog`;
CREATE TABLE `KitChangeLog` (
  `kitChangeLogId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `kitComponentId` bigint(20) NOT NULL,
  `locationBarcodeOld` varchar(255) DEFAULT NULL,
  `locationBarcodeNew` varchar(255) DEFAULT NULL,
  `exhausted` tinyint(1),
  `logDate` TIMESTAMP,
  PRIMARY KEY(`kitChangeLogId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `KitDescriptor`;
CREATE TABLE `KitDescriptor` (
  `kitDescriptorId` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `version` int(3) DEFAULT NULL,
  `manufacturer` varchar(100) NOT NULL,
  `partNumber` varchar(50) NOT NULL,
  `kitType` varchar(30) NOT NULL,
  `platformType` varchar(20) NOT NULL,
  `units` varchar(20) NOT NULL,
  `kitValue` DECIMAL NOT NULL,
  PRIMARY KEY (`kitDescriptorId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `KitComponentDescriptor`;
CREATE TABLE `KitComponentDescriptor` (
  `kitComponentDescriptorId` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `referenceNumber` varchar(50) NOT NULL,
  `kitDescriptorId` bigint(20) NOT NULL,
  PRIMARY KEY (`kitComponentDescriptorId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `KitComponent`;
CREATE TABLE `KitComponent` (
  `kitComponentId` bigint(20) NOT NULL AUTO_INCREMENT,
  `identificationBarcode` varchar(255) DEFAULT NULL,
  `locationBarcode` varchar(255) DEFAULT NULL,
  `lotNumber` varchar(30) NOT NULL,
  `kitReceivedDate` date NOT NULL,
  `kitExpiryDate` date NOT NULL,
  `exhausted` tinyint(1),
  `kitComponentDescriptorId` bigint(20) NOT NULL,
  PRIMARY KEY (`kitComponentId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;