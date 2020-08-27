import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class TestGreneator {
    private Log logger = LogFactory.getLog(getClass());

    //    private Logger logger  = LoggerFactory.getLogger(getClass());
    public String getProjectConfig() {
        WbfcConfig conf = new WbfcConfig();

        conf.setControllerPath(new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "java");
        String projectPackage = "com.wisea.qdcy";
        conf.setControllerPackage(projectPackage + ".controller");

        conf.setServicePath(conf.getControllerPath());
        conf.setServicePackage(projectPackage + ".service");

        conf.setEntityPath(conf.getControllerPath());
        conf.setEntityPackage(projectPackage + ".entity");

        conf.setPoPath(conf.getControllerPath());
        conf.setPoPackage(projectPackage + ".po");

        conf.setVoPath(conf.getControllerPath());
        conf.setVoPackage(projectPackage + ".vo");

        conf.setDaoPath(conf.getControllerPath());
        conf.setDaoPackage(projectPackage + ".mapper");

        conf.setXmlPath(new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources");
        conf.setXmlPackage("mappings");
        conf.setHasXml("true");
        logger.debug("logger: getProjectConfig is end");
        System.out.println("sysout: getProjectConfig is end");
        return new Gson().toJson(conf);
    }

    public void resetConfig() {
    }

    public String getTables() {
        List<WbfcDataTable> res = Lists.newArrayList();
        res.add(new WbfcDataTable("sys_dict", "SysDict"));
        res.add(new WbfcDataTable("sys_system", "SysSystem"));
        return new Gson().toJson(res);
    }

    public String getTableColumnOptions(String tableName) {
        List<String> res = Lists.newArrayList();
        res.add("id");
        res.add("name");
        res.add("create_date");
        res.add("update_date");
        logger.debug("logger: getTableColumnOptions is end");
        System.out.println("sysout: getTableColumnOptions is end");
        return new Gson().toJson(res);
    }

    public String getDefaultColumnList(String tableName) {
/*        List<WbfcDataColumn> res = Lists.newArrayList();
        WbfcDataColumn dc = new WbfcDataColumn();
        dc.setColName("create_date");
        dc.setColProperties("javaType=java.time.OffsetDateTime");
        res.add(dc);
        WbfcDataColumn dc2 = new WbfcDataColumn();
        dc2.setColName("update_date");
        dc2.setColProperties("javaType=java.time.OffsetDateTime");
        res.add(dc2);
        return new Gson().toJson(res);*/
        return "";
    }

    public boolean isAbsDirectory(String path) {
        File tep = new File(path);
        if (tep.exists()) {
            // 必须是目录且是绝对路径
            return tep.isAbsolute() && tep.isDirectory();
        } else {
            // 不存在时 不能用isDirectory  根据是否后后缀 有校验是否为目录
            return tep.isAbsolute() && ConverterUtil.isEmpty(FilenameUtils.getExtension(tep.getName()));
        }
    }

    public void generateCodes() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                TestCharsetApplication.appendLog("Start test generate code ...");
                try {
                    Thread.sleep(5000);
                    TestCharsetApplication.appendLog("generate code testetstetest");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TestCharsetApplication.appendLog("End test generate code ...");
                TestCharsetApplication.setGenerateStatus("success");
            }
        }).start();
    }

    public void saveWbfcConfig(String configStr) {
        System.out.println(configStr);
    }

    public void close() {
        WbfcFxApplication.hide();
    }

    public String checkDbConnection() {
        return "success";
    }

    public void generatorXml() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE generatorConfiguration\n" +
                        "        PUBLIC \"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN\"\n" +
                        "        \"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd\">\n" +
                        "\n" +
                        "<generatorConfiguration>\n" +
                        "    \n" +
                        "\n" +
                        "    <!-- 指定数据连接驱动jar地址 -->\n" +
                        "    <classPathEntry location=\"D:/worksIdeaProjects/WbfcEditor/build/idea-sandbox/config/jdbc-drivers/MySQL ConnectorJ/8.0.21/mysql-connector-java-8.0.21.jar\" />\n" +
                        "\n" +
                        "    <!-- 一个数据库一个context -->\n" +
                        "    <context id=\"MySqlTables\" targetRuntime=\"MyBatis3\">\n" +
                        "        <property name=\"javaFileEncoding\" value=\"UTF-8\"/>\n" +
                        "\n" +
                        "        <!-- Wbfc生成插件 -->\n" +
                        "        <plugin type=\"com.wisea.cloud.wbfceditor.generator.WbfcModelGenerator\">\n" +
                        "            <property name=\"hasPoVo\" value=\"true\"/>\n" +
                        "            <property name=\"hasService\" value=\"true\"/>\n" +
                        "            <property name=\"hasController\" value=\"true\"/>\n" +
                        "            <!-- 精简Po和Vo -->\n" +
                        "            <property name=\"simplePoVo\" value=\"false\"/>\n" +
                        "        </plugin>\n" +
                        "\n" +
                        "        <!-- 注释 -->\n" +
                        "        <commentGenerator type=\"com.wisea.cloud.wbfceditor.generator.WbfcCommentGenerator\">\n" +
                        "            <!-- 是否取消注释 -->\n" +
                        "            <property name=\"suppressAllComments\" value=\"false\"/>\n" +
                        "            <!-- 是否生成注释代时间戳 -->\n" +
                        "            <property name=\"suppressDate\" value=\"true\"/>\n" +
                        "            <!-- 文件编码格式 -->\n" +
                        "            \n" +
                        "        </commentGenerator>\n" +
                        "\n" +
                        "        <!--配置数据库链接 -->\n" +
                        "        <jdbcConnection driverClass=\"com.mysql.cj.jdbc.Driver\"\n" +
                        "                        connectionURL=\"jdbc:mysql://192.168.1.150:3306/qdcytest2\"\n" +
                        "                        userId=\"dbuser\" password=\"dbuser123@\">\n" +
                        "            <property name=\"useUnicode\" value=\"true\"></property>\n" +
                        "            <property name=\"nullCatalogMeansCurrent\" value=\"true\"></property>\n" +
                        "            <property name=\"characterEncoding\" value=\"UTF-8\"></property>\n" +
                        "            <property name=\"zeroDateTimeBehavior\" value=\"convertToNull\"></property>\n" +
                        "        </jdbcConnection>\n" +
                        "\n" +
                        "        <!-- 类型转换 -->\n" +
                        "        <javaTypeResolver>\n" +
                        "            <!-- 是否使用bigDecimal， false可自动转化以下类型（Long, Integer, Short, etc.） -->\n" +
                        "            <property name=\"forceBigDecimals\" value=\"false\"/>\n" +
                        "        </javaTypeResolver>\n" +
                        "\n" +
                        "        <!-- 生成实体类地址 -->\n" +
                        "        <javaModelGenerator targetPackage=\"com.wisea.TestPlugin.entity\"\n" +
                        "                            targetProject=\"D:/worksIdeaProjects/TestPlugin\\src\\main\\java\\sc\">\n" +
                        "            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->\n" +
                        "            <property name=\"enableSubPackages\" value=\"true\"/>\n" +
                        "            <!-- 是否针对string类型的字段在set的时候进行trim调用 -->\n" +
                        "            <property name=\"trimStrings\" value=\"true\"/>\n" +
                        "        </javaModelGenerator>\n" +
                        "\n" +
                        "        <!-- 生成mapxml文件 -->\n" +
                        "        <sqlMapGenerator targetPackage=\"mappings\"\n" +
                        "                         targetProject=\"D:/worksIdeaProjects/TestPlugin\\src\\main\\java\\resources\">\n" +
                        "            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->\n" +
                        "            <property name=\"enableSubPackages\" value=\"true\"/>\n" +
                        "        </sqlMapGenerator>\n" +
                        "\n" +
                        "        <!-- 生成mapxml对应client，也就是接口dao type=\"XMLMAPPER\" -->\n" +
                        "        <javaClientGenerator type=\"com.wisea.cloud.wbfceditor.generator.WbfcMapperGenerator\"\n" +
                        "                             targetPackage=\"com.wisea.TestPlugin.mapper\"\n" +
                        "                             targetProject=\"D:/worksIdeaProjects/TestPlugin\\src\\main\\java\\sc\">\n" +
                        "            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->\n" +
                        "            <property name=\"enableSubPackages\" value=\"true\"/>\n" +
                        "            <!-- 是否有Po和Vo -->\n" +
                        "            <property name=\"hasPoVo\" value=\"true\"/>\n" +
                        "            <!-- 精简Po和Vo -->\n" +
                        "            <property name=\"simplePoVo\" value=\"false\"/>\n" +
                        "            <!-- 文件编码格式 -->\n" +
                        "            \n" +
                        "        </javaClientGenerator>\n" +
                        "\n" +
                        "        <!-- 配置表信息 -->\n" +
                        "        <!-- schema即为数据库名 tableName为对应的数据库表 domainObjectName是要生成的实体类 enable*ByExample是否生成example类 -->\n" +
                        "        <!-- <table tableName=\"sys_user\" domainObjectName=\"SysUser\"\n" +
                        "               enableCountByExample=\"false\" enableSelectByExample=\"false\"\n" +
                        "               enableUpdateByExample=\"false\" enableDeleteByExample=\"false\">\n" +
                        "            <columnOverride column=\"create_date\"\n" +
                        "                            javaType=\"java.time.OffsetDateTime\"/>\n" +
                        "            <columnOverride column=\"update_date\"\n" +
                        "                            javaType=\"java.time.OffsetDateTime\"/>\n" +
                        "        </table> -->\n" +
                        "        \n" +
                        "            <table tableName=\"gen_test\" domainObjectName=\"GenTest\" enableCountByExample=\"false\" enableSelectByExample=\"false\" enableUpdateByExample=\"false\" enableDeleteByExample=\"false\"><columnOverride column=\"create_date\" javaType=\"java.time.OffsetDateTime\"/><columnOverride column=\"update_date\" javaType=\"java.time.OffsetDateTime\"/></table>\n" +
                        "\n" +
                        "        \n" +
                        "    </context>\n" +
                        "</generatorConfiguration>";
                try {
                    // 转码
                    //res = new String(res.getBytes(), Charset.forName(wbfcConfig.getCharset()));
                    TestCharsetApplication.setDiyXml(encodeStr(res, StandardCharsets.UTF_8.name()));
//                WbfcFxApplication.setDiyXml(ConverterUtil.escape(res));
                    //WbfcFxApplication.setDiyXml(encodeStr(res, wbfcConfig.getCharset());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                TestCharsetApplication.setDiyXml(escape("generatorXml"));
//            }
//        }).start();
    }

    /**
     * 转码
     *
     * @param str
     * @param charset
     * @return
     */
    public String encodeStr(String str, String charset) {
        String temp = null;
        try {
            temp = URLEncoder.encode(str, charset);
            // 如果是utf8的 js还有部分符号需要替换
            if (Charset.forName(charset).equals(StandardCharsets.UTF_8)) {
                temp = temp.replaceAll("\\+", "%20")
                        .replaceAll("\\!", "%21")
                        .replaceAll("\\'", "%27")
                        .replaceAll("\\(", "%28")
                        .replaceAll("\\)", "%29")
                        .replaceAll("\\~", "%7E");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public String openChooseFile(String pathName, String pathVal) {
        File exisFir = null;
        if (ConverterUtil.isNotEmpty(pathVal)) {
            exisFir = findExistsFile(pathVal);
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(exisFir);
        directoryChooser.setTitle("请选择");
        File directory = directoryChooser.showDialog(new Stage());
        return directory.getAbsolutePath();
    }

    /**
     * 递归查找返回存在的File对象
     *
     * @param pathVal
     * @return
     */
    public static File findExistsFile(String pathVal) {
        File exVf = null;
        if (null != pathVal && !"".equals(pathVal)) {
            File pathFile = new File(pathVal);
            if (pathFile.exists()) {
                exVf = pathFile;
            } else {
                exVf = findExistsFile(pathFile.getParent());
            }
        }
        return exVf;
    }

    public String getCharsetList() {
        SortedMap<String, Charset> map = Charset.availableCharsets();
        return new Gson().toJson(map.values().stream().filter(f -> f.canEncode()).map(Charset::name).collect(Collectors.toList()));
    }
}
