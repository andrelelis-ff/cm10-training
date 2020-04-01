-- DB config for MySql

CREATE SCHEMA cm_management CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON cm_management.*
 TO 'cm_management'@'localhost'
  IDENTIFIED BY 'cm_management';

CREATE SCHEMA cm_caefeeder CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON cm_caefeeder.*
 TO 'cm_caefeeder'@'localhost'
  IDENTIFIED BY 'cm_caefeeder';

CREATE SCHEMA cm_master CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON cm_master.*
 TO 'cm_master'@'localhost'
  IDENTIFIED BY 'cm_master';

CREATE SCHEMA cm_mcaefeeder CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON cm_mcaefeeder.*
 TO 'cm_mcaefeeder'@'localhost'
  IDENTIFIED BY 'cm_mcaefeeder';

CREATE SCHEMA cm_replication CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
GRANT ALL PRIVILEGES ON cm_replication.*
 TO 'cm_replication'@'localhost'
  IDENTIFIED BY 'cm_replication';

