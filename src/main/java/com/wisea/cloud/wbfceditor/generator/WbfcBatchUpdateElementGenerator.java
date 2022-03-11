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

public class WbfcBatchUpdateElementGenerator extends AbstractXmlElementGenerator {

    public WbfcBatchUpdateElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
        if (columnList.size() <= 0) {
            return;
        }
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", "batchUpdate")); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(answer);


        StringBuilder sTemp = new StringBuilder();
        sTemp.append("<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\";\">"); //$NON-NLS-1$
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        OutputUtilities.xmlIndent(sTemp, 1);
        sTemp.append("update "); //$NON-NLS-1$
        sTemp.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sTemp.toString()));

        sTemp.setLength(0);
        OutputUtilities.xmlIndent(sTemp, 2);
        sTemp.append("<set>");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);


        OutputUtilities.xmlIndent(sTemp, 3);
        for (int i = 0; i < columnList.size(); i++) {
            IntrospectedColumn col = columnList.get(i);
            String colName = MyBatis3FormattingUtilities.getEscapedColumnName(col);
            // 主键或默认更新属性跳过
            if (introspectedTable.getPrimaryKeyColumns().contains(col) || colName.equals("create_by") || colName.equals("create_date") || colName.equals("del_flag")) {
                continue;
            }
            sTemp.append(colName);
            sTemp.append(" = ");
            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(col, "item."));
            sTemp.append(",");
            if (sTemp.length() > 80) {
                answer.addElement(new TextElement(sTemp.toString()));
                sTemp.setLength(0);
                OutputUtilities.xmlIndent(sTemp, 3);
            }
        }
        String tempSb = sTemp.toString();
        if(tempSb.endsWith(",")){
            tempSb = tempSb.substring(0, tempSb.length() - 1);
            sTemp.setLength(0);
            sTemp.append(tempSb);
        }
//        Optional<IntrospectedColumn> updateByColumn = introspectedTable.getColumn("update_by");
//        if (updateByColumn.isPresent()) {
//            sTemp.append(',');
//            sTemp.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateByColumn.get()));
//            sTemp.append(" = ");
//            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(updateByColumn.get(), "item."));
//        }
//        Optional<IntrospectedColumn> updateDateColumn = introspectedTable.getColumn("update_date");
//        if (updateDateColumn.isPresent()) {
//            sTemp.append(',');
//            sTemp.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(updateDateColumn.get()));
//            sTemp.append(" = ");
//            sTemp.append(MyBatis3FormattingUtilities.getParameterClause(updateDateColumn.get(), "item."));
//        }
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        OutputUtilities.xmlIndent(sTemp, 2);
        sTemp.append("</set>");
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        OutputUtilities.xmlIndent(sTemp, 2);
        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sTemp.setLength(0);
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
        }
        sTemp.setLength(0);
        sTemp.append("</foreach>");
        answer.addElement(new TextElement(sTemp.toString()));

        parentElement.addElement(answer);
    }
}
