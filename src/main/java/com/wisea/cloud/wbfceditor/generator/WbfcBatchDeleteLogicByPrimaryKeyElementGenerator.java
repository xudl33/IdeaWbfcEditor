package com.wisea.cloud.wbfceditor.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Optional;

public class WbfcBatchDeleteLogicByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    public WbfcBatchDeleteLogicByPrimaryKeyElementGenerator() {
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

        answer.addAttribute(new Attribute("id", "batchDelete")); //$NON-NLS-1$

        answer.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sTemp = new StringBuilder();
        sTemp.append("<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\";\">");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        OutputUtilities.xmlIndent(sTemp, 1);
        sTemp.append("update "); //$NON-NLS-1$
        sTemp.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        OutputUtilities.xmlIndent(sTemp, 2);
        sTemp.append("set del_flag = '1'");
        Optional<IntrospectedColumn> updateByColumn = introspectedTable.getColumn("update_by");
        if (updateByColumn.isPresent()) {
            sTemp.append(',');
            sTemp.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateByColumn.get()));
            sTemp.append(" = ");
            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(updateByColumn.get(), "item."));
        }
        Optional<IntrospectedColumn> updateDateColumn = introspectedTable.getColumn("update_date");
        if (updateDateColumn.isPresent()) {
            sTemp.append(',');
            sTemp.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateDateColumn.get()));
            sTemp.append(" = ");
            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(updateDateColumn.get(),"item."));
        }
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            OutputUtilities.xmlIndent(sTemp, 1);
            if (and) {
                sTemp.append("  and ");
            } else {
                sTemp.append("where ");
                and = true;
            }

            sTemp.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sTemp.append(" = ");
            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item."));
            answer.addElement(new TextElement(sTemp.toString()));
            sTemp.setLength(0);
        }

        sTemp.append("</foreach>");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        parentElement.addElement(answer);
    }
}
