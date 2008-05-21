package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 09:43:56 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class BundleWriter {
    protected final Task task;
    protected final MBBundle currentBundle;
    protected final String configFile;
    protected final String outputPath;
    protected final FileType fileType;
    protected boolean overwrite = false, deleteOldFiles = true;
    private boolean debugMode;

    enum FileType {
        NO, XML, PROPERTIES,
        JS, JS_PRETTY,
        JAVA_FULL_ENUM_KEYS, JAVA_ENUM_KEYS, JAVA_FULL, JAVA_SMALL, 
        SQL
    }

    protected List myUsedLocales;


    public BundleWriter(Task task, String configFile, MBBundle currentBundle, String outputPath, FileType fileType) {
        this.task = task;
        this.configFile = configFile;
        this.currentBundle = currentBundle;
        this.outputPath = outputPath;
        this.fileType = fileType;
    }

    public void execute() throws Exception {
        if (!fileType.equals(FileType.NO)) {
            if (!overwrite && !needsNewFiles()) {
                task.log(suffix() + " file(s) for " + getCurrentBundle().getBaseName() +
                        " up to date", Project.MSG_VERBOSE);
            } else {
                writeOutputFiles();
            }
        }
    }


    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDeleteOldFiles(boolean deleteOldFiles) {
        this.deleteOldFiles = deleteOldFiles;
    }

    /**
     * generate the current bundle's property files
     */
    protected void writeOutputFiles() throws Exception {
        deleteFiles();
        Iterator locales = getLocalesUsed().iterator();
        new File(getOutputPathName()).mkdirs();
        while (locales.hasNext()) {
            String locale = (String) locales.next();
            writeOutputFilePerLocale(locale);
        }
    }

    protected void writeOutputFilePerLocale(String locale) throws Exception {
        // do nothing
    }

    protected void deleteFiles() {
        if (deleteOldFiles) {
            // try to delete only if directory exists
            StringBuilder baseName = buildOutputFileNameBase();
            File dir = new File(getOutputPath());
            if (dir.exists()) {
                if (fileType.equals(FileType.XML)) {
                    deleteFiles(baseName.append("*.xml").toString());
                } else if (fileType.equals(FileType.PROPERTIES)) {
                    deleteFiles(baseName.append("*.properties").toString());
                } else if (fileType.equals(FileType.JS)) {
                    deleteFiles(baseName.append("*.js").toString());
                }
            }
        }
    }

    protected  StringBuilder buildOutputFileNameBase() {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getOutputPath());
        fileName.append("/");
        fileName.append(getCurrentBundle().getBaseName());
        return fileName;
    }

    protected  void deleteFiles(String filePattern) {
        Delete delete = (Delete) task.getProject().createTask("delete");
        FileSet fs = new FileSet();
        File file = new File(filePattern);
        fs.setDir(file.getParentFile());
        fs.setIncludes(file.getName());
        fs.setProject(task.getProject());
        delete.addFileset(fs);
        delete.execute();
    }

    /**
     * true when generation is neccessary, false when up-to-date
     */
    protected boolean needsNewFiles() throws FileNotFoundException {
        Iterator locales = getLocalesUsed().iterator();
        boolean result = false;
        while (locales.hasNext()) {
            String each = (String) locales.next();
            File outfile = new File(getFileName(each));
            if (!outfile.exists()) {
                result = true;
            } else {
                File infile = new File(configFile);
                if (!infile.exists()) {
                    throw new FileNotFoundException(infile + " not found");
                }
                if (infile.lastModified() > outfile.lastModified()) {
                    result = true;
                }
            }
            if (result) {
                task.log(outfile.getPath() + " is outdated!", Project.MSG_DEBUG);
                break;
            }
        }
        return result;
    }

    protected String getOutputPathName() {
        String result = buildOutputFileNameBase().toString();
        return result.substring(0, result.lastIndexOf('/'));
    }

    protected String getFileName(String locale) {
        StringBuilder fileName = buildOutputFileNameBase();
        if (locale != null && locale.length() > 0) {
            fileName.append("_");
            fileName.append(locale);
        }
        fileName.append(suffix());
        return fileName.toString();
    }

    protected abstract String suffix();

    /**
     * the root path to store the files
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * @return a list of String with the locales used in the current bundle
     */
    protected  List getLocalesUsed() {
        if (myUsedLocales == null) {
            HashSet locales = new HashSet();
            for (MBEntry eachEntry : getCurrentBundle().getEntries()) {
                for (MBText eachText : eachEntry.getTexts()) {
                    locales.add(eachText.getLocale());
                }
            }
            List result = new ArrayList(locales);
            Collections.sort(result);
            myUsedLocales = result;
        }
        return myUsedLocales;
    }

    protected MBBundle getCurrentBundle() {
        return currentBundle;
    }

    protected Properties createProperties(String aLocale) {
        Iterator<MBEntry> entries = getCurrentBundle().getEntries().iterator();
        Properties p = new Properties();

        while (entries.hasNext()) {
            MBEntry eachEntry = entries.next();
            String key = eachEntry.getKey();
            MBText langText = eachEntry.getText(aLocale);
            if (langText == null && aLocale != null && !"".equals(aLocale)) {
                langText = eachEntry.getText("");
            }
            if (langText != null) {
                String value;

                // in debug mode the keys are also displayed as labels
                if (!debugMode) {
                    value = langText.getValue();
                }
                else {
                    value = key;
                }
                // Continue text at line breaks followed by whitespaces (indentations due to code formatter etc.)
                int indentIndex;
                while (value != null && (indentIndex = value.indexOf("\n ")) > -1) {
                    int lastBlankIndex = indentIndex + 1;
                    while (lastBlankIndex + 1 < value.length() && Character
                            .isWhitespace(value.charAt(lastBlankIndex + 1))) {
                        lastBlankIndex++;
                    }
                    value = value.substring(0, indentIndex) + ' ' +
                            value.substring(lastBlankIndex + 1);
                }
                task.log("'" + key + "' ==> '" + value + "'", Project.MSG_DEBUG);
                if (key != null && value != null) {
                    p.setProperty(key, value);
                }
            }
        }
        return p;
    }
}
