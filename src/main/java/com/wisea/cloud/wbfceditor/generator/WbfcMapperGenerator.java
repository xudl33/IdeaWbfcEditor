package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.config.JoinTable;
import com.wisea.cloud.wbfceditor.generator.config.WbfcTableConfiguration;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class WbfcMapperGenerator extends JavaMapperGenerator {
    private static final String CRUD_DAO = "com.wisea.cloud.common.mybatis.persistence.CrudDao";
    private boolean hasPoVo = true;

    public WbfcMapperGenerator() {
        super("");
    }

    public WbfcMapperGenerator(String project) {
        super(project);
    }

    @Override
    public String getProject() {
        return context.getJavaClientGeneratorConfiguration().getTargetProject();
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        // 设置属性
        String hasPoVal = context.getProperty("hasPoVo");
        if (ConverterUtil.isNotEmpty(hasPoVal)) {
            this.hasPoVo = ConverterUtil.toBoolean(hasPoVal);
        }
        progressCallback.startTask(getString("Progress.17", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();


        String tableName = introspectedTable.getTableConfiguration().getTableName();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);
        commentGenerator.addJavaFileComment(interfaze, tableName + " 表DAO", GeneratorUtil.getTableRemarks(introspectedTable));

        FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(CRUD_DAO);
        superClass.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        interfaze.addSuperInterface(superClass);
        interfaze.addImportedType(superClass);

        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = context.getJavaClientGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        // 以下除去example方法 所有方法都移入CrudDao中了
        // 增加使用Po来进行分页查询方法
        addFindPageWithPoMethod(interfaze);
        // 增加使用Po进行list查询方法
        addFindListWithPoMethod(interfaze);
        // addCountByExampleMethod(interfaze);
        // addDeleteByExampleMethod(interfaze);
        // addDeleteByPrimaryKeyMethod(interfaze);
        // addInsertMethod(interfaze);
        // addInsertSelectiveMethod(interfaze);
        addSelectByExampleWithBLOBsMethod(interfaze);
        addSelectByExampleWithoutBLOBsMethod(interfaze);
        // addSelectByPrimaryKeyMethod(interfaze);
        addUpdateByExampleSelectiveMethod(interfaze);
        addUpdateByExampleWithBLOBsMethod(interfaze);
        addUpdateByExampleWithoutBLOBsMethod(interfaze);
        // addUpdateByPrimaryKeySelectiveMethod(interfaze);
        // addUpdateByPrimaryKeyWithBLOBsMethod(interfaze);
        // addUpdateByPrimaryKeyWithoutBLOBsMethod(interfaze);

        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 添加带有一对多的查询函数
        if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
            addGetRelsPoMethod(interfaze);
            addDeleteRelsMethod(interfaze, tc.getJoinTableList(), false);
        }

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().clientGenerated(interfaze, introspectedTable)) {
            answer.add(interfaze);
        }

        List<CompilationUnit> extraCompilationUnits = getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        return answer;
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
        JavaModelGeneratorConfiguration config = context.getJavaModelGeneratorConfiguration();
        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        sb.append(introspectedTable.getFullyQualifiedTable().getSubPackageForModel(isSubPackagesEnabled(config)));
        sb.append('.');
        sb.append(introspectedTable.getTableConfiguration().getDomainObjectName());
        return sb.toString();
    }

    protected void addDeleteRelsMethod(Interface interfaze, List<JoinTable> joinTableList, boolean parentIsEntity) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        for (JoinTable otm : joinTableList) {
            if (!otm.isCasecade()) {
                continue;
            }
            // 入参定义为Object 兼容findPage和findList
            String pagePoAllName = "";
            IntrospectedTable introspectedTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
            FullyQualifiedJavaType joinType = new FullyQualifiedJavaType(calculateJavaType(introspectedTable));
            boolean isJoinEntity = false;
            if (this.hasPoVo) {
                // 如果没有主键就没有生成Po vo 用Entity
                if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns()) || parentIsEntity) {
                    pagePoAllName = joinType.getFullyQualifiedName();
                } else {
                    pagePoAllName = joinType.getFullyQualifiedName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
                }
            } else {
                pagePoAllName = joinType.getFullyQualifiedName();
            }
            if (!pagePoAllName.endsWith("GetVo")) {
                isJoinEntity = true;
            }
            String listVoAllName = joinType.getFullyQualifiedName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            // 如果没有主键 是无法生成GetVo的 使用entity
            if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns())) {
                listVoAllName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
            }
            FullyQualifiedJavaType pageParamType = new FullyQualifiedJavaType(pagePoAllName);
            FullyQualifiedJavaType listVoParamType = new FullyQualifiedJavaType(listVoAllName);
            interfaze.addImportedType(pageParamType);
            interfaze.addImportedType(listVoParamType);
            String pageParamName = WbfcModelGenerator.fullyQualifiedJavaType2FieldName(pageParamType);
            // 主表关联函数为getRelations 关联表为 getXXRels
            String sqlId = "batchDelete" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
            Method method = new Method(sqlId);
