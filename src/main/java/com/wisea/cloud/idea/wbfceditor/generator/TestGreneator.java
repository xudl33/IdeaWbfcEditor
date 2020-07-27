package com.wisea.cloud.idea.wbfceditor.generator;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.DataSourceStorage;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.openapi.project.Project;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;

import java.io.File;
import java.util.List;

public class TestGreneator {
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
        return new Gson().toJson(conf);
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
        // 必须是目录且是绝对路径
        return tep.isAbsolute() && tep.isDirectory();
    }

    public String generateCodes() {
        return "success";
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
}
