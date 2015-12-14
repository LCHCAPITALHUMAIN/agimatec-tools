The tool can execute files with suffix .groovy as  [groovy scripts](http://groovy.codehaus.org/).

It does not make any difference whether you invoke a groovy script from within a xml configuration or directly by placing it in the scripts directory (prefix up-) for automatic invocation, except that you cannot provide additional parameters when invoking the script automatically.

Important hints:
Groovy scripts are compiled to classes as well. The naming of the scripts might be important:
  * do not prefix them with "up-", because script name must no contain "-".
> > Solution: configure "Scripts-Prefix" = "up_"
  * use "_" in the version format instead of ".", because "." indicates a package path in the script name, which might lead to surprising side effects
> > Solution: name your scripts in format "up\_1\_0\_0\_myscript.groovy" instead "up-1.0.0\_myscript.groovy"

# Features #

[Find more information here (refer to doGroovyScript)](DBMigrateXmlScript.md)