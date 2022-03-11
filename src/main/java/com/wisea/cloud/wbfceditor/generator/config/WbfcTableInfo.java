package com.wisea.cloud.wbfceditor.generator.config;

import org.mybatis.generator.config.PropertyHolder;

/**
 * tableInfo标签 要删除
 */
public class WbfcTableInfo extends PropertyHolder {

    /**
     * 简化Po和Vo
     */
    private String simplePoVo = "false";
    /**
     * 批量新增
     */
    private String batchInsert = "false";
    /**
     * 批量更新
     */
    private String batchUpdate = "false";
    /**
     * 批量删除
     */
    private String batchDelete = "false";
    /**
     * 是否只为关系表
     */
    private boolean isRelation = false;
    /**
     * 更新策略
     */
    private String updateStrategy = "DELETE_INSERT";

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

    public boolean isRelation() {
        return isRelation;
    }

    public void setRelation(boolean relation) {
        isRelation = relation;
    }

    public String getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(String updateStrategy) {
        this.updateStrategy = updateStrategy;
    }
}
