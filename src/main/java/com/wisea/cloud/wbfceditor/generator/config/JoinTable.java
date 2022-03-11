package com.wisea.cloud.wbfceditor.generator.config;

import org.apache.commons.compress.utils.Lists;
import org.mybatis.generator.config.PropertyHolder;

import java.util.List;

/**
 * OneToMany标签
 */
public class JoinTable extends PropertyHolder {
    /**
     * 类型 OneToMany:一对多 OneToOne:一对一
     */
    private String type = "OneToMany";
    private String tableName;
    private String columns;
    /**
     * 是否级联 false:不级联(新增/修改/删除) true:级联上级操作
     */
    private boolean isCasecade = true;

    private List<JoinTable> joinTableList = Lists.newArrayList();
    private List<String> parents = Lists.newArrayList();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCasecade() {
        return isCasecade;
    }

    public void setCasecade(boolean casecade) {
        isCasecade = casecade;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public List<JoinTable> getJoinTableList() {
        return joinTableList;
    }

    public void setJoinTableList(List<JoinTable> joinTableList) {
        this.joinTableList = joinTableList;
    }

    public void addJoinList(JoinTable co) {
        this.joinTableList.add(co);
    }

    public List<String> getParents() {
        return parents;
    }

    public void addParents(List<String> parents) {
        this.parents.addAll(parents);
    }

    public void addParent(String parent) {
        this.parents.add(parent);
    }

    /**
     * 获取关联表的根节点
     *
     * @return
     */
    public String getRoot() {
        if (this.parents.size() > 0) {
            return this.parents.get(0);
        }
        return "";
    }
}
