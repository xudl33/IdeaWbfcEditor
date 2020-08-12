import com.google.common.collect.Lists;
import com.wisea.cloud.common.mybatis.generator.MybatisGeneratorTables;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.FtlManagerUtil;
import com.wisea.cloud.common.util.IOUtils;
import com.wisea.cloud.idea.wbfceditor.generator.WbfcGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorRunner;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class GeneratorTest {
    public static void main(String[] args) throws InvalidConfigurationException, InterruptedException, SQLException, IOException, XMLParserException {
        WbfcGenerator wbfcGenerator = new WbfcGenerator();
        WbfcConfig wbfcConfig = new WbfcConfig();
        URL url = null;
        try {
            url = new File(GeneratorTest.class.getResource("/config/log4j.properties").toURI()).toURI().toURL();
            wbfcConfig.setLogProperties(url.toString().replace("file:/", "file:///"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        wbfcConfig.setClassPathEntry("D:/worksIdeaProjects/WbfcEditor/build/idea-sandbox/config/jdbc-drivers/MySQL ConnectorJ/8.0.21/mysql-connector-java-8.0.21.jar");
        wbfcConfig.setDbUrl("jdbc:mysql://192.168.1.150:3306/qdcytest2");
        wbfcConfig.setDbDriver("com.mysql.cj.jdbc.Driver");
        wbfcConfig.setDbUser("dbuser");
        wbfcConfig.setDbPassword("dbuser123@");
        wbfcConfig.setTotalPath("D:\\worksIdeaProjects\\TestPlugin\\src\\main\\java");
        wbfcConfig.setXmlPath("D:\\worksIdeaProjects\\TestPlugin\\src\\main\\resources");
        wbfcConfig.setControllerPackage("com.wisea.qdcy.standards.controller");
        wbfcConfig.setServicePackage("com.wisea.qdcy.standards.service");
        wbfcConfig.setEntityPackage("com.wisea.qdcy.standards.entity");
        wbfcConfig.setPoPackage("com.wisea.qdcy.standards.po");
        wbfcConfig.setVoPackage("com.wisea.qdcy.standards.vo");
        wbfcConfig.setDaoPackage("com.wisea.qdcy.standards.mapper");
        wbfcConfig.setXmlPackage("mappings");
        wbfcConfig.setSimplePoVo("false");
        List<WbfcDataTable> tableList = Lists.newLinkedList();
        WbfcDataTable table = new WbfcDataTable("standard_type_mage", "StandardTypeMage");
        table.setBatchInsert("true");
        table.setBatchUpdate("true");
        table.setBatchDelete("true");
        tableList.add(table);
        wbfcConfig.setTablesCloumnList(tableList);
        GeneratorUtil.setWbfcEditorGenerator(wbfcGenerator);
        GeneratorUtil.setWbfcConfig(wbfcConfig);
        // 设置列
        List<String> tableStrList = Lists.newArrayList();
        for (WbfcDataTable dTable : wbfcConfig.getTablesCloumnList()) {
            List<WbfcDataColumn> colList = dTable.getColumns();
            // 在页面中自定义的列属性
            if (colList.size() > 0) {
                String detailTemplete = GeneratorUtil.getColumnOverrideTempletes(colList);
                tableStrList.add(MybatisGeneratorTables.getTableDom(dTable.getTableName(), dTable.getEntityName(), detailTemplete, false, false, false, false));
            } else {
                // 默认的列属性
                List<TableColumn> tableColumnList = GeneratorUtil.getWbfcEditorGenerator().getTableColumn(dTable.getTableName());
                String detailTemplete = GeneratorUtil.getDetailTemple(tableColumnList);
                tableStrList.add(MybatisGeneratorTables.getTableDom(dTable.getTableName(), dTable.getEntityName(), detailTemplete, false, false, false, false));
            }
        }
        wbfcConfig.setTablesList(tableStrList);
        File newGenerator = new File("mybatisGenerator.xml");
        IOUtils.createFileParents(newGenerator);
        try {
            if (newGenerator.exists()) {
                newGenerator.delete();
            }
            FtlManagerUtil.ftlPath = "/templates/";
            FtlManagerUtil.createTotal(newGenerator, wbfcConfig, "generatorConfig.ftl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 生成各种代码文件
        List<String> warnings = Lists.newArrayList();
        boolean overwrite = true;
        Properties extraProperties = new Properties();
//        String confFilePath = "";
//        try {
//            confFilePath = new File(GeneratorTest.class.getResource("/config/log4j.properties").toURI()).getAbsolutePath();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

//        extraProperties.setProperty("java.util.logging.config.file", "D:\\worksIdeaProjects\\WbfcEditor\\src\\main\\resources\\config\\logging.properties");
/*        extraProperties.setProperty("log4j.rootLogger", "INFO, ConsoleLogger");
        extraProperties.setProperty("log4j.appender.ConsoleLogger", "org.apache.log4j.ConsoleAppender");
        extraProperties.setProperty("log4j.appender.ConsoleLogger.layout", "org.apache.log4j.PatternLayout");
        extraProperties.setProperty("log4j.appender.ConsoleLogger.ConversionPattern", "%-4r %-5p %c - %m%n");
        extraProperties.setProperty("log4j.logger.org.mybatis.generator", "DEBUG");*/

        ConfigurationParser cp = new ConfigurationParser(extraProperties, warnings);
//        Configuration config = cp.parseConfiguration(new File("D:\\maven_repository\\.m2\\repository\\caches\\modules-2\\files-2.1\\com.jetbrains.intellij.idea\\ideaIU\\2020.1.3\\6d8aedc3acbb649a85115137d9da55ac9140dc65\\ideaIU-2020.1.3\\bin\\WbfcEditorTempFiles\\mybatisGenerator.xml"));
        Configuration config = cp.parseConfiguration(newGenerator);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        System.out.println("success");
    }
}
