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

public class WbfcFindListElementGenerator extends AbstractXmlElementGenerator {
    private boolean hasPoVo = true;

    public WbfcFindListElementGenerator(boolean hasPoVo) {
        super();
        this.hasPoVo = hasPoVo;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", "findList")); //$NON-NLS-1$
        if (this.hasPoVo) {
            String voAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
            answer.addAttribute(new Attribute("resultType", voAllName));

            String poAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "ListPo";
            answer.addAttribute(new Attribute("parameterType", poAllName));
        } else {
            if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
                answer.addAttribute(new Attribute("resultMap", //$NON-NLS-1$
                        introspectedTable.getResultMapWithBLOBsId()));
            } else {
                answer.addAttribute(new Attribute("resultMap", //$NON-NLS-1$
                        introspectedTable.getBaseResultMapId()));
            }
            answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                    introspectedTable.getBaseRecordType()));
        }

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
