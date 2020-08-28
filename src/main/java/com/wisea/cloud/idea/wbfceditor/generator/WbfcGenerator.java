package com.wisea.cloud.idea.wbfceditor.generator;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.DataSourceStorage;
import com.intellij.database.dataSource.DatabaseDriver;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.ui.classpath.SimpleClasspathElement;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.common.util.Exceptions;
import com.wisea.cloud.common.util.FtlManagerUtil;
import com.wisea.cloud.common.util.IOUtils;
import com.wisea.cloud.idea.wbfceditor.setting.WbfcEditorPersistentState;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import com.wisea.cloud.idea.wbfceditor.utils.FilePathUtil;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorRunner;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDbInfo;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FilenameUtils;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class WbfcGenerator implements WbfcEditorGenerator {
    private Log logger = LogFactory.getLog(getClass());
    public static Gson gson = new Gson();

    /**
     * 名称驼峰转换
     *
     * @param name
     * @return
     */
    public static String case2Name(String name) {
        if (null != name && !"".equals(name)) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
        }
        return "";
    }

    /**
     * 获取当前项目配置实体
     *
     * @return
     */
    public String getProjectConfig() {
        WbfcConfig conf = GeneratorUtil.getWbfcConfig();
        Project project = WbfcFxApplication.getProject();
        String path = getWbfcPath();
        File tempFile = new File(path + File.separator + "WbfcEditor.tmp");
        if (tempFile.exists()) {
            try {
                String[] serStr = IOUtils.readLines(tempFile);
                String serJson = Arrays.stream(serStr).collect(Collectors.joining());
                WbfcConfig tmpConf = ConverterUtil.gson.fromJson(serJson, WbfcConfig.class);
                if (ConverterUtil.isNotEmpty(tmpConf)) {
                    ConverterUtil.copyProperties(tmpConf, conf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            conf.setControllerPath(FilePathUtil.getSyStemFilePath(project.getBasePath(), File.separator, "src", File.separator, "main", File.separator, "java"));
            String projectPackage = "com.wisea." + project.getName();
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

            conf.setXmlPath(FilePathUtil.getSyStemFilePath(project.getBasePath(), File.separator, "src", File.separator, "main", File.separator, "resources"));
            conf.setXmlPackage("mappings");
        }
        return gson.toJson(conf);
    }

    /**
     * 是否为合法的路径
     *
     * @param path
     * @return
     */
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

    /**
     * 获取选择的表的列表
     *
     * @return
     */
    public String getTables() {
        List<WbfcDataTable> res = Lists.newArrayList();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        for (DbTable dbTable : dbTables) {
            res.add(new WbfcDataTable(dbTable.getName(), case2Name(dbTable.getName())));
        }
        return gson.toJson(res);
    }

    /**
     * 通过数据库表名查询DBTable
     *
     * @param tableName
     * @return
     */
    public DbTable getDbTableByName(String tableName) {
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        Optional<DbTable> opt = dbTables.stream().filter(n -> n.getName().equals(tableName)).findAny();
        DbTable table = null;
        if (opt.isPresent()) table = opt.get();
        return table;
    }

    /**
     * 获取默认转换的配置列
     *
     * @param tableName
     * @return
     */
    public String getDefaultColumnList(String tableName) {
        List<WbfcDataColumn> res = new ArrayList<>();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        Optional<DbTable> opt = dbTables.stream().filter(n -> n.getName().equals(tableName)).findAny();
        DbTable table = getDbTableByName(tableName);

        if (null != table) {
            JBIterable<? extends DasColumn> columnsIter = DasUtil.getColumns(table);
            List<? extends DasColumn> dasColumns = columnsIter.toList();
            for (DasColumn dasColumn : dasColumns) {
                String dataType = dasColumn.getDataType().typeName.toLowerCase();
                if (dataType.contains("date") || dataType.contains("datatime")) {
                    WbfcDataColumn dc = new WbfcDataColumn();
                    dc.setColName(dasColumn.getName());
                    dc.setColProperties("javaType=java.time.OffsetDateTime");
                    res.add(dc);
                }
            }
        }
        return gson.toJson(res);
    }

    /**
     * 获取表的列名列表
     *
     * @param tableName
     * @return
     */
    public String getTableColumnOptions(String tableName) {
        List<String> res = new ArrayList<>();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        Optional<DbTable> opt = dbTables.stream().filter(n -> n.getName().equals(tableName)).findAny();
        DbTable table = getDbTableByName(tableName);

        if (null != table) {
            JBIterable<? extends DasColumn> columnsIter = DasUtil.getColumns(table);
            List<? extends DasColumn> dasColumns = columnsIter.toList();
            for (DasColumn dasColumn : dasColumns) {
                String colName = dasColumn.getName();
                res.add(colName);
            }
        }
        //logger.debug("logger: getTableColumnOptions is end");
        //System.out.println("sysout: getTableColumnOptions is end");
        return gson.toJson(res);
    }

    /**
     * 获取表名
     *
     * @param tableName
     * @return
     */
    @Override
    public String getTableRemarks(String tableName) {
        DbTable table = getDbTableByName(tableName);
        if (null != table) {
            String comment = table.getComment();
            return comment;
        }
        return "";
    }

    /**
     * 校验数据库连接状态
     *
     * @return
     */
    public String checkDbConnection() {
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        DbTable table = dbTables.get(0);
        if (null != table) {
            Project project = WbfcFxApplication.getProject();
            DbPsiFacade facade = DbPsiFacade.getInstance(project);
            DataSourceStorage storage = DataSourceStorage.getProjectStorage(facade.getProject());
            LocalDataSource localDataSource = storage.getDataSourceById(table.getDataSource().getUniqueId());
            OneTimeString ots = DatabaseCredentials.getInstance().getPassword(localDataSource);
            if (null == ots) {
                return "您的数据库连接没有存储Password,请重新添加密码并保存后再试。";
            }
        }
        return "success";
    }

    /**
     * 获取配置信息实体
     *
     * @return
     */
    @Override
    public WbfcDbInfo getWbfcDbInfo() {
        WbfcDbInfo dbInfo = new WbfcDbInfo();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        DbTable table = dbTables.get(0);
        if (null != table) {
            DbDataSource dataSource = table.getDataSource();

            Project project = WbfcFxApplication.getProject();
            DbPsiFacade facade = DbPsiFacade.getInstance(project);
            DataSourceStorage storage = DataSourceStorage.getProjectStorage(facade.getProject());
            LocalDataSource localDataSource = storage.getDataSourceById(dataSource.getUniqueId());
            dbInfo.setDbUrl(localDataSource.getUrl());
            dbInfo.setDbDriver(localDataSource.getDriverClass());
            dbInfo.setDbUser(localDataSource.getUsername());

            OneTimeString ots = DatabaseCredentials.getInstance().getPassword(localDataSource);
            if (null != ots) {
                dbInfo.setDbPassword(ots.toString());
            }

            DatabaseDriver driver = localDataSource.getDatabaseDriver();
            List<SimpleClasspathElement> libList = driver.getClasspathElements();
            if (libList.size() > 0) {
                File libFile = null;
                String url = libList.get(0).getClassesRootUrls().get(0);
                if (url.startsWith("file://")) {
                    url = url.replaceFirst("file://", "");
                }
                dbInfo.setClassPathEntry(url);
            }
            //DatabaseDriverManager.getInstance().getDriver(.getName());
        }

        return dbInfo;
    }

    /**
     * 获取某表对应的列
     *
     * @param tableName
     * @return
     */
    @Override
    public List<TableColumn> getTableColumn(String tableName) {
        List<TableColumn> res = new ArrayList<>();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        Optional<DbTable> opt = dbTables.stream().filter(n -> n.getName().equals(tableName)).findAny();
        DbTable table = getDbTableByName(tableName);

        if (null != table) {
            JBIterable<? extends DasColumn> columnsIter = DasUtil.getColumns(table);
            List<? extends DasColumn> dasColumns = columnsIter.toList();
            for (DasColumn dasColumn : dasColumns) {
                TableColumn tCol = new TableColumn();
                tCol.setColumnName(dasColumn.getName());
                tCol.setTypeName(dasColumn.getDataType().typeName);
                res.add(tCol);
            }
        }
        return res;
    }

    /**
     * 获取配置文件目录
     *
     * @return
     */
    @Override
    public String getWbfcPath() {
        String path = "";
        Project project = WbfcFxApplication.getProject();
        WbfcEditorPersistentState state = WbfcEditorPersistentState.getInstance();
        String tempPath = "";
        if (null != state) {
            tempPath = state.getPath();
        }
        path = ConverterUtil.toString(state.getPath(), FilePathUtil.getSyStemFilePath(project.getWorkspaceFile().getParent().getPath(), File.separator, "wbfceditor"));
        if (path.endsWith("\\") || path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        path = FilePathUtil.getSyStemFilePath(path);
        return path;
    }

    /**
     * 保存配置文件
     *
     * @param configStr
     */
    public void saveWbfcConfig(String configStr) {
        if (ConverterUtil.isEmpty(configStr)) return;
        WbfcConfig tempConf = gson.fromJson(configStr, WbfcConfig.class);
        GeneratorUtil.setWbfcConfig(tempConf);
        String secStr = ConverterUtil.gson.toJson(tempConf);
        File serFile = new File(getWbfcPath() + File.separator + "WbfcEditor.tmp");
        try {
            IOUtils.writeLines(serFile, new String[]{secStr});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭APP
     */
    public void close() {
        WbfcFxApplication.hide();
    }

    /**
     * 生成代码(异步)
     */
    public void generateCodes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = "";
                try {
                    WbfcEditorRunner.generatorFiles();
                    res = "success";
                } catch (Exception e) {
                    logger.error(Exceptions.getStackTraceAsString(e));
                    res = "error";
                }
                WbfcFxApplication.setGenerateStatus(res);
            }
        }).start();
    }

    /**
     * 生成XML(异步)
     */
    public void generatorXml() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = "";
                try {
                    // 校验并生成配置
                    WbfcConfig wbfcConfig = GeneratorUtil.beforeGenMakeConfig();
                    // 校验文件生成目录
                    GeneratorUtil.makeAllPathDirs(wbfcConfig);
                    String[] tplArray = IOUtils.readLines(this.getClass().getResourceAsStream("/templates/generatorConfig.ftl"));
                    // 获取模板
                    String genConfiTpl = Lists.newArrayList(Arrays.stream(tplArray).iterator()).stream().collect(Collectors.joining("\n"));
                    // 生成xml
                    res = FtlManagerUtil.createWithStr(wbfcConfig, genConfiTpl);
                    // 转码
                    WbfcFxApplication.setDiyXml(encodeStr(res, StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }).start();
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

    /**
     * 重置配置文件
     */
    public void resetConfig() {
        String path = getWbfcPath();
        File tempFile = new File(path + File.separator + "WbfcEditor.tmp");
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    /**
     * 打开文件选择器
     *
     * @param pathName
     * @param pathVal
     * @return
     */
    public String openChooseFile(String pathName, String pathVal) {
        File exisFir = null;
        if (ConverterUtil.isNotEmpty(pathVal)) {
            exisFir = findExistsFile(pathVal);
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(exisFir);
        directoryChooser.setTitle("选择文件夹");
        File directory = directoryChooser.showDialog(new Stage());
        return directory.getAbsolutePath();

//        ApplicationManager.getApplication().invokeLater(() -> {
//            Project project = WbfcFxApplication.getProject();
//            if (ConverterUtil.isNotEmpty(pathName)) {
//                String res = "";
//                // 默认值
//                String def = ConverterUtil.toString(pathVal, GeneratorUtil.getWbfcConfigPath());
//                VirtualFile select = WbfcGenerator.findExistsVirFile(def);
//                Stage stage = WbfcFxApplication.getStage();
//                VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), (Project) null, (VirtualFile) select);
//                if (file != null) {
//                    res = FileUtil.toSystemDependentName(file.getPath());
//                    // 转换避免WebView传参问题
//                    res = ConverterUtil.escape(res);
//                    // 回填到页面
//                    WbfcFxApplication.setFormVal(pathName, res);
//                }
//            }
//        });

//        MessageBus messageBus = WbfcFxApplication.getProject().getMessageBus();
//        messageBus.syncPublisher(OpenFileChooseTopicListener.TOPIC_OPEN_FILE_CHOOSE).openFileChoose(pathName, pathVal);
    }


    /**
     * 递归查找返回存在的VirtualFile对象
     *
     * @param pathVal
     * @return
     */
    public static VirtualFile findExistsVirFile(String pathVal) {
        VirtualFile exVf = null;
        if (ConverterUtil.isNotEmpty(pathVal)) {
            File pathFile = new File(pathVal);
            if (pathFile.exists()) {
                exVf = LocalFileSystem.getInstance().findFileByIoFile(pathFile);
            } else {
                exVf = findExistsVirFile(pathFile.getParent());
            }
        }
        return exVf;
    }

    /**
     * 递归查找返回存在的File对象
     *
     * @param pathVal
     * @return
     */
    public static File findExistsFile(String pathVal) {
        File exVf = null;
        if (ConverterUtil.isNotEmpty(pathVal)) {
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
