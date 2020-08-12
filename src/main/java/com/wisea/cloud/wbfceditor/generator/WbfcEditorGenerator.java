package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDbInfo;

import java.util.List;

public interface WbfcEditorGenerator {
    public String getTableRemarks(String tableName);

    public WbfcDbInfo getWbfcDbInfo();

    public List<TableColumn> getTableColumn(String tableName);

    public String getWbfcPath();
}
