# Introduction #

This class has a main() method that allows to invoke other tools (main()) with different parameters.

  * [JavaDoc InvokerTool](http://www.agimatec.de/agimatec-tools/site/dbmigrate/apidocs/com/agimatec/database/InvokerTool.html)
  * each invocation is surrounded by { }

# Example #

Invokes the DDLParserTool 3 times via the InvokerTool:

```
<java fork="true" failonerror="true"
    classname="com.agimatec.database.InvokerTool"
    classpathref="maven.compile.classpath">
  <arg line="{ com.agimatec.sql.meta.script.DDLParserTool "/>
  <arg line="-catalog target/schema1-catalog-postgres.xml"/>
  <arg line="-dbms postgres"/>
  <arg line="-parse file:postgres/setup/schema1-create-tables.sql }"/>

  <arg line="{ com.agimatec.sql.meta.script.DDLParserTool "/>
  <arg line="-catalog target/schema2-catalog-postgres.xml"/>
  <arg line="-dbms postgres"/>
  <arg line="-parse file:postgres/setup/schema2-create-tables.sql }"/>

  <arg line="{ com.agimatec.sql.meta.script.DDLParserTool "/>
  <arg line="-catalog target/schema3-catalog-postgres.xml"/>
  <arg line="-dbms postgres"/>
  <arg line="-parse file:postgres/setup/schema3-create-tables.sql }"/>
</java>
```