package com.agimatec.dbmigrate;

import com.agimatec.commons.config.Config;
import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.FileNode;
import com.agimatec.commons.util.ResourceUtils;
import com.agimatec.dbmigrate.action.ChangeDirCommand;
import com.agimatec.dbmigrate.action.MigrateAction;
import com.agimatec.dbmigrate.action.OperationAction;
import com.agimatec.dbmigrate.action.ScriptAction;
import com.agimatec.dbmigrate.util.SQLCursor;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Automatical database migration program for Agimatec GmbH
 * <pre>
 * Features:
 * 01- detect from-version from database or config-file
 * 02- execute sql-scripts or commands from a config file for
 *     all files that belong to versions later than "from-version".
 * 03- automatic script detection and sorting.
 * 04- can stop execution at "to-version", if set in config-file.
 * 05- supports conditional execution in sql-scripts (-- #if #endif syntax)
 * 06- support conditional execution in config-files (list-tags)
 * 07- supports enviroment variables in sql-scripts ( ${variable} syntax )
 * 08- supports connect commands in SQL-scripts
 * 09- a) supports local-enviroment variables per config-file of each migration version,
 *     b) environment variables per migration config,
 *     c) all JVM System-Properties are accessible as environment variable default
 * 10- supports Simulation-mode "with jvm parameter -Dsim=true" to check
 *     behavior before affecting the system.
 * 11- supports subscripts (@scriptname.sql; syntax)
 * 12 - supports db_version upgrade with setVersion(dbversion)-method
 *      or -- @version(dbversion) script-directive
 * 13 - runs all scripts as files or as classpath resource
 * 14 - (optional) automatically create db_version table
 * 15 - (optional) automatically set version in db_version table after script execution
 * 16 - can be integrated into grails project (see plugin viaboxx-dbmigrate)
 * 17 - runs sql, xml or groovy script (auto-detect by file suffix)
 * </pre>
 * Author: Roman Stumm
 * Date: 2007, 2008, 2009, 2010
 * <pre>
 * final String sim = System.getProperty(SYSTEM_PROPERTY_SIM);
 * sim = "true"|"yes" :: simlation, echo execution sequence into log, but do not invoke any script
 * otherwise (=default) :: execute scripts/java in sequence
 * </pre>
 */
public class AutoMigrationTool extends BaseMigrationTool {
  // the local environment entries of a automatically found .xml config
  private Map localEnv = null;
  private boolean sim;
  private List<MigrateAction> actionOverride;

  public AutoMigrationTool() {
    super();
  }

  /**
   * run the tool and exit the JVM afterwards.
   *
   * @throws Exception exit(0) = successful
   *                   exit(1) = in case of an exception
   */
  public static void main(String[] args) {
    AutoMigrationTool tool = new AutoMigrationTool();
    try {
      if (!tool.parseArgs(args)) return;
      try {
        tool.setUp();
        tool.startAutomaticMigration();
      } finally {
        tool.tearDown();
      }
      System.exit(0);
    } catch (Throwable ex) {
      log.fatal(null, ex);
      System.exit(1);
    }
  }

  public boolean isSim() {
    return sim;
  }

  private boolean parseArgs(String[] args) {

    String sim = "false";
    for (int i = 0; i < args.length; i++) {
      String each = args[i];
      if ("-sim".equalsIgnoreCase(each)) {
        i++;    // skip next param
        sim = args[i];
      } else if ("-conf".equalsIgnoreCase(each)) {
        i++;    // skip next param
        setMigrateConfigFileName(args[i]);
      } else if ("-help".equalsIgnoreCase(each)) {
        printUsage();
        return false;
      } else if ("-script".equalsIgnoreCase(each)) {
        i++;
        ScriptAction action = ScriptAction.create(this, args[i]);
        if (action != null) {
          addActionOverride(action);
        }
      } else if ("-op".equalsIgnoreCase(each)) {
        String op = args[++i];
        String param = "";
        i++;
        if (i < args.length) {
          param = args[i];
        }
        addActionOverride(new OperationAction(this, op, param));
      }
    }
    this.sim = "true".equalsIgnoreCase(sim) || "yes".equalsIgnoreCase(sim);
    return true;
  }

  private void addActionOverride(MigrateAction operationAction) {
    if (actionOverride == null) actionOverride = new LinkedList();
    actionOverride.add(operationAction);
  }

