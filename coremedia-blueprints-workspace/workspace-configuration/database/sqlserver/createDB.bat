sqlcmd -i %~dp0\create_cm_caefeeder.sql
sqlcmd -i %~dp0\create_cm_management.sql
sqlcmd -i %~dp0\create_cm_master.sql
sqlcmd -i %~dp0\create_cm_mcaefeeder.sql
sqlcmd -i %~dp0\create_cm_replication.sql

