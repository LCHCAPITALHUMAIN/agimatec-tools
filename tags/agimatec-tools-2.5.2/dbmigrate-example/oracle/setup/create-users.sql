CREATE USER ${DB_testdb} PROFILE "DEFAULT" IDENTIFIED BY "${DB_testdb_PASSWORD}"
    TEMPORARY TABLESPACE "TEMP" ACCOUNT UNLOCK;
GRANT UNLIMITED TABLESPACE TO ${DB_testdb};
GRANT "CONNECT" TO ${DB_testdb};
GRANT "RESOURCE" TO ${DB_testdb};
GRANT CREATE VIEW to ${DB_testdb};
