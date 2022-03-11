package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.config.JoinTable;
import com.wisea.cloud.wbfceditor.generator.config.WbfcTableConfiguration;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.apache.commons.compress.utils.Lists;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class WbfcJoinTableElementGenerator extends AbstractXmlElementGenerator {

    protected WbfcXMLMapperGenerator generator;
    protected WbfcConfig wbfcConfig = null;

    public WbfcJoinTableElementGenerator(WbfcXMLMapperGenerator xmlMapperGenerator) {
        super();
        this.generator = xmlMapperGenerator;
        this.wbfcConfig = GeneratorUtil.getWbfcConfig();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        List<JoinTable> joinTableList = tc.getJoinTableList();
        // 添加relsXXXResultMap
        addResultMap(parentElement, introspectedTable, joinTableList, null, false);
        // 添加删除关联的sql
        addDeleteRelsElem(parentElement, joinTableList, false);
    }

    /**
     * 增加relsResultMap
     *
     * @param parentElement
     */
    protected void addResultMap(XmlElement parentElement, IntrospectedTable introspectedTable, List<JoinTable> joinTableList, JoinTable tableOtm, boolean parentIsEntity) {

        XmlElement answer = new XmlElement("resultMap");
        String javaType = calculateJavaType(introspectedTable);
        String mpName = introspectedTable.getTableConfiguration().getDomainObjectName();
        answer.addAttribute(new Attribute("id", mpName + "RelsResultMap"));

        String voName = javaType;
        boolean joinIsEntity = false;
        // 是否有povo
        if (this.generator.isHasPoVo()) {
            // 只有主表用ListVo
            voName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
            // 关联表用GetVo
            if(null != tableOtm){
                voName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            }
            // 如果没有主键 是无法生成Po VO 使用entity
            if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns()) || parentIsEntity) {
                voName = javaType;
            }
        }
        if (!voName.endsWith("Vo")) {
            joinIsEntity = true;
        }
        answer.addAttribute(new Attribute("type", voName));

        context.getCommentGenerator().addComment(answer);

        // 添加id属性
        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getPrimaryKeyColumns()) {
            XmlElement resultElement = new XmlElement("id");
            resultElement.addAttribute(generateColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute(
                    "property", introspectedColumn.getJavaProperty()));
            resultElement.addAttribute(new Attribute("jdbcType",
                    introspectedColumn.getJdbcTypeName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(new Attribute(
                        "typeHandler", introspectedColumn.getTypeHandler()));
            }

            answer.addElement(resultElement);
        }

        // 添加非id属性
        List<IntrospectedColumn> myColumns = Lists.newArrayList();
        myColumns.addAll(introspectedTable.getBaseColumns());
        for (IntrospectedColumn introspectedColumn : myColumns) {
            String fieldName = introspectedColumn.getJavaProperty();
            // 如果列为基础属性 则跳过
            if (fieldName.equals("createBy") || fieldName.equals("updateBy") || fieldName.equals("createDate") || fieldName.equals("updateDate")) {
                continue;
            }
            XmlElement resultElement = new XmlElement("result");

            resultElement.addAttribute(generateColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute(
                    "property", introspectedColumn.getJavaProperty()));
            resultElement.addAttribute(new Attribute("jdbcType",
                    introspectedColumn.getJdbcTypeName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(new Attribute(
                        "typeHandler", introspectedColumn.getTypeHandler()));
            }
            answer.addElement(resultElement);
        }
        // 添加自定义的一对一 一对多属性
        if (ConverterUtil.isNotEmpty(joinTableList)) {
            for (JoinTable otm : joinTableList) {
                String xmlType = "collection";
//                boolean subJoinIsEntity = false;
                if ("oneToOne".equalsIgnoreCase(otm.getType())) {
                    xmlType = "association";
                }
                XmlElement otmElement = new XmlElement(xmlType);
                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
                String joinProperty = "";
                if (null != joinTable) {
                    joinProperty = joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                    if (!"oneToOne".equalsIgnoreCase(otm.getType())) {
                        joinProperty += "List";
                    }
                    joinProperty = JavaBeansUtil.getValidPropertyName(joinProperty);
                    otmElement.addAttribute(new Attribute("property", joinProperty));
                    // ofType 使用entity 避免字段不对应
                    String joinVoName = calculateJavaType(joinTable);
                    // 是否有povo
//                    if (this.generator.isHasPoVo()) {
//                        joinVoName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage())  + "GetVo";
//                        // 如果没有主键 是无法生成Po VO 使用entity
//                        if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns()) || parentIsEntity) {
//                            joinVoName = javaType;
//                        }
//                    }
//                    if (!joinVoName.endsWith("Vo")) {
//                        subJoinIsEntity = true;
//                    }
                    //otmElement.addAttribute(new Attribute("ofType", joinVoName));
                    String sqlId = "get" + joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                    otmElement.addAttribute(new Attribute("select", sqlId));
                    StringBuffer columnQud = new StringBuffer();
                    Map<String, String> columnMap = GeneratorUtil.convColumnsToMap(otm.getColumns());
                    columnQud.append("{");
                    for (String key : columnMap.keySet()) {
                        columnQud.append(GeneratorUtil.col2Pro(key));
                        columnQud.append(" = ");
                        columnQud.append(columnMap.get(key));
                        columnQud.append(",");
                    }
                    // 删除最后一个逗号
                    columnQud.deleteCharAt(columnQud.length() - 1);
                    columnQud.append("}");
                    otmElement.addAttribute(new Attribute("column", columnQud.toString()));
                    answer.addElement(otmElement);
                    // 如果还有子关联的 就递归继续添加result和关联查询的sql
                    List<JoinTable> subList = otm.getJoinTableList();
                    if (ConverterUtil.isNotEmpty(subList)) {
                        addResultMap(parentElement, joinTable, subList, otm, joinIsEntity);
                    } else {
                        // 没有子关联了就直接添加关联查询的sql
                        addSelectRelsELem(parentElement, joinTable, subList, otm, joinIsEntity);
                    }
                }
            }
        }
        parentElement.addElement(answer);
        // 添加关联的sql
        addSelectRelsELem(parentElement, introspectedTable, joinTableList, tableOtm, joinIsEntity);
    }

    /**
     * 添加删除关联表sql
     *
     * @param parentElement
     * @param joinTableList
     */
    public void addDeleteRelsElem(XmlElement parentElement, List<JoinTable> joinTableList, boolean parentIsEntity) {
        if (ConverterUtil.isNotEmpty(joinTableList)) {
            for (JoinTable otm : joinTableList) {
                if (!otm.isCasecade()) {
                    continue;
                }
                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
                String sqlId = "batchDelete" + joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                StringBuffer whereQud = new StringBuffer();
                Map<String, String> columnMap = GeneratorUtil.convColumnsToMap(otm.getColumns());
                boolean hasAnd = false;
                for (String key : columnMap.keySet()) {
                    Optional<IntrospectedColumn> colOpt = joinTable.getColumn(key);
                    if (colOpt.isPresent()) {
                        if (hasAnd) {
                            whereQud.append(" and ");
                        }
                        IntrospectedColumn col = colOpt.get();
                        whereQud.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(col));
                        whereQud.append(" = ");
                        whereQud.append(MyBatis3FormattingUtilities.getParameterClause(col));
                        hasAnd = true;
                    }
                }
                boolean isJoinEntity = false;
                FullyQualifiedJavaType joinType = new FullyQualifiedJavaType(calculateJavaType(joinTable));
                if (this.generator.isHasPoVo()) {
                    // 如果没有主键就没有生成Po vo 用Entity
                    if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
                        isJoinEntity = true;
                    }
                } else {
                    isJoinEntity = true;
                }
                WbfcBatchDeleteDiyElementGenerator priBatchDelete = new WbfcBatchDeleteDiyElementGenerator(ConverterUtil.toBoolean(this.generator.isHasPoVo()), sqlId, whereQud.toString(), isJoinEntity);
                priBatchDelete.setContext(context);
                priBatchDelete.setIntrospectedTable(joinTable);
                priBatchDelete.addElements(parentElement);
                // 如果还有子关联的 就递归继续添加result和关联查询的sql
                List<JoinTable> subList = otm.getJoinTableList();
                if (ConverterUtil.isNotEmpty(subList)) {
                    addDeleteRelsElem(parentElement, subList, isJoinEntity);
                }
            }
        }
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

    /**
     * 添加查询关联关系SQL
     *
     * @param parentElement
     * @param introspectedTable
     * @param joinTableList
     */
    public void addSelectRelsELem(XmlElement parentElement, IntrospectedTable introspectedTable, List<JoinTable> joinTableList, JoinTable otm, boolean parentIsEntity) {
        XmlElement selectElem = new XmlElement("select");
        String joinType = calculateJavaType(introspectedTable);
        String mpName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String mapId = "get" + mpName + "Rels";
        // 如果是主表 关联函数为getRelations 关联表为 getXXRels
        if (null == otm) {
            mapId = "getRelations";
        }
        selectElem.addAttribute(new Attribute("id", mapId));
        String voName = "";
        // 如果没有关联的列表就代表没有子元素
        if (ConverterUtil.isNotEmpty(joinTableList)) {
            // 有子元素代表有子关联 要按map返回
            voName = mpName + "RelsResultMap";
            selectElem.addAttribute(new Attribute("resultMap", voName));
        } else {
            voName = joinType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            if (parentIsEntity) {
                voName = joinType;
            }
            selectElem.addAttribute(new Attribute("resultType", voName));
        }

        // ofType 使用entity 避免字段不对应
        String entityName = joinType;
        // 是否有povo
//        if (this.generator.isHasPoVo()) {
//            entityName = joinType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "GetPo";
//            if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns()) || parentIsEntity) {
//                entityName = joinType;
//            }
//            selectElem.addAttribute(new Attribute("parameterType", entityName));
//        } else {
//            entityName = introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
//        }
        selectElem.addAttribute(new Attribute("parameterType", entityName));

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ");
        sb.append(introspectedTable.getTableConfiguration().getTableName());
        selectElem.addElement(new TextElement(sb.toString()));
        sb.setLength(0);
        sb.append("where ");
        selectElem.addElement(new TextElement(sb.toString()));
        sb.setLength(0);

        boolean hasDel = false;
        Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
        if (delColumn.isPresent()) {
            sb.append("del_flag = '0'");
            hasDel = true;
            selectElem.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
        }

        // otm不为空说明是关联表
        if (null != otm) {
            // 设置列
            // column结构 = {关联表.列名 = 主表.列名} 多个逗号分隔
            Map<String, String> columnMap = GeneratorUtil.convColumnsToMap(otm.getColumns());
            for (String key : columnMap.keySet()) {
                if (hasDel) {
                    sb.append(" and ");
                }
                // 通过结构完成进行设置 所以key既是关联表列名 也是主表入参数据
                sb.append(key + " = " + "#{" + GeneratorUtil.col2Pro(key) + "}");
                selectElem.addElement(new TextElement(sb.toString()));
                sb.setLength(0);
            }
        } else {
            // otm为空是主表
            // 添加id条件 兼容 findList findPage和selectByPrimaryKey
            List<IntrospectedColumn> priList = introspectedTable.getPrimaryKeyColumns();
            boolean hasAnd = false;
            for (IntrospectedColumn col : priList) {
                String colJavaType = col.getFullyQualifiedJavaType().getFullyQualifiedName();
                // 只有字符串型用 不等于'' 其他都不等于空就可以了 避免判断失败的问题
                if (colJavaType.equals(String.class.getName())) {
                    sb.append("<if test=\"" + col.getJavaProperty() + " != null>");
                } else {
                    sb.append("<if test=\"" + col.getJavaProperty() + " != null and " + col.getJavaProperty() + " != ''\">");
                }
                selectElem.addElement(new TextElement(sb.toString()));
                sb.setLength(0);
                if (hasDel) {
                    sb.append(" and ");
                }
                sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(col));
                sb.append(" = ");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(col));
                selectElem.addElement(new TextElement(sb.toString()));
                sb.setLength(0);
                sb.append("</if>");
                selectElem.addElement(new TextElement(sb.toString()));
                sb.setLength(0);
            }
        }

        context.getCommentGenerator().addComment(selectElem);
        parentElement.addElement(selectElem);
    }

    public void addGetRelsELem(XmlElement parentElement, JoinTable otm, String sqlId) {
        IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
        if (null != joinTable) {
            // getRelsXXX
            XmlElement selectElem = new XmlElement("select");

            selectElem.addAttribute(new Attribute("id", sqlId));
            String voAllName = joinTable.getTableConfiguration().getDomainObjectName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            selectElem.addAttribute(new Attribute("resultType", voAllName));

            context.getCommentGenerator().addComment(selectElem);
        }
    }


