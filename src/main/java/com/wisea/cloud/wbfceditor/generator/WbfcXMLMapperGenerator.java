package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.wbfceditor.generator.WbfcDeleteLogicByPrimaryKeyElementGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcFindListElementGenerator;
import com.wisea.cloud.wbfceditor.generator.WbfcFindPageElementGenerator;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

public class WbfcXMLMapperGenerator extends XMLMapperGenerator {
    private boolean hasPoVo = true;

    public WbfcXMLMapperGenerator(boolean hasPoVo) {
        this.hasPoVo = hasPoVo;
    }

    @Override
    protected XmlElement getSqlMapElement() {
        XmlElement answer = super.getSqlMapElement();
        // 有PoVo
        if (this.hasPoVo) {
            // 增加findPage方法
            addFindPageElement(answer);
        }
        // 增加findList方法
        addFindListElement(answer);
        // 增加deleteLogicByPrimaryKey方法
        addDeleteLogicByPrimaryKey(answer);
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
}
