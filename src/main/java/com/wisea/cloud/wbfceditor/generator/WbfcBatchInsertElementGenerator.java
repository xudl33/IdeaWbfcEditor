package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.List;

public class WbfcBatchInsertElementGenerator extends AbstractXmlElementGenerator {

    public WbfcBatchInsertElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
        if (columnList.size() <= 0) {
            return;
        }
        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", "batchInsert")); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sTemp = new StringBuilder();
        sTemp.append("insert into "); //$NON-NLS-1$
        sTemp.append(introspectedTable.getTableConfiguration().getTableName());
        sTemp.append(" (");
        for (int i = 0; i < columnList.size(); i++) {
            IntrospectedColumn col = columnList.get(i);
            sTemp.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(col));
            if(i < columnList.size() - 1){
                sTemp.append(", ");
            }
            if (sTemp.length() > 80) {
                answer.addElement(new TextElement(sTemp.toString()));
                sTemp.setLength(0);
                OutputUtilities.xmlIndent(sTemp, 1);
            }
        }
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append(") values ");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append("<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append("(");
        for (int i = 0; i < columnList.size(); i++) {
            OutputUtilities.xmlIndent(sTemp, 1);
            IntrospectedColumn col = columnList.get(i);
            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(col, "item."));
            if(i < columnList.size() - 1){
                sTemp.append(",");
            }
            if (sTemp.length() > 80) {
                answer.addElement(new TextElement(sTemp.toString()));
                sTemp.setLength(0);
                OutputUtilities.xmlIndent(sTemp, 1);
            }
        }
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append(")");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append("</foreach>");
        answer.addElement(new TextElement(sTemp.toString()));

        parentElement.addElement(answer);
    }
}
