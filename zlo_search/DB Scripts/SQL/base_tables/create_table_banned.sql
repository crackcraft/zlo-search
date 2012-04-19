
create table banned (
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  ip VARCHAR(15),
  reason VARCHAR(512) NULL,
  ban_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY (ip)
)
ENGINE=INNODB
DEFAULT CHARSET=cp1251;