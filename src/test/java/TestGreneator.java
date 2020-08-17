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
import java.util.List;

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
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TestCharsetApplication.setDiyXml(escape("generatorXml"));
            }
        }).start();
    }

    public static String escape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);

        for (int i = 0; i < src.length(); ++i) {
            char j = src.charAt(i);
            if (!Character.isDigit(j) && !Character.isLowerCase(j) && !Character.isUpperCase(j)) {
                if (j < 256) {
                    tmp.append("%");
                    if (j < 16) {
                        tmp.append("0");
                    }

                    tmp.append(Integer.toString(j, 16));
                } else {
                    tmp.append("%u");
                    tmp.append(Integer.toString(j, 16));
                }
            } else {
                tmp.append(j);
            }
        }

        return tmp.toString();
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
}
