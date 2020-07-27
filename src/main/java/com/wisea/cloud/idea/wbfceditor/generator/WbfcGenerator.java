package com.wisea.cloud.idea.wbfceditor.generator;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.*;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.ui.classpath.SimpleClasspathElement;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorRunner;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDbInfo;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.apache.commons.compress.utils.Lists;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WbfcGenerator implements WbfcEditorGenerator {
    public static Gson gson = new Gson();

    public static String case2Name(String name) {
        if (null != name && !"".equals(name)) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
        }
        return "";
    }

    public String getProjectConfig() {
        WbfcConfig conf = GeneratorUtil.getWbfcConfig();
        Project project = WbfcFxApplication.getProject();
        conf.setControllerPath(project.getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "java");
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

        conf.setXmlPath(project.getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources");
        conf.setXmlPackage("mappings");
        return gson.toJson(conf);
    }

    public boolean isAbsDirectory(String path) {
        File tep = new File(path);
        // 必须是目录且是绝对路径
        return tep.isDirectory() && tep.isAbsolute();
    }

    public String getTables() {
        List<WbfcDataTable> res = Lists.newArrayList();
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        for (DbTable dbTable : dbTables) {
            res.add(new WbfcDataTable(dbTable.getName(), case2Name(dbTable.getName())));
        }
        return gson.toJson(res);
    }

    public DbTable getDbTableByName(String tableName) {
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        Optional<DbTable> opt = dbTables.stream().filter(n -> n.getName().equals(tableName)).findAny();
        DbTable table = null;
        if (opt.isPresent()) table = opt.get();
        return table;
    }

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

        return gson.toJson(res);
    }

    @Override
    public String getTableRemarks(String tableName) {
        DbTable table = getDbTableByName(tableName);
        if (null != table) {
            String comment = table.getComment();
            return comment;
        }
        return "";
    }

    public String checkDbConnection(){
        List<DbTable> dbTables = WbfcFxApplication.getTableList();
        DbTable table = dbTables.get(0);
        if (null != table) {
            Project project = WbfcFxApplication.getProject();
            DbPsiFacade facade = DbPsiFacade.getInstance(project);
            DataSourceStorage storage = DataSourceStorage.getProjectStorage(facade.getProject());
            LocalDataSource localDataSource = storage.getDataSourceById(table.getDataSource().getUniqueId());
            OneTimeString ots = DatabaseCredentials.getInstance().getPassword(localDataSource);
            if(null == ots){
                return "您的数据库连接没有存储Password,请重新添加密码并保存后再试。";
            }
        }
       return "success";
    }

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
            if(null != ots){
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

    public void saveWbfcConfig(String configStr) {
        if (ConverterUtil.isEmpty(configStr)) return;
        WbfcConfig tempConf = gson.fromJson(configStr, WbfcConfig.class);
        GeneratorUtil.setWbfcConfig(tempConf);
    }

    public void close() {
        WbfcFxApplication.hide();
    }

    public String generateCodes() {
        try {
            WbfcEditorRunner.generatorFiles();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }
}