  private void printUsage() {
    System.out.println("usage: java " + getClass().getName() +
        " -sim false -conf migration.xml -script aScript -op operationName operationParameter ");
    System.out.println("Options:\n\t-help \t (optional) print this help");
    System.out.println(
        "\t-sim \t (optional) true|yes=simulation only, default is false");
    System.out.println(
        "\t-conf \t (optional) name of migration.xml configuration file, default is migration.xml");
    System.out.println(
        "\t-script \t (optional, multiple occurrence supported) name of a upgrade-file (sql, groovy, xml) with operations. tool will execute the given file(s) only!");
    System.out.println(
        "\t-op \t (optional, multiple occurrence supported) the operation in the same syntax as in an upgrade-file. tool will execute the given operation(s) only!");
  }

  // overwritten to provide the enviroment (or local env) to the script executor

  public Map getEnvironment() {
    if (localEnv == null) {
      return super.getEnvironment();
    } else {
      return localEnv;
    }
  }

  protected Map getMigrateEnvironment() {
    return super.getEnvironment();
  }

  public void startAutomaticMigration() throws Exception {
    log("----------------- start migration -----------------");
    connectTargetDatabase();
    if (actionOverride != null && !actionOverride.isEmpty()) {
      print("PERFORMING COMMAND LINE ACTIONS ONLY!");
      performActions(actionOverride);
    } else {
      performActions(createActions());
    }
  }

  public void performActions(List<MigrateAction> actionOverride) throws Exception {
    if (sim) print("SIMULATION ONLY - SEQUENCE FOLLOWS:");
    try {
      if (actionOverride.isEmpty()) {
        print("THERE ARE NO ACTIONS TO PERFORM.");
      } else {
        int i = 0;
        for (MigrateAction each : actionOverride) {
          i++;
          print("ACTION " + i + " (of " + actionOverride.size() + ") = " +
              each.getInfo());
          each.doIt();
        }
      }
    } catch (Exception ex) {
      rollback();
      log(ex);
      throw ex;
    }
  }

  protected void prepareLocalEnvironment(Config cfg) {
    Map tempEnv = cfg.getMap("env");
    if (tempEnv != null) { // merge env
      localEnv = new HashMap(getMigrateEnvironment());
      localEnv.putAll(tempEnv);
      replaceProperties(localEnv);
    }
  }

  public void doXmlScript(String filePath) throws Exception {
    if (!sim) {
      Config cfg = ConfigManager.getDefault()
          .readConfig(filePath, false);
      try {
        prepareLocalEnvironment(cfg);
        perform(cfg.getList("Operations"));
      } finally {
        localEnv =
            null;    // remove localEnv after exec. of config
      }
    }
  }

  public DBVersionString getToVersion() {
    String ver = getMigrateConfig().getString("to-version");
    if (ver == null || ver.length() == 0) {
      return null;
    } else {
      print("Using to-version: " + ver);
      return DBVersionString.fromString(ver);
    }
  }

  public DBVersionString getFromVersion() throws SQLException {
    String ver = getMigrateConfig().getString("from-version");
    DBVersionString version;
    if (ver == null || ver.length() == 0) {
      version = readVersion();
      print("Current database version: " + version);
    } else {
      version = DBVersionString.fromString(ver);
      print("Using from-version: " + version);
    }
    return version;
  }

  /**
   * remove all entries from the list that are not relevant
   * for direct execution from the given dbVersion
   *
   * @param version - the current database version
   */
  private List<DBVersionString> filterVersions(DBVersionString version,
                                               List<DBVersionString> versionFiles) {
    Iterator<DBVersionString> iter = versionFiles.iterator();
    DBVersionString toversion = getToVersion();
    while (iter.hasNext()) {
      DBVersionString each = iter.next();
      if (!each.isLater(version)) {
        iter.remove();
      } else if (toversion != null && each.isLater(toversion)) {
        iter.remove();
      }
    }
    return versionFiles;
  }

  /**
   * read the version from the database
   *
   * @throws SQLException
   */
  public DBVersionString readVersion() throws SQLException {
    String version = null;
    try {
      SQLCursor rs = sqlSelect(getDbVersionMeta().toSQLSelectVersion());
      try {
        while (rs.next()) {
          version = rs.getString(1);
        }
      } finally {
        rs.close();
      }
    } catch (SQLException ex) { // we assume: no table DB_VERSION in database
      log.warn("cannot read " + getDbVersionMeta().getQualifiedVersionColumn() + " because " + ex.getMessage());
    }
    return version == null ? null : DBVersionString.fromString(version);
  }