//    public void addGetRels(XmlElement parentElement, List<OneToMany> oneToManyList) {
//        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
//        if (ConverterUtil.isNotEmpty(oneToManyList)) {
//            for (OneToMany otm : oneToManyList) {
//                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
//                if (null == joinTable) {
//                    continue;
//                }
//                // getRelsXXX
//                XmlElement selectElem = new XmlElement("select");
//
//                selectElem.addAttribute(new Attribute("id", "getRels" + joinTable.getTableConfiguration().getMapperName()));
//                String voAllName = joinTable.getTableConfiguration().getDomainObjectName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
//                selectElem.addAttribute(new Attribute("resultType", voAllName));
//
//                context.getCommentGenerator().addComment(selectElem);
//
//                StringBuilder sb = new StringBuilder();
//                sb.append("select * from ");
//                sb.append(joinTable.getAliasedFullyQualifiedTableNameAtRuntime());
//                sb.append("where ");
//                Optional<IntrospectedColumn> delColumn = joinTable.getColumn("del_flag");
//                if (delColumn.isPresent()) {
//                    sb.append("del_flag = '0' and ");
//                }
//                sb.append(otm.getJoinColName()"where ");
//                selectElem.addElement(new TextElement(sb.toString()));
//                parentElement.addElement(selectElem);
//            }
//        }
//    }

    public Attribute generateColumnAttribute(IntrospectedColumn introspectedColumn) {
        return new Attribute("column",
                MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn));
    }
}
