import com.wisea.cloud.idea.wbfceditor.generator.WbfcGenerator;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;

public class XMLGenTest {
    public static void main(String[] args) {
        File newGenerator = new File("C:\\Users\\Lion\\Desktop\\xml.txt");
        WbfcGenerator wbfcGenerator = new WbfcGenerator();
        WbfcConfig wbfcConfig = new WbfcConfig();
        wbfcConfig.setTotalPath("D:/worksIdeaProjects/TestPlugin\\src\\main\\java\\sc");
        wbfcConfig.setXmlPath("D:/worksIdeaProjects/TestPlugin\\src\\main\\resources");
        wbfcConfig.setControllerPackage("com.wisea.szdk.entity.controller");
        wbfcConfig.setServicePackage("com.wisea.szdk.entity.service");
        wbfcConfig.setEntityPackage("com.wisea.szdk.entity.entity");
        wbfcConfig.setPoPackage("com.wisea.szdk.entity.po");
        wbfcConfig.setVoPackage("com.wisea.szdk.entity.vo");
        wbfcConfig.setDaoPackage("com.wisea.szdk.entity.mapper");
        wbfcConfig.setXmlPackage("mappings");
        wbfcConfig.setSimplePoVo("false");
        GeneratorUtil.setWbfcConfig(wbfcConfig);
        ConfigurationParser cp = new ConfigurationParser(null);
        Configuration config = null;
        try {
            config = cp.parseConfiguration(newGenerator);

            DefaultShellCallback callback = new DefaultShellCallback(true);

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, null);
            myBatisGenerator.generate(null);
            System.out.println("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
