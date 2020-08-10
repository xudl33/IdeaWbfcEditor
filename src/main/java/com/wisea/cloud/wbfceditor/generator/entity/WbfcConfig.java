package com.wisea.cloud.wbfceditor.generator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.model.annotation.Check;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Wbfc代码生成器配置文件
 *
 */
public class WbfcConfig implements Serializable {

    private String logProperties;

    @Check(test = "required", requiredMsg = "数据库驱动jar包路径不能为空")
    private String classPathEntry;

    @Check(test = "required", requiredMsg = "DB Driver不能为空")
    private String dbDriver;

    @Check(test = "required", requiredMsg = "DB URL不能为空")
    private String dbUrl;

    @Check(test = "required", requiredMsg = "DB Username不能为空")
    private String dbUser;

    @Check(test = "required", requiredMsg = "DB Password不能为空")
    private String dbPassword;

    @Check(test = "logic", logic = "'true'.equals(#hasController)?#isNotEmpty(#controllerPath):true", logicMsg = "Controller路径不能为空")
    private String controllerPath;

    @Check(test = "logic", logic = "'true'.equals(#hasController)?#isNotEmpty(#controllerPath):true", logicMsg = "Controller包名不能为空")
    private String controllerPackage;

    @Check(test = "logic", logic = "'true'.equals(#hasService)?#isNotEmpty(#servicePath):true", logicMsg = "Service路径不能为空")
    private String servicePath;

    @Check(test = "logic", logic = "'true'.equals(#hasService)?#isNotEmpty(#servicePackage):true", logicMsg = "Service包名不能为空")
    private String servicePackage;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#entityPath):true", logicMsg = "Entity路径不能为空")
    private String entityPath;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#entityPackage):true", logicMsg = "Entity路径不能为空")
    private String entityPackage;

    @Check(test = "logic", logic = "'true'.equals(#hasPoVo)?#isNotEmpty(#poPath):true", logicMsg = "Po路径不能为空")
    private String poPath;

    @Check(test = "logic", logic = "'true'.equals(#hasPoVo)?#isNotEmpty(#poPackage):true", logicMsg = "Po包名不能为空")
    private String poPackage;

    @Check(test = "logic", logic = "'true'.equals(#hasPoVo)?#isNotEmpty(#voPath):true", logicMsg = "Vo路径不能为空")
    private String voPath;

    @Check(test = "logic", logic = "'true'.equals(#hasPoVo)?#isNotEmpty(#voPackage):true", logicMsg = "Vo包名不能为空")
    private String voPackage;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#daoPath):true", logicMsg = "Dao(Mapper)路径不能为空")
    private String daoPath;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#daoPackage):true", logicMsg = "Dao(Mapper)包名不能为空")
    private String daoPackage;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#xmlPath):true", logicMsg = "MapperXML路径不能为空")
    private String xmlPath;

    @Check(test = "logic", logic = "'true'.equals(#hasMapper)?#isNotEmpty(#xmlPath):true", logicMsg = "MapperXML包名不能为空")
    private String xmlPackage;

    @Check(test = "required", cascade = true)
    private List<WbfcDataTable> tablesCloumnList = Lists.newArrayList();

    private List<String> tablesList = Lists.newArrayList();

    private String hasXml = "false";
    private String diyXml;

    @Check(test = "required")
    private String hasController = "true";

    @Check(test = "required")
    private String hasService = "true";

    @Check(test = "required")
    private String hasMapper = "true";

    @Check(test = "required")
    private String hasPoVo = "true";

    @Check(test = "required")
    private String simplePoVo = "false";

    public String getLogProperties() {
        return logProperties;
    }

    public void setLogProperties(String logProperties) {
        this.logProperties = logProperties;
    }

    public String getClassPathEntry() {
        return classPathEntry;
    }

