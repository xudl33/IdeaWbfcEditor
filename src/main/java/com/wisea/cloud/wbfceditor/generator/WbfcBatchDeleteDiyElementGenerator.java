package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.Optional;

/**
 * 自定义删除条件
 */
public class WbfcBatchDeleteDiyElementGenerator extends AbstractXmlElementGenerator {
    private boolean hasPoVo;
    private boolean parentIsEntity;
    private String sqlId;
    private String whereQud;

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getWhereQud() {
        return whereQud;
    }

    public void setWhereQud(String whereQud) {
        this.whereQud = whereQud;
    }

    public WbfcBatchDeleteDiyElementGenerator(boolean hasPoVo, String sqlId, String whereQud, boolean parentIsEntity) {
        super();
        this.hasPoVo = hasPoVo;
        this.sqlId = sqlId;
        this.whereQud = whereQud;
        this.parentIsEntity = parentIsEntity;
    }

    /**
     * 是否有子包
     *
     * @param propertyHolder
     * @return
     */
    public boolean isSubPackagesEnabled(PropertyHolder propertyHolder) {
        return ConverterUtil.toBoolean(propertyHolder.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES));
    }

    /**
     * 计算表的JavaType
     *
     * @param introspectedTable
     * @return
     */
    public String calculateJavaType(IntrospectedTable introspectedTable) {
        String javaType = introspectedTable.getBaseRecordType();
        if (ConverterUtil.isNotEmpty(javaType)) {
            return javaType;
        }
        JavaModelGeneratorConfiguration config = context
                .getJavaModelGeneratorConfiguration();
        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        sb.append(introspectedTable.getFullyQualifiedTable().getSubPackageForModel(isSubPackagesEnabled(config)));
        sb.append('.');
        sb.append(introspectedTable.getTableConfiguration().getDomainObjectName());
        return sb.toString();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        // 如果有delflag才设置deleteLogicByPrimaryKey方法
        Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
        if (null == delColumn) {
            return;
        }
        XmlElement answer = new XmlElement("delete");

        answer.addAttribute(new Attribute("id", this.sqlId));

        FullyQualifiedJavaType joinType = new FullyQualifiedJavaType(calculateJavaType(introspectedTable));
        String parametertType = "";
        if (this.hasPoVo) {
            // 如果没有主键就没有生成Po vo 用Entity
            if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns()) || parentIsEntity) {
                parametertType = joinType.getFullyQualifiedName();
            } else {
                parametertType = joinType.getFullyQualifiedName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            }
        } else {
            parametertType = joinType.getFullyQualifiedName();
        }
        answer.addAttribute(new Attribute("parameterType", parametertType));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sTemp = new StringBuilder();

        sTemp.append("delete from "); //$NON-NLS-1$
        sTemp.append(introspectedTable.getTableConfiguration().getTableName());
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);
        sTemp.append("where " + whereQud);
        answer.addElement(new TextElement(sTemp.toString()));
        sTemp.setLength(0);

        parentElement.addElement(answer);
    }
}
