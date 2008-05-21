package com.agimatec.dbtransform;

import com.agimatec.commons.generator.GeneratorSettings;
import com.agimatec.commons.generator.GeneratorTool;
import com.agimatec.dbtransform.ejb3.Ejb3Schema;
import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * Description: tool to transform a catalog description from one dbms to another.<br/>
 * User: roman.stumm <br/>
 * Date: 05.06.2007 <br/>
 * Time: 15:25:13 <br/>
 * Copyright: Agimatec GmbH
 */
public class CatalogGeneratorTool extends GeneratorTool {
    protected static final Log log = LogFactory.getLog(CatalogGeneratorTool.class);

    private CatalogConversion config;

    @Override
    protected CatalogConversion getConfig() {
        return config;
    }

    @Override
    protected void readConfig(File configFile)
            throws IOException, ClassNotFoundException {
        if (configFile != null) {
            config = (CatalogConversion) new XStreamPersistencer().load(configFile);
        }
    }

    @Override
    protected String getDestFile(String templateBaseName) {
        return ((CatalogGeneratorSettings) settings).getOutputPrefix() +
                templateBaseName + "-" + dbms +
                ((CatalogGeneratorSettings) settings).getOutputSuffix();
    }

    protected File getCatalogFile(String destDir, String templateBaseName) {
        return new File(destDir + "/" + templateBaseName + "-" + dbms + ".xml");
    }

    public static void main(String[] args) throws Exception {
        CatalogGeneratorTool tool = new CatalogGeneratorTool();
        tool.runMain(args, new CatalogGeneratorSettings());
    }

    @Override
    public void initialize(GeneratorSettings gsettings) throws IOException {
        CatalogGeneratorSettings settings = (CatalogGeneratorSettings) gsettings;
        super.initialize(settings);
        CatalogDescription catalog = settings.getCatalog();
        if (config !=
                null) {  // when config == null, do not transform catalog, just generate files with template
            log.info("Performing transformation: " + config.getName());
            catalog = config.transformCatalog(catalog);
            templateEngine.putModel("catalog", catalog);
            templateEngine.putModel("ejb3schema", new Ejb3Schema(catalog));
            if (settings.getCatalogFile() != null) {
                log.info("Saving transformed catalog to " + settings.getCatalogFile());
                File f = new File(settings.getCatalogFile()).getParentFile();
                if (f != null) f.mkdirs();
                new XStreamPersistencer()
                        .save(catalog, new File(settings.getCatalogFile()));
            }
        } else {
            templateEngine.putModel("catalog", catalog);
            templateEngine.putModel("ejb3schema", new Ejb3Schema(catalog));
            log.info("Skipped transformation, it has been disabled!");
        }
    }


}
