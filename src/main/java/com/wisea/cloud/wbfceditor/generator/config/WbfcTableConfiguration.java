package com.wisea.cloud.wbfceditor.generator.config;

import com.wisea.cloud.common.util.ConverterUtil;
import org.apache.commons.compress.utils.Lists;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;

import java.util.List;

/**
 * WbfcTable配置 覆盖原TableConfiguration
 */
public class WbfcTableConfiguration extends TableConfiguration {
    /**
     * wbfc特有的表信息
     */
    private WbfcTableInfo tableInfo;
    /**
     * 一对多关联关系
     */
    private List<JoinTable> joinTableList;

    public WbfcTableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(WbfcTableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public boolean hasRelation(){
        return ConverterUtil.isNotEmpty(joinTableList);
    }

    public List<JoinTable> getJoinTableList() {
        return joinTableList;
    }

    public void setJoinTableList(List<JoinTable> joinTableList) {
        this.joinTableList = joinTableList;
    }

    public WbfcTableConfiguration(Context context) {
        super(context);
        joinTableList = Lists.newArrayList();
        tableInfo = new WbfcTableInfo();
    }

    public void addJoinTable(JoinTable co) {
        joinTableList.add(co);
    }
}
