# Introduction #

Invoke [DBUnit](http://www.dbunit.org/) to import a database dump from xml or excel format.
Optional execution of DELETE with a different dataset possible.

## Usage ##
You can use the invocation "dbSetup" from within the dbmigrate tool's xml configuration (see [xml script features](http://code.google.com/p/agimatec-tools/wiki/DBMigrateXmlScript)).

```
java com.agimatec.database.DbUnitSetupTool {driver} {url} {user} {password} [-d {deleteDataFile}] [-i {insertDataFile}]
```


## Options ##
Parameters in required sequence:
  * driver  = Jdbc Driver class name
  * url     = Jdbc Connect url
  * user    = database user
  * password = database password
  * -d dataset with tables to delete (delete\_data.xml)
  * -i dataset with data to insert  (data.xml)

# Example #

```
java com.agimatec.database.DbUnitSetupToolorg.postgresql.Driver jdbc:postgresql://localhost:5432/testdb admin password -d delete_data.xml -i testdb.xml
```