DROP TABLE user;
CREATE TABLE user (
  `uid`       INT(11)      NOT NULL AUTO_INCREMENT,
  `name`      VARCHAR(45)  NOT NULL,
  `email`     VARCHAR(128) NOT NULL,
  `password`  VARCHAR(128)          DEFAULT NULL,
  `salt`      VARCHAR(128)          DEFAULT NULL,
  `timestamp` TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `email_UNIQUE` (`email`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8;

DROP TABLE message;
CREATE TABLE message (
  `id`          INT(11)   NOT NULL AUTO_INCREMENT,
  `uid`         INT(11)   NOT NULL,
  `uidForeign`  INT(11)   NOT NULL,
  `message`     TEXT               DEFAULT NULL,
  `timestamp`   TIMESTAMP NOT NULL,
  `messageRead` TINYINT   NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`uid`) REFERENCES owner (`uid`),
  FOREIGN KEY (`uidForeign`) REFERENCES owner (`uid`),
  UNIQUE KEY `id_unique` (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8;

DROP TABLE contact;
CREATE TABLE contact (
  `uid`        INT(11) NOT NULL,
  `uidForeign` INT(11) NOT NULL,
  `notified`   BOOLEAN DEFAULT NULL,
  `timestamp`  TIMESTAMP,
  PRIMARY KEY (`uid`, `uidForeign`),
  FOREIGN KEY (`uid`) REFERENCES owner (`uid`),
  FOREIGN KEY (`uidForeign`) REFERENCES owner (`uid`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8;