//            Method method = new Method("get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels");
            method.setAbstract(true);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(pageParamType, pageParamName));
            interfaze.addMethod(method);
            commentGenerator.addMethodComment(method, "级联删除" + introspectedTable.getRemarks() + "表");
            // 如果还有子关联的 就递归继续添加result和关联查询的sql
            List<JoinTable> subList = otm.getJoinTableList();
            if (ConverterUtil.isNotEmpty(subList)) {
                addDeleteRelsMethod(interfaze, subList, isJoinEntity);
            }
        }

    }

    protected void addGetRelsPoMethod(Interface interfaze) {
        if (this.hasPoVo) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
            // 入参定义为Object 兼容findPage和findList
            String pagePoAllName = "java.lang.Object";
            String listVoAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
            FullyQualifiedJavaType pageParamType = new FullyQualifiedJavaType(pagePoAllName);
            FullyQualifiedJavaType listVoParamType = new FullyQualifiedJavaType(listVoAllName);
            FullyQualifiedJavaType retClass = new FullyQualifiedJavaType("java.util.List");
            interfaze.addImportedType(retClass);
            interfaze.addImportedType(pageParamType);
            interfaze.addImportedType(listVoParamType);
            String pageParamName = WbfcModelGenerator.fullyQualifiedJavaType2FieldName(pageParamType);
            // 主表关联函数为getRelations 关联表为 getXXRels
            Method method = new Method("getRelations");
//            Method method = new Method("get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels");
            method.setAbstract(true);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(pageParamType, pageParamName));

            retClass.addTypeArgument(new FullyQualifiedJavaType(listVoAllName));
            method.setReturnType(retClass);
            interfaze.addMethod(method);
            commentGenerator.addMethodComment(method, "带关联关系的列表查询 参数Object 可为PagePo ListPo GetPo 和 Entity");
        }
    }

    /**
     * 增加使用Po来进行分页查询方法
     *
     * @param interfaze
     */
    protected void addFindPageWithPoMethod(Interface interfaze) {
        if (this.hasPoVo) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
            String pagePoAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "PagePo";
            String listVoAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
            FullyQualifiedJavaType pageParamType = new FullyQualifiedJavaType(pagePoAllName);
            FullyQualifiedJavaType listVoParamType = new FullyQualifiedJavaType(listVoAllName);
            FullyQualifiedJavaType retClass = new FullyQualifiedJavaType("java.util.List");
            interfaze.addImportedType(retClass);
            interfaze.addImportedType(pageParamType);
            interfaze.addImportedType(listVoParamType);
            String pageParamName = WbfcModelGenerator.fullyQualifiedJavaType2FieldName(pageParamType);
            Method method = new Method("findPage");
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setAbstract(true);
            method.addParameter(new Parameter(pageParamType, pageParamName));

            retClass.addTypeArgument(new FullyQualifiedJavaType(listVoAllName));
            method.setReturnType(retClass);
            interfaze.addMethod(method);
            commentGenerator.addMethodComment(method, "分页查询");
        }
    }

    /**
     * 增加使用Po来进行list查询方法
     *
     * @param interfaze
     */
    protected void addFindListWithPoMethod(Interface interfaze) {
        if (this.hasPoVo) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
            String pagePoAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "ListPo";
            String listVoAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "ListVo";
            FullyQualifiedJavaType pageParamType = new FullyQualifiedJavaType(pagePoAllName);
            FullyQualifiedJavaType listVoParamType = new FullyQualifiedJavaType(listVoAllName);
            FullyQualifiedJavaType retClass = new FullyQualifiedJavaType("java.util.List");
            interfaze.addImportedType(retClass);
            interfaze.addImportedType(pageParamType);
            interfaze.addImportedType(listVoParamType);
            String pageParamName = WbfcModelGenerator.fullyQualifiedJavaType2FieldName(pageParamType);
            Method method = new Method("findList");
            method.setAbstract(true);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(pageParamType, pageParamName));

            retClass.addTypeArgument(new FullyQualifiedJavaType(listVoAllName));
            method.setReturnType(retClass);
            interfaze.addMethod(method);
            commentGenerator.addMethodComment(method, "列表查询");
        }
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new WbfcXMLMapperGenerator(this.hasPoVo);
    }
}
