package com.wisea.cloud.wbfceditor.generator.entity;

import com.google.common.collect.Lists;
import com.wisea.cloud.model.annotation.Check;

import java.util.List;

public class WbfcDataTable {
    @Check(test = "required", requiredMsg = "表名不能为空")
    private String tableName;

    @Check(test = "required", requiredMsg = "映射名不能为空")
    private String entityName;

    @Check(test="required")
    private String simplePoVo = "false";

    @Check(test="required")
    private String batchInsert = "false";

    @Check(test="required")
    private String batchUpdate = "false";

    @Check(test="required")
    private String batchDelete = "false";

    @Check(test = "logic", cascade = true, nullSkip = true)
    private List<WbfcDataColumn> columns = Lists.newArrayList();

    public WbfcDataTable(String tableName, String entityName) {
        this.tableName = tableName;
        this.entityName = entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<WbfcDataColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<WbfcDataColumn> columns) {
        this.columns = columns;
    }


    public String getSimplePoVo() {
        return simplePoVo;
    }

    public void setSimplePoVo(String simplePoVo) {
        this.simplePoVo = simplePoVo;
    }

    public String getBatchInsert() {
        return batchInsert;
    }

    public void setBatchInsert(String batchInsert) {
        this.batchInsert = batchInsert;
    }

    public String getBatchUpdate() {
        return batchUpdate;
    }

    public void setBatchUpdate(String batchUpdate) {
        this.batchUpdate = batchUpdate;
    }

    public String getBatchDelete() {
        return batchDelete;
    }

    public void setBatchDelete(String batchDelete) {
        this.batchDelete = batchDelete;
    }
}
