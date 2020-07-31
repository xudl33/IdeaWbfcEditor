package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.WbfcDeleteLogicByPrimaryKeyElementGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcFindListElementGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcFindPageElementGenerator;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

public class WbfcXMLMapperGenerator extends XMLMapperGenerator {
    private boolean hasPoVo = true;
    private boolean batchInsert = false;
    private boolean batchUpdate = false;
    private boolean batchDelete = false;

    public WbfcXMLMapperGenerator(boolean hasPoVo) {
        this.hasPoVo = hasPoVo;
    }

    public boolean isHasPoVo() {
        return hasPoVo;
    }

    public void setHasPoVo(boolean hasPoVo) {
        this.hasPoVo = hasPoVo;
    }

    public boolean isBatchInsert() {
        return batchInsert;
    }

    public void setBatchInsert(boolean batchInsert) {
        this.batchInsert = batchInsert;
    }

    public boolean isBatchUpdate() {
        return batchUpdate;
    }

    public void setBatchUpdate(boolean batchUpdate) {
        this.batchUpdate = batchUpdate;
    }

    public boolean isBatchDelete() {
        return batchDelete;
    }

    public void setBatchDelete(boolean batchDelete) {
        this.batchDelete = batchDelete;
    }

    @Override
    protected XmlElement getSqlMapElement() {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        WbfcDataTable datatable = wbfcConfig.getDataTable(tableName);
        if (null != datatable) {
            this.batchInsert = ConverterUtil.toBoolean(datatable.getBatchInsert());
            this.batchUpdate = ConverterUtil.toBoolean(datatable.getBatchUpdate());
            this.batchDelete = ConverterUtil.toBoolean(datatable.getBatchDelete());
        }
        XmlElement answer = super.getSqlMapElement();
        // 有PoVo
        if (this.hasPoVo) {
            // 增加findPage方法
            addFindPageElement(answer);
        }
        // 增加findList方法
        addFindListElement(answer);
        if (this.batchInsert) {
            addBatchInsertElement(answer);
        }
        if (this.batchUpdate) {
            addBatchUpdateElement(answer);
        }
        if (this.batchDelete) {
            // 增加批量删除方法
            addBatchDeleteByPrimaryKey(answer);
        } else {
            // 增加deleteLogicByPrimaryKey方法
            addDeleteLogicByPrimaryKey(answer);
        }
        return answer;
    }

    protected void addFindPageElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcFindPageElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void addFindListElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcFindListElementGenerator(this.hasPoVo);
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void addDeleteLogicByPrimaryKey(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcDeleteLogicByPrimaryKeyElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void addBatchDeleteByPrimaryKey(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcBatchDeleteLogicByPrimaryKeyElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void addBatchInsertElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcBatchInsertElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void addBatchUpdateElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new WbfcBatchUpdateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }
}