    public void setClassPathEntry(String classPathEntry) {
        this.classPathEntry = classPathEntry;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public void setControllerPath(String controllerPath) {
        this.controllerPath = controllerPath;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getServicePackage() {
        return servicePackage;
    }

    public void setServicePackage(String servicePackage) {
        this.servicePackage = servicePackage;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public String getPoPath() {
        return poPath;
    }

    public void setPoPath(String poPath) {
        this.poPath = poPath;
    }

    public String getPoPackage() {
        return poPackage;
    }

    public void setPoPackage(String poPackage) {
        this.poPackage = poPackage;
    }

    public String getVoPath() {
        return voPath;
    }

    public void setVoPath(String voPath) {
        this.voPath = voPath;
    }

    public String getVoPackage() {
        return voPackage;
    }

    public void setVoPackage(String voPackage) {
        this.voPackage = voPackage;
    }

    public String getDaoPath() {
        return daoPath;
    }

    public void setDaoPath(String daoPath) {
        this.daoPath = daoPath;
    }

    public String getDaoPackage() {
        return daoPackage;
    }

    public void setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getXmlPackage() {
        return xmlPackage;
    }

    public void setXmlPackage(String xmlPackage) {
        this.xmlPackage = xmlPackage;
    }

    @JsonIgnore
    public List<String> getTablesList() {
        return tablesList;
    }

    public void setTablesList(List<String> tablesList) {
        this.tablesList = tablesList;
    }

    public String getHasController() {
        return hasController;
    }

    public void setHasController(String hasController) {
        this.hasController = hasController;
    }

    public String getHasService() {
        return hasService;
    }

    public void setHasService(String hasService) {
        this.hasService = hasService;
    }

    public String getHasMapper() {
        return hasMapper;
    }

    public void setHasMapper(String hasMapper) {
        this.hasMapper = hasMapper;
    }

    public String getHasPoVo() {
        return hasPoVo;
    }

    public void setHasPoVo(String hasPoVo) {
        this.hasPoVo = hasPoVo;
    }

    @JsonIgnore
    public List<WbfcDataTable> getTablesCloumnList() {
        return tablesCloumnList;
    }

    public void setTablesCloumnList(List<WbfcDataTable> tablesCloumnList) {
        this.tablesCloumnList = tablesCloumnList;
    }

    public String getSimplePoVo() {
        return simplePoVo;
    }

    public void setSimplePoVo(String simplePoVo) {
        this.simplePoVo = simplePoVo;
    }

    public String getHasXml() {
        return hasXml;
    }

    public void setHasXml(String hasXml) {
        this.hasXml = hasXml;
    }

    @JsonIgnore
    public String getDiyXml() {
        return diyXml;
    }

    public void setDiyXml(String diyXml) {
        this.diyXml = diyXml;
    }

    public WbfcDataTable getDataTable(String tableName) {
        Optional<WbfcDataTable> tab = tablesCloumnList.stream().filter(t -> tableName.equals(t.getTableName())).findFirst();
        if (!tab.isPresent()) {
            return null;
        }
        return tab.get();
    }

    public Map<String, WbfcDataColumn> getColumnsMap(String tableName) {
        Map<String, WbfcDataColumn> res = Maps.newHashMap();
        if (ConverterUtil.isEmpty(tableName)) {
            return res;
        }
        Optional<WbfcDataTable> tab = tablesCloumnList.stream().filter(t -> tableName.equals(t.getTableName())).findFirst();
        if (!tab.isPresent()) {
            return res;
        }
        res = tab.get().getColumns().stream().collect(Collectors.toMap(WbfcDataColumn::getColName, c -> c));
        return res;
    }

    public void setTotalPath(String totalPath){
        if (ConverterUtil.isNotEmpty(totalPath)){
            if(ConverterUtil.toBoolean(this.hasController)){
                this.setControllerPath(totalPath);
            }
            if(ConverterUtil.toBoolean(this.hasService)){
                this.setServicePath(totalPath);
            }
            if(ConverterUtil.toBoolean(this.hasPoVo)){
                this.setPoPath(totalPath);
                this.setVoPath(totalPath);
            }
            this.setEntityPath(totalPath);
            this.setDaoPath(totalPath);
        }
    }
}
