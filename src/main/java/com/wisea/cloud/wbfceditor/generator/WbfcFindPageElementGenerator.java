package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Optional;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class WbfcFindPageElementGenerator extends AbstractXmlElementGenerator {

    public WbfcFindPageElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute("id", "findPage")); //$NON-NLS-1$
        String voAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
        answer.addAttribute(new Attribute("resultType", voAllName));

        String poAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "PagePo";
        answer.addAttribute(new Attribute("parameterType", poAllName));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("select "); //$NON-NLS-1$

        if (stringHasValue(introspectedTable.getSelectByPrimaryKeyQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByPrimaryKeyQueryId());
            sb.append("' as QUERYID,"); //$NON-NLS-1$
        }
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getBaseColumnListElement());
        if (introspectedTable.hasBLOBColumns()) {
            answer.addElement(new TextElement(",")); //$NON-NLS-1$
            answer.addElement(getBlobColumnListElement());
        }

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
        if (delColumn.isPresent()) {
            sb.setLength(0);
            sb.append("where del_flag = '0'"); //$NON-NLS-1$
            answer.addElement(new TextElement(sb.toString()));
        }
        parentElement.addElement(answer);
    }
}
