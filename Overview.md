You can find the latest version on maven-central:
<a href='http://search.maven.org/#search|ga|1|g%3A%22de.viaboxx%22'>here</a>

# Introduction #

agimatec-tools contains 3 frameworks, more or less independent of each other:

  * [DBMigrate](dbmigrate.md), if you need a framework for database migration or other tools contained in dbmigrate (e.g. sql script execution, schema transformation, code generation sql->java) (see [Why use dbmigrate and who can use it](DBMigrateWhy.md))

  * [DBImport](dbimport.md), if you need to import data from excel, csv, fixed-length, xml files/streams

  * [Annomark](annomark.md), if you want to generate source code or other files (properties, xml) driven by annotations

and - on a separate side -
  * [NLSTools](NLSTools.md), if you want to manage resource bundles with XML/excel, generate properties for Java and Adobe Flex, SQL, merge etc.

You can
  * use dbimport without any other agimatec framework.
  * use dbmigrate (and optionally dbimport from within some of your scripts)
  * use annomark, but this requires dbmigrate in the classpath as well (because annomark uses some common-utility classes from dbmigrate)


<img src='http://agimatec-tools.googlecode.com/svn/wiki/Schema.png' />

## What's new? ##

  * ReleaseNotes

# Building the project with Maven2 #
[How to build agimatec-tools](BuildAgimatecTools.md)

![http://agimatec-tools.googlecode.com/svn/wiki/agimatec-tools-small.jpg](http://agimatec-tools.googlecode.com/svn/wiki/agimatec-tools-small.jpg)