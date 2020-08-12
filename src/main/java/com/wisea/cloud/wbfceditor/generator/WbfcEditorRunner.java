package com.wisea.cloud.wbfceditor.generator;

import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import com.wisea.cloud.common.exception.VerifyException;
import com.wisea.cloud.common.mybatis.generator.MybatisGeneratorTables;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.*;
import com.wisea.cloud.idea.wbfceditor.setting.WbfcEditorPersistentState;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcColunmOverrideProperty;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.logger.WbfcEditorLogger;
import com.wisea.cloud.wbfceditor.generator.logger.WbfcLogFactory;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.apache.commons.io.Charsets;
import org.apache.log4j.Level;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;


import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class WbfcEditorRunner {
//    private static final Logger logger = LoggerFactory.getLogger(WbfcEditorRunner.class);


    public static void generatorFiles() {
        LogFactory.setLogFactory(new WbfcLogFactory());
        Logger logger = (Logger) LogFactory.getLog(WbfcEditorRunner.class);
        logger.debug("Start WbfcGenerator... at " + ConverterUtil.dateToString(new Date(), ConverterUtil.FORMATE_DATE_TIME_24H));
        // 校验并生成配置
        WbfcConfig wbfcConfig = GeneratorUtil.beforeGenMakeConfig();

        // 在插件配置目录生成临时文件
        Project project = WbfcFxApplication.getProject();
        String path = GeneratorUtil.getWbfcConfigPath();
        File newGenerator = new File(path + File.separator + "mybatisGenerator.xml");
        IOUtils.createFileParents(newGenerator);
        try {

            /*List<String> lines = IOUtils.readLines(in, Charsets.toCharset("UTF-8"));*/
            // wbfcConfig.setLogProperties("log4j.properties");
            if (newGenerator.exists()) {
                newGenerator.delete();
            }
            if (ConverterUtil.toBoolean(wbfcConfig.getHasXml()) && ConverterUtil.isNotEmpty(wbfcConfig.getDiyXml())) {
                FtlManagerUtil.createFileWithStr(newGenerator, wbfcConfig, wbfcConfig.getDiyXml());
            } else {
                FtlManagerUtil.ftlPath = "/templates/";
                FtlManagerUtil.createTotal(newGenerator, wbfcConfig, "generatorConfig.ftl");
            }

            // 生成各种代码文件
            List<String> warnings = Lists.newArrayList();
            boolean overwrite = true;
            Properties extraProperties = new Properties();
            extraProperties.setProperty("verbose", "true");

            ConfigurationParser cp = new ConfigurationParser(extraProperties, warnings);

            Configuration config = cp.parseConfiguration(newGenerator);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            WbfcProgressCallback progressCallback = new WbfcProgressCallback(logger, true);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            //myBatisGenerator.generate(null);
            myBatisGenerator.generate(progressCallback);

        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error("exception with WbfcEditorRunner.generatorFiles " + Exceptions.getStackTraceAsString(e1));
            throw new RuntimeException(e1);
        } finally {
            newGenerator.delete();
            logger.debug("End WbfcGenerator... at " + ConverterUtil.dateToString(new Date(), ConverterUtil.FORMATE_DATE_TIME_24H));
        }
        // 删除临时文件
        // if (null != newGenerator) {
        // newGenerator.delete();
        // }
    }
}