  private List<MigrateAction> createActions()
      throws SQLException, IOException {
    String upDir = getScriptsDir();
    List<DBVersionString> files =
        filterVersions(getFromVersion(), readDir(getScriptPrefix(), upDir));
    List<MigrateAction> actions;
    String beforeDir = getBeforeAllScriptsDir();
    if (beforeDir != null) {
      List<DBVersionString> before = readDir(null, beforeDir);
      actions = createActions(before);
      actions.add(0, new ChangeDirCommand(this, beforeDir));
      if (upDir != null || !files.isEmpty()) {
        actions.add(new ChangeDirCommand(this, upDir));
        actions.addAll(createActions(files, getDbVersionMeta().isAutoVersion()));
      }
    } else {
      actions = createActions(files);
    }
    addActionsAfterAll(getAfterAllScriptsDir(), actions);
    return actions;
  }

  public void addActionsAfterAll(String dir, List<MigrateAction> actions) throws IOException {
    if (dir != null) {
      actions.add(new ChangeDirCommand(this, dir));
      actions.addAll(createActions(readDir(null, dir)));
    }
  }

  /**
   * create some up- actions of a custom script dir dependent on current version
   * @param scriptDir
   * @param enableAutoVersion
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public List<MigrateAction> createUpgradeActions(String scriptDir, boolean enableAutoVersion)
      throws SQLException, IOException {
    List<DBVersionString> files =
        filterVersions(getFromVersion(), readDir(getScriptPrefix(), scriptDir));
    List<MigrateAction> actions = new ArrayList();
    if (scriptDir != null || !files.isEmpty()) {
      actions.add(new ChangeDirCommand(this, scriptDir));
      actions.addAll(createActions(files, enableAutoVersion && getDbVersionMeta().isAutoVersion()));
    }
    return actions;
  }

  public String getScriptPrefix() {
    String prefix = getMigrateConfig().getString("Scripts-Prefix");
    if (prefix == null) {
      return "up-";
    } else {
      return prefix;
    }
  }

  private List<MigrateAction> createActions(List<DBVersionString> files, boolean autoVersion) {
    List<MigrateAction> actions = new LinkedList();
    for (DBVersionString file : files) {
      ScriptAction action =
          ScriptAction.create(this, file.getFileName(), file.getFileType());
      if (action != null) {
        actions.add(action);
        if (autoVersion) {
          actions.add(new OperationAction(this, "version", file.getVersion()));
        }
      }
    }
    return actions;
  }

  private List<MigrateAction> createActions(List<DBVersionString> files) {
    return createActions(files, false);
  }

  /**
   * read possible scripts and configs
   *
   * @return them in a sorted order, sorted by execution sequence
   */
  private List<DBVersionString> readDir(String prefix, String directory)
      throws IOException {
    if (directory == null) return new ArrayList();
    Collection<String> resources = readResources(directory);
    List<DBVersionString> order = new ArrayList<DBVersionString>(resources.size());
    for (String each : resources) {
      DBVersionString ver = DBVersionString.fromString(prefix, each);
      if (ver != null) order.add(ver);
    }
    Collections.sort(order);
    return order;
  }

  private Collection<String> readResources(String directory) throws IOException {
    Collection<String> resources = new ArrayList();
    for (URL each : ConfigManager.toURLs(directory)) {
      resources.addAll(ResourceUtils.readLines(each));
    }
    return resources;
  }

  public String getBeforeAllScriptsDir() {
    FileNode dir = (FileNode) getMigrateConfig().get("Scripts-Before-All");
    return (dir == null) ? null : dir.getFilePath();
  }

  public String getAfterAllScriptsDir() {
    FileNode dir = (FileNode) getMigrateConfig().get("Scripts-After-All");
    return (dir == null) ? null : dir.getFilePath();
  }

  public Map getLocalEnv() {
    return localEnv;
  }

  public List<MigrateAction> getActionOverride() {
    return actionOverride;
  }

  public void setActionOverride(List<MigrateAction> actionOverride) {
    this.actionOverride = actionOverride;
  }

  public void setSim(boolean sim) {
    this.sim = sim;
  }

  public void setLocalEnv(Map localEnv) {
    this.localEnv = localEnv;
  }
}