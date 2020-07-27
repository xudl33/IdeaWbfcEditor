package com.wisea.cloud.wbfceditor.generator;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class WbfcMapperGenerator extends JavaMapperGenerator {
    private static final String CRUD_DAO = "com.wisea.cloud.common.mybatis.persistence.CrudDao";
    private boolean hasPoVo = true;
    public WbfcMapperGenerator(){
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
        commentGenerator.addJavaFileComment(interfaze,  tableName+ " 表DAO", GeneratorUtil.getTableRemarks(introspectedTable));

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
