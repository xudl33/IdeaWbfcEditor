import com.wisea.cloud.common.util.FtlManagerUtil;
import com.wisea.cloud.idea.wbfceditor.generator.TestGreneator;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;

import java.io.File;

public class FtlTest {
    public static void main(String[] args) {
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

        conf.setDbDriver("com.mysql");
        conf.setDbUrl("jdbc:url:mysql");
        conf.setDbUser("dbuser");
        conf.setDbPassword("123456");

        FtlManagerUtil.ftlPath = "/templates/";
        FtlManagerUtil.createTotal(new File("test.xml"), conf, "generatorConfig.ftl");
    }
}
