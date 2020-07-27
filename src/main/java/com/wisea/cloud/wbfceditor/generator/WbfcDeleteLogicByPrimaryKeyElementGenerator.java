package com.wisea.cloud.wbfceditor.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Optional;

public class WbfcDeleteLogicByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    public WbfcDeleteLogicByPrimaryKeyElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        // 如果有delflag才设置deleteLogicByPrimaryKey方法
        Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
        if (null == delColumn) {
            return;
        }
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", "deleteLogic")); //$NON-NLS-1$

        answer.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("set del_flag = '1'");
        Optional<IntrospectedColumn> updateByColumn = introspectedTable.getColumn("update_by");
        if (updateByColumn.isPresent()) {
            sb.append(',');
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateByColumn.get()));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(updateByColumn.get()));
        }
        Optional<IntrospectedColumn> updateDateColumn = introspectedTable.getColumn("update_date");
        if (updateDateColumn.isPresent()) {
            sb.append(',');
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateDateColumn.get()));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(updateDateColumn.get()));
        }
        answer.addElement(new TextElement(sb.toString()));

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            answer.addElement(new TextElement(sb.toString()));
        }

        parentElement.addElement(answer);
    }
}
