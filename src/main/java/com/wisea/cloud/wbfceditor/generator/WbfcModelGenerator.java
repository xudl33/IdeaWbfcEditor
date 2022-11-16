package com.wisea.cloud.wbfceditor.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.config.JoinTable;
import com.wisea.cloud.wbfceditor.generator.config.WbfcTableConfiguration;
import com.wisea.cloud.wbfceditor.generator.config.WbfcTableInfo;
import com.wisea.cloud.wbfceditor.generator.constants.UpdateStrategyEnum;
import com.wisea.cloud.wbfceditor.generator.entity.RelationInfo;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.rules.Rules;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getSetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.isTrue;


/**
 * Wbfc生成插件
 *
 * @author XuDL(Wisea)
 * <p>
 * 2018年9月4日 下午5:18:53
 */
public class WbfcModelGenerator extends PluginAdapter {

    private static final String DATA_ENTITY = "com.wisea.cloud.common.entity.DataEntity";
    private static final String DATA_LONG_ENTITY = "com.wisea.cloud.common.entity.DataLongEntity";
    private static final String ENTITY_PAGE = "com.wisea.cloud.model.Page";
    private static final String PAGE_PO = "com.wisea.cloud.model.po.PagePo";
    private static final String BASE_SERVICE = "com.wisea.cloud.common.service.BaseService";
    private static final String ID_SERIALIZER = "com.wisea.cloud.model.annotation.IdSerializer";
    private static final String OFFSET_DATE_TIME_DESERIALIZER = "com.wisea.cloud.model.annotation.OffsetDateTimeDeserializer";
    private static final String OFFSET_DATE_TIME_SERIALIZER = "com.wisea.cloud.model.annotation.OffsetDateTimeSerializer";
    private static final String RESULT_POJO = "com.wisea.cloud.common.pojo.ResultPoJo";
    private static final String ANNOTATION_CHECK = "com.wisea.cloud.model.annotation.Check";
    private static final String ANNOTATION_DATA_CHECK = "com.wisea.cloud.common.annotation.DataCheck";
    private static final String CONVERTER_UTIL = "com.wisea.cloud.common.util.ConverterUtil";
    private static final String SIMPLE_DELETE_PO = "com.wisea.cloud.model.po.LongIdsPo";
    private static final String GOOGLE_LIST = "com.google.common.collect.Lists";
    private static final String UTIL_LIST = "java.util.List";
    private static final String STREAM_UTILS = "com.wisea.cloud.common.util.StreamUtils";
    private boolean hasPoVo = true;
    private boolean hasController = true;
    private boolean hasService = true;
    private boolean simplePoVo = false;
    private boolean batchInsert = false;
    private boolean batchUpdate = false;
    /**
     * 更新策略 deleteInsert:先删后插 onlyUpdate:只做更新
     */
    private UpdateStrategyEnum updateStrategy = UpdateStrategyEnum.DELETE_INSERT;

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return super.clientGeneralSelectDistinctMethodGenerated(method, interfaze, introspectedTable);
    }

    private boolean batchDelete = false;
    private RelationInfo relationInfo = new RelationInfo();
    private Map<String, InnerClass> createMap = Maps.newHashMap();
    private Date createDate = null;

    public boolean isHasPoVo() {
        return hasPoVo;
    }

    public void setHasPoVo(boolean hasPoVo) {
        this.hasPoVo = hasPoVo;
    }

    public boolean isHasController() {
        return hasController;
    }

    public void setHasController(boolean hasController) {
        this.hasController = hasController;
    }

    public boolean isHasService() {
        return hasService;
    }

    public void setHasService(boolean hasService) {
        this.hasService = hasService;
    }

    public boolean isSimplePoVo() {
        return simplePoVo;
    }

    public void setSimplePoVo(boolean simplePoVo) {
        this.simplePoVo = simplePoVo;
    }

    public boolean isBatchInsert() {
        return batchInsert;
    }

    public void setBatchInsert(boolean batchInsert) {
        this.batchInsert = batchInsert;
    }

    public boolean isBatchUpdate() {
        return batchUpdate;
    }

    public void setBatchUpdate(boolean batchUpdate) {
        this.batchUpdate = batchUpdate;
    }

    public boolean isBatchDelete() {
        return batchDelete;
    }

    public void setBatchDelete(boolean batchDelete) {
        this.batchDelete = batchDelete;
    }

    public UpdateStrategyEnum getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(UpdateStrategyEnum updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private FullyQualifiedJavaType calculateJavaModelGenerators(IntrospectedTable introspectedTable) {
        Rules rules = introspectedTable.getRules();
        if (rules.generateExampleClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getExampleType());
        }

        if (rules.generatePrimaryKeyClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        }

        if (rules.generateBaseRecordClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        }

        if (rules.generateRecordWithBLOBsClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        }
        return null;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        // 设置属性
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        String tableName = tc.getTableName();
        WbfcDataTable dataTable = wbfcConfig.getDataTable(tableName);
        WbfcTableInfo tableInfo = tc.getTableInfo();
        String hasPoVal = properties.getProperty("hasPoVo");
        if (ConverterUtil.isNotEmpty(hasPoVal)) {
            this.hasPoVo = ConverterUtil.toBoolean(hasPoVal);
        }
        String hasControllerVal = properties.getProperty("hasController");
        if (ConverterUtil.isNotEmpty(hasControllerVal)) {
            this.hasController = ConverterUtil.toBoolean(hasControllerVal);
        }
        String hasServiceVal = properties.getProperty("hasService");
        if (ConverterUtil.isNotEmpty(hasServiceVal)) {
            this.hasService = ConverterUtil.toBoolean(hasServiceVal);
        }
        String hasSimplePoVo = properties.getProperty("simplePoVo");
        // 是否结构精简 全局配置<UI选项优先级<XML
        this.simplePoVo = getBooleanAttr(hasSimplePoVo, dataTable.getSimplePoVo(), tableInfo.getSimplePoVo());

        String updateStrategy = properties.getProperty("updateStrategy");
        if (ConverterUtil.isNotEmpty(updateStrategy)) {
            this.updateStrategy = UpdateStrategyEnum.valueOf(updateStrategy);
        }

        WbfcDataTable datatable = wbfcConfig.getDataTable(tableName);
        if (null != datatable) {
            // UI选项优先级<XML
            this.batchInsert = getBooleanAttr(datatable.getBatchInsert(), tableInfo.getBatchInsert());
            this.batchUpdate = getBooleanAttr(datatable.getBatchUpdate(), tableInfo.getBatchUpdate());
            this.batchDelete = getBooleanAttr(datatable.getBatchDelete(), tableInfo.getBatchDelete());
        }
        createMap.clear();
        createDate = new Date();
        Plugin plugins = context.getPlugins();

        List<GeneratedJavaFile> list = Lists.newArrayList();
        // list = introspectedTable.getGeneratedJavaFiles();
        // 替换entity
        genderEntity(introspectedTable, plugins, list);
        // 生成Po和Vo
        genderPoVo(plugins, wbfcConfig, introspectedTable, list);
        // 生成Service
        genderService(plugins, wbfcConfig, introspectedTable, list);
        // 生成Controller
        genderController(plugins, wbfcConfig, introspectedTable, list);
        return list;
    }

    /**
     * 按数组顺序(优先级)返回布尔值
     *
     * @param props
     * @return
     */
    private boolean getBooleanAttr(Object... props) {
        boolean needSet = false;
        for (Object obj : props) {
            if (ConverterUtil.isNotEmpty(obj)) {
                needSet = ConverterUtil.toBoolean(obj);
            }
        }
        return needSet;
    }

    /**
     * 生成Controller
     *
     * @param plugins
     * @param wbfcConfig
     * @param introspectedTable
     * @param list
     */
    protected void genderController(Plugin plugins, WbfcConfig wbfcConfig, IntrospectedTable introspectedTable, List<GeneratedJavaFile> list) {
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果有service 且 不是关系表 就生成Controller函数
        if (this.hasController && !tc.getTableInfo().isRelation()) {
            String tableRemarks = GeneratorUtil.getTableRemarks(introspectedTable);
            String controllerAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getControllerPackage()) + "Controller";
            FullyQualifiedJavaType beanType = new FullyQualifiedJavaType(controllerAllName);
            TopLevelClass topLevelClass = new TopLevelClass(beanType);
            String beanDoc = topLevelClass.getType().getShortName();
            topLevelClass.setVisibility(JavaVisibility.PUBLIC);

            WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
            commentGenerator.addJavaFileComment(topLevelClass, createDate, beanDoc, tableRemarks + " Controller");

            // 必要依赖
            topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
            topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestBody");
            topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMapping");
            topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMethod");
            topLevelClass.addImportedType("org.springframework.web.bind.annotation.RestController");

            // wbfc特有
            topLevelClass.addImportedType(ANNOTATION_DATA_CHECK);
            topLevelClass.addImportedType(ENTITY_PAGE);
            topLevelClass.addImportedType(RESULT_POJO);

            // swaggerui
            topLevelClass.addImportedType("io.swagger.annotations.Api");
            topLevelClass.addImportedType("io.swagger.annotations.ApiOperation");

            // controller注解
            String apiDoc = ConverterUtil.toString(tableRemarks, beanDoc);
            topLevelClass.addAnnotation("@Api(tags = \"" + apiDoc + "相关接口\")");
            topLevelClass.addAnnotation("@RequestMapping(value = \"/w/" + beanDoc.replace("Controller", "") + "\")");
            topLevelClass.addAnnotation("@RestController");

            // 增加Service
            InnerClass serviceCompilationUnit = createMap.get("service");
            // 如果Service不存在则跳过Controller
            if (null == serviceCompilationUnit) {
                return;
            }
            FullyQualifiedJavaType serivceType = serviceCompilationUnit.getType();
            topLevelClass.addImportedType(serivceType);
            Field serviceField = new Field(fullyQualifiedJavaType2FieldName(serivceType), serivceType);
            serviceField.setVisibility(JavaVisibility.PROTECTED);
            serviceField.addAnnotation("@Autowired");
            topLevelClass.addField(serviceField);

            // findPage函数
            Method pageMethod = getControllerPageMethod(topLevelClass, introspectedTable, serivceType, apiDoc);
            topLevelClass.addMethod(pageMethod);

            // findList函数
            Method listMethod = getControllerListMethod(topLevelClass, introspectedTable, serivceType, apiDoc);
            topLevelClass.addMethod(listMethod);

            // Get函数
            Method getMethod = getControllerGetMethod(topLevelClass, introspectedTable, serivceType, apiDoc);
            // 没有主键的情况下 没有get函数
            if (getMethod != null) {
                topLevelClass.addMethod(getMethod);
            }

            // 精简模式为saveOrUpdate
            if (this.simplePoVo) {
                // insert函数
                Method saveOrUpdateMethod = getControllerBaseMethod(topLevelClass, introspectedTable, serivceType, apiDoc, "saveOrUpdate");
                topLevelClass.addMethod(saveOrUpdateMethod);

                // 精简模式不支持batchInser和batchUpdate

                // delete函数
                Method deleteMethod = getControllerSimpleBatDeteleMethod(topLevelClass, introspectedTable, serivceType, apiDoc);
                // 没有主键的情况下 没有delete函数
                if (getMethod != null) {
                    topLevelClass.addMethod(deleteMethod);
                }
            } else {
                // insert函数
                Method insertMethod = getControllerBaseMethod(topLevelClass, introspectedTable, serivceType, apiDoc, "insert");
                topLevelClass.addMethod(insertMethod);

                // batchInsert函数
                if (this.batchInsert) {
                    Method batchInsertMethod = getControllerBatchBaseMethod(topLevelClass, introspectedTable, serivceType, apiDoc, "insert");
                    if (null != batchInsertMethod) {
                        topLevelClass.addMethod(batchInsertMethod);
                    }
                }

                // update函数
                Method updateMethod = getControllerBaseMethod(topLevelClass, introspectedTable, serivceType, apiDoc, "update");
                topLevelClass.addMethod(updateMethod);

                // batchUpdate函数
                if (this.batchUpdate) {
                    Method batchUpdateMethod = getControllerBatchBaseMethod(topLevelClass, introspectedTable, serivceType, apiDoc, "update");
                    if (null != batchUpdateMethod) {
                        topLevelClass.addMethod(batchUpdateMethod);
                    }
                }

                // batDelete函数
                Method batDeleteMethod = getControllerBatDeteleMethod(topLevelClass, introspectedTable, serivceType, apiDoc);
                // 没有主键的情况下 没有batdelete函数
                if (getMethod != null) {
                    topLevelClass.addMethod(batDeleteMethod);
                }
            }


            GeneratedJavaFile gjf = new GeneratedJavaFile(topLevelClass, wbfcConfig.getControllerPath(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
            list.add(gjf);
            createMap.put("controller", topLevelClass);
        }
    }

    /**
     * 创建简单的批量删除po
     *
     * @param topLevelClass     设置类
     * @param introspectedTable 表
     * @param serivceType       java类名
     * @param baseApiDoc        注释
     * @return BatDetele函数
     */
    protected Method getControllerSimpleBatDeteleMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("batDelete");
        method.addAnnotation("@ApiOperation(value = \"批量删除" + baseApiDoc + "\")");
        method.addAnnotation("@RequestMapping(value = \"batDelete\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");

        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        topLevelClass.addImportedType(RESULT_POJO);

        FullyQualifiedJavaType dpParamType = new FullyQualifiedJavaType(SIMPLE_DELETE_PO);
        topLevelClass.addImportedType(dpParamType);

        String doParamName = "po";
        // 设置入参
        Parameter methodParam = new Parameter(dpParamType, doParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(new FullyQualifiedJavaType("java.lang.Object"));
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + ".batDelete(" + doParamName + ");");

        return method;
    }


    /**
     * 创建Controller的批量删除函数
     *
     * @param topLevelClass     设置类
     * @param introspectedTable 表
     * @param serivceType       java类名
     * @param baseApiDoc        注释
     * @return
     */
    protected Method getControllerBatDeteleMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("batDelete");
        method.addAnnotation("@ApiOperation(value = \"批量删除" + baseApiDoc + "\")");
        method.addAnnotation("@RequestMapping(value = \"batDelete\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");

        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType doParamType = null;

        // 如果是PoVo 那么入参为InsertPo
        if (this.hasPoVo) {
            String poName = "BatDeletePo";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);

        String doParamName = fullyQualifiedJavaType2FieldName(doParamType);
        // 设置入参
        Parameter methodParam = new Parameter(doParamType, doParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(new FullyQualifiedJavaType("java.lang.Object"));
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + ".batDelete(" + doParamName + ");");

        return method;
    }

    /**
     * 创建Controller的基础函数(saveOrUpdate)
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param serivceType
     * @param baseApiDoc
     * @return
     */
    protected Method getControllerSaveOrUpdateMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc, String type) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method(type);

        method.addAnnotation("@ApiOperation(value = \"新增或修改" + baseApiDoc + "\")");
        method.addAnnotation("@RequestMapping(value = \"" + type + "\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");

        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType doParamType = null;

        // 一定为SaveOrUpdatePo
        String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";

        InnerClass doParam = createMap.get(poName);
        if (null == doParam) {
            return null;
        }
        doParamType = doParam.getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);

        String doParamName = fullyQualifiedJavaType2FieldName(doParamType);
        // 设置入参
        Parameter methodParam = new Parameter(doParamType, doParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(new FullyQualifiedJavaType("java.lang.Object"));
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + "." + type + "(" + doParamName + ");");

        return method;
    }

    /**
     * 创建Controller的batch基础函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param serivceType
     * @param baseApiDoc
     * @return
     */
    protected Method getControllerBatchBaseMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc, String type) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("batch" + type.substring(0, 1).toUpperCase() + type.substring(1));
        if (type.equals("insert")) {
            method.addAnnotation("@ApiOperation(value = \"批量新增" + baseApiDoc + "\")");
        } else if (type.equals("update")) {
            method.addAnnotation("@ApiOperation(value = \"批量修改" + baseApiDoc + "\")");
        }
        method.addAnnotation("@RequestMapping(value = \"" + method.getName() + "\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");

        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType doParamType = null;

        // 如果是PoVo 那么入参为InsertPo
        if (this.hasPoVo) {
            String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);
        topLevelClass.addImportedType(ANNOTATION_CHECK);

        String doParamName = fullyQualifiedJavaType2FieldName(doParamType) + "List";
        FullyQualifiedJavaType doParamListType = new FullyQualifiedJavaType(UTIL_LIST);
        doParamListType.addTypeArgument(doParamType);
        // 设置入参
        Parameter methodParam = new Parameter(doParamListType, doParamName);
        methodParam.addAnnotation("@RequestBody");
        methodParam.addAnnotation("@Check(test = \"required\", cascade = true)");
        method.addParameter(methodParam);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(new FullyQualifiedJavaType("java.lang.Object"));
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + "." + method.getName() + "(" + doParamName + ");");

        return method;
    }

    /**
     * 创建Controller的基础函数(insert or update)
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param serivceType
     * @param baseApiDoc
     * @return
     */
    protected Method getControllerBaseMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc, String type) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method(type);
        if (type.equals("insert")) {
            method.addAnnotation("@ApiOperation(value = \"新增" + baseApiDoc + "\")");
        } else if (type.equals("update")) {
            method.addAnnotation("@ApiOperation(value = \"修改" + baseApiDoc + "\")");
        } else if (type.equals("saveOrUpdate")) {
            method.addAnnotation("@ApiOperation(value = \"新增或修改" + baseApiDoc + "\")");
        }
        method.addAnnotation("@RequestMapping(value = \"" + type + "\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");

        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType doParamType = null;

        // 如果是PoVo 那么入参为InsertPo
        if (this.hasPoVo) {
            String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);

        String doParamName = fullyQualifiedJavaType2FieldName(doParamType);
        // 设置入参
        Parameter methodParam = new Parameter(doParamType, doParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(new FullyQualifiedJavaType("java.lang.Object"));
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + "." + type + "(" + doParamName + ");");

        return method;
    }

    /**
     * 创建Controller的Get函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    protected Method getControllerGetMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("get");
        method.addAnnotation("@ApiOperation(value = \"查询" + baseApiDoc + "\")");
        method.addAnnotation("@RequestMapping(value = \"get\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");
        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType getParamType = null;
        FullyQualifiedJavaType getReturnParamType = null;

        // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
        if (this.hasPoVo) {
            InnerClass doParam = createMap.get("GetPo");
            if (null == doParam) {
                return null;
            }
            getParamType = doParam.getType();
            getReturnParamType = createMap.get("GetVo").getType();
        } else {
            // 如果是entity则入参和出参都是entity
            getParamType = createMap.get("entity").getType();
            getReturnParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(getParamType);
        topLevelClass.addImportedType(getReturnParamType);

        String getParamName = fullyQualifiedJavaType2FieldName(getParamType);
        // 设置入参
        Parameter methodParam = new Parameter(getParamType, getParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        topLevelClass.addImportedType(getParamType);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(getReturnParamType);
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + ".get(" + getParamName + ");");

        return method;
    }

    /**
     * 创建Controller的查询分页函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param serivceType
     * @param baseApiDoc
     * @return
     */
    protected Method getControllerPageMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("findPage");
        method.addAnnotation("@ApiOperation(value = \"获取" + baseApiDoc + "分页列表\")");
        method.addAnnotation("@RequestMapping(value = \"page\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");
        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType pageParamType = null;
        FullyQualifiedJavaType pageReturnParamType = null;

        // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
        if (this.hasPoVo) {
            pageParamType = createMap.get("PagePo").getType();
            pageReturnParamType = createMap.get("ListVo").getType();
        } else {
            // 如果是entity则入参和出参都是entity
            pageParamType = createMap.get("entity").getType();
            pageReturnParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(ENTITY_PAGE);
        topLevelClass.addImportedType(UTIL_LIST);
        topLevelClass.addImportedType(pageParamType);
        topLevelClass.addImportedType(pageReturnParamType);

        String pageParamName = fullyQualifiedJavaType2FieldName(pageParamType);
        // 设置入参
        Parameter methodParam = new Parameter(pageParamType, pageParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        topLevelClass.addImportedType(pageParamType);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType(ENTITY_PAGE);
        returnPageType.addTypeArgument(pageReturnParamType);
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);
        method.addBodyLine("return " + serviceParamName + ".findPage(" + pageParamName + ");");

        return method;
    }

    /**
     * 创建Controller的查询函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param serivceType
     * @param baseApiDoc
     * @return
     */
    protected Method getControllerListMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType serivceType, String baseApiDoc) {
        String serviceParamName = fullyQualifiedJavaType2FieldName(serivceType);
        Method method = new Method("list");
        method.addAnnotation("@ApiOperation(value = \"获取" + baseApiDoc + "列表\")");
        method.addAnnotation("@RequestMapping(value = \"list\", method = RequestMethod.POST)");
        method.addAnnotation("@DataCheck");
        // 设置可见性
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType pageParamType = null;
        FullyQualifiedJavaType pageReturnParamType = null;

        // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
        if (this.hasPoVo) {
            pageParamType = createMap.get("ListPo").getType();
            pageReturnParamType = createMap.get("ListVo").getType();
        } else {
            // 如果是entity则入参和出参都是entity
            pageParamType = createMap.get("entity").getType();
            pageReturnParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(UTIL_LIST);
        topLevelClass.addImportedType(pageParamType);
        topLevelClass.addImportedType(pageReturnParamType);

        String pageParamName = fullyQualifiedJavaType2FieldName(pageParamType);
        // 设置入参
        Parameter methodParam = new Parameter(pageParamType, pageParamName);
        methodParam.addAnnotation("@RequestBody");
        method.addParameter(methodParam);

        topLevelClass.addImportedType(pageParamType);

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnListType = new FullyQualifiedJavaType(UTIL_LIST);
        returnListType.addTypeArgument(pageReturnParamType);
        returnType.addTypeArgument(returnListType);
        method.setReturnType(returnType);

        // 设置body
        method.addBodyLine("return " + serviceParamName + ".findList(" + pageParamName + ");");

        return method;
    }

    /**
     * 生成Service
     *
     * @param plugins
     * @param wbfcConfig
     * @param introspectedTable
     * @param list
     */
    protected void genderService(Plugin plugins, WbfcConfig wbfcConfig, IntrospectedTable introspectedTable, List<GeneratedJavaFile> list) {
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果有service 就生成Service函数
        if (this.hasService) {
            String serviceAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getServicePackage()) + "Service";
            FullyQualifiedJavaType beanType = new FullyQualifiedJavaType(serviceAllName);
            TopLevelClass topLevelClass = new TopLevelClass(beanType);

            // 必要依赖
            topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");

            String beanDoc = topLevelClass.getType().getShortName();
            topLevelClass.setVisibility(JavaVisibility.PUBLIC);
            WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
            commentGenerator.addJavaFileComment(topLevelClass, createDate, beanDoc, GeneratorUtil.getTableRemarks(introspectedTable) + " Service");

            // 增加父类
            FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(BASE_SERVICE);
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);

            // 增加Mapper
            FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
            topLevelClass.addImportedType(mapperType);
            Field mapperField = new Field(fullyQualifiedJavaType2FieldName(mapperType), mapperType);
            mapperField.setVisibility(JavaVisibility.PROTECTED);
            mapperField.addAnnotation("@Autowired");
            topLevelClass.addField(mapperField);

            // Page函数
            Method pageMethod = getServicePageMethod(topLevelClass, introspectedTable, mapperType, "查询分页列表");
            topLevelClass.addMethod(pageMethod);

            // List函数
            Method listMethod = getServiceListMethod(topLevelClass, introspectedTable, mapperType, "查询列表");
            topLevelClass.addMethod(listMethod);

            // Get函数
            Method getMethod = getServiceGetMethod(topLevelClass, introspectedTable, mapperType, "查询");
            // 没有主键的情况下 没有get函数
            if (null != getMethod) {
                topLevelClass.addMethod(getMethod);
            }

            // 如果是精简模式就是saveOrUpdate
            if (this.simplePoVo) {
                // Insert函数
                Method saveOrUpdateMethod = getServiceBaseMethod(topLevelClass, introspectedTable, mapperType, "saveOrUpdate", "新增或修改");
                topLevelClass.addMethod(saveOrUpdateMethod);

                // 精简模式不支持batchInser和batchUpdate

                // batDelete函数
                Method batDeleteMethod = getServiceSimpleBatDeleleMethod(topLevelClass, introspectedTable, mapperType, "batDelete", "批量删除");
                // 没有主键的情况下 没有批量删除方法
                if (null != batDeleteMethod) {
                    topLevelClass.addMethod(batDeleteMethod);
                }

            } else {
                // Insert函数
                Method insertMethod = getServiceBaseMethod(topLevelClass, introspectedTable, mapperType, "insert", "新增");
                topLevelClass.addMethod(insertMethod);


                // Update函数
                Method updateMethod = getServiceBaseMethod(topLevelClass, introspectedTable, mapperType, "update", "修改");
                topLevelClass.addMethod(updateMethod);


                // batchInsert函数
                if (this.batchInsert) {
                    Method batchInsertMethod = getServiceBatchBasicMethod(topLevelClass, introspectedTable, mapperType, "insert", "批量新增");
                    if (null != batchInsertMethod) {
                        topLevelClass.addMethod(batchInsertMethod);
                    }
                }

                // batchUpdate函数
                if (this.batchUpdate) {
                    Method batchUpdateMethod = getServiceBatchBasicMethod(topLevelClass, introspectedTable, mapperType, "update", "批量修改");
                    if (null != batchUpdateMethod) {
                        topLevelClass.addMethod(batchUpdateMethod);
                    }
                }

                // batDelete函数
                Method batDeleteMethod = getServiceBatDeleleMethod(topLevelClass, introspectedTable, mapperType, "batDelete", "批量删除");
                // 没有主键的情况下 没有批量删除方法
                if (null != batDeleteMethod) {
                    topLevelClass.addMethod(batDeleteMethod);
                }
            }
//
//            // 查询是否有关联关系
//            List<OneToMany> joinLst = relationInfo.getOneToManys(introspectedTable.getTableConfiguration().getTableName());
//            // 是否有关联关系
//            if (ConverterUtil.isNotEmpty(joinLst) || ((WbfcTableConfiguration) introspectedTable.getTableConfiguration()).hasRelation()) {
//                // 添加删除关系函数
//                addServiceDeleteRelations(topLevelClass, mapperType, joinLst);
//            }


            GeneratedJavaFile gjf = new GeneratedJavaFile(topLevelClass, wbfcConfig.getServicePath(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
            list.add(gjf);
            createMap.put("service", topLevelClass);
        }
    }

//    /**
//     * 添加删除关系表
//     *
//     * @param topLevelClass
//     * @param mapperType
//     * @param joinTableList
//     */
//    protected void addServiceDeleteRelations(TopLevelClass topLevelClass, FullyQualifiedJavaType mapperType, List<JoinTable> joinTableList) {
//        for (JoinTable otm : joinTableList) {
//            if (!otm.isCasecade()) {
//                continue;
//            }
//            IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
//            StringBuffer whereQud = new StringBuffer();
//            //获取级联删除的Method
//            Method relationDeleteMethod = getServiceDeleteRelationMethod(topLevelClass, joinTable, mapperType);
//            if (null != relationDeleteMethod) {
//                topLevelClass.addMethod(relationDeleteMethod);
//            }
//            // 如果还有子关联的 就递归继续添加result和关联查询的sql
//            List<JoinTable> subList = otm.getJoinTableList();
//            if (ConverterUtil.isNotEmpty(subList)) {
//                addServiceDeleteRelations(topLevelClass, mapperType, subList);
//            }
//        }
//    }

//    /**
//     * 获取级联删除的Method
//     *
//     * @param topLevelClass
//     * @param introspectedTable
//     * @param mapperType
//     * @return
//     */
//    protected Method getServiceDeleteRelationMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType) {
//        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
//        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
//        String sqlId = "batchDelete" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
//        Method retMod = new Method(sqlId);
//        String remarks = introspectedTable.getRemarks();
//        GeneratorUtil.getTableRemarks(introspectedTable);
//        commentGenerator.addMethodComment(retMod, "级联删除" + remarks + "表");
//        // insert方法参数类型
//        FullyQualifiedJavaType doParamType = null;
//        retMod.addAnnotation("@Transactional(readOnly = false)");
//        // 如果是PoVo 那么入参为xxPo
//        if (this.hasPoVo) {
//            String poName = "GetVo";
//            InnerClass doParam = createMap.get(poName);
//            if (null == doParam) {
//                doParamType = createMap.get("entity").getType();
//            } else {
//                doParamType = doParam.getType();
//            }
//        } else {
//            // 如果是entity则入参是entity
//            doParamType = createMap.get("entity").getType();
//        }
//
//        FullyQualifiedJavaType doParamListType = new FullyQualifiedJavaType(UTIL_LIST);
//        doParamListType.addTypeArgument(doParamType);
//        // 转成参数名
//        String doParamName = fullyQualifiedJavaType2FieldName(doParamType) + "List";
//        retMod.addParameter(new Parameter(doParamListType, doParamName));
//        topLevelClass.addImportedType(UTIL_LIST);
//        topLevelClass.addImportedType(doParamType);
//        retMod.addBodyLine(mapperParamName + "." + sqlId + "(" + doParamName + ");");
//        return retMod;
//    }

    /**
     * 获取批量新增的函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperType
     * @param type
     * @param javaDoc
     * @return
     */
    protected Method getServiceBatchBasicMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String type, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
        Method method = new Method("batch" + type.substring(0, 1).toUpperCase() + type.substring(1));
        // insert方法参数类型
        FullyQualifiedJavaType doParamType = null;
        // 写入数据库类型
        FullyQualifiedJavaType intoDbType = null;

        // 如果是PoVo 那么入参为xxPo
        if (this.hasPoVo) {
            String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
        }
        intoDbType = createMap.get("entity").getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);
        topLevelClass.addImportedType(GOOGLE_LIST);
        topLevelClass.addImportedType(intoDbType);
        topLevelClass.addImportedType(CONVERTER_UTIL);


        // 事务注解加入
        topLevelClass.addImportedType("org.springframework.transaction.annotation.Transactional");

        method.addAnnotation("@Transactional(readOnly = false)");

        // 转成参数名
        String doParamName = fullyQualifiedJavaType2FieldName(doParamType) + "List";
        // 写入数据库参数名
        String intoDbName = fullyQualifiedJavaType2FieldName(intoDbType);

        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType doParamListType = new FullyQualifiedJavaType(UTIL_LIST);
        doParamListType.addTypeArgument(doParamType);
        // 设置入参
        method.addParameter(new Parameter(doParamListType, doParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType("java.lang.Object");
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);

        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        method.addBodyLine("ResultPoJo<Object> result = new ResultPoJo<>();");
        method.addBodyLine("List<" + intoDbType.getShortName() + "> dbList = Lists.newArrayList();");

        method.addBodyLine("for (" + doParamType.getShortName() + " temp : " + doParamName + ") {");
        method.addBodyLine(intoDbType.getShortName() + " doTemp = new " + intoDbType.getShortName() + "();");
        method.addBodyLine("ConverterUtil.copyProperties(temp, doTemp);");
        if (type.equals("insert")) {
            method.addBodyLine("doTemp.preInsert();");
        } else if (type.equals("update")) {
            method.addBodyLine("doTemp.preUpdate();");
        }
        method.addBodyLine("dbList.add(doTemp);");
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果是一对多的 同时也要写子表
        if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
            addJoinTableList(introspectedTable, topLevelClass, type, method, tc.getJoinTableList(), "temp", "doTemp", false);
        }
        method.addBodyLine("}");
        if (type.equals("insert")) {
            method.addBodyLine(mapperParamName + ".batchInsert(dbList);");
        } else if (type.equals("update")) {
            method.addBodyLine(mapperParamName + ".batchUpdate(dbList);");
        }

        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 添加批量递归管理表处理
     *
     * @param method
     * @param list
     */
    public void addJoinTableList(IntrospectedTable parnetTable, TopLevelClass topLevelClass, String type, Method method, List<JoinTable> list, String parentAttrName, String parentEntityName, boolean parentIsEntity) {
        if (ConverterUtil.isNotEmpty(list)) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            for (JoinTable otm : list) {
                String tableName = otm.getTableName();
                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, tableName);
                // 添加关联表对应的参数
                String javaType = calculateJavaType(joinTable);
                // 为了避免取关联值取不到 带有子关联的一律都用mapper
                String serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getDaoPackage()) + "Mapper";
                // 添加到autowired
                addAutowiredFeild(topLevelClass, serviceAllName);
                // 如果没有主键 是无法生成Po VO 使用entity service函数都是po 所以直接使用mapper
//                if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns())) {
//                    serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getDaoPackage()) + "Mapper";
//                } else {
//                    serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getServicePackage()) + "Service";
//                }
//                topLevelClass.addImportedType(serviceAllName);
                //               FullyQualifiedJavaType serviceAttr = new FullyQualifiedJavaType(serviceAllName);

                String poName = javaType;
                String typeName = JavaBeansUtil.getCamelCaseString(type, true);

                boolean joinIsEntity = false;
                // 如果是PoVo 那么入参为xxPo
                if (this.hasPoVo) {
                    poName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + typeName + "Po";
                    // 如果没有主键 是无法生成Po VO 使用entity
                    if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
                        poName = javaType;
                    }
                }
                topLevelClass.addImportedType(poName);
                FullyQualifiedJavaType poType = new FullyQualifiedJavaType(poName);
                String poShortName = poType.getShortName();
                String entityShortName = new FullyQualifiedJavaType(javaType).getShortName();
                if (!poShortName.endsWith("Po")) {
                    joinIsEntity = true;
                } else {
                    topLevelClass.addImportedType(javaType);
                }
                String serviceShotName = fullyQualifiedJavaType2FieldName(new FullyQualifiedJavaType(serviceAllName));
                String paramListVar = joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                if ("oneToMany".equalsIgnoreCase(otm.getType())) {
                    paramListVar += "List";
                }
                String paramListAttr = paramListVar.substring(0, 1).toLowerCase() + paramListVar.substring(1);
                String paramName = parentAttrName + ".get" + paramListVar;

                // 如果参数不是Entity 则后面操作需要把PO换成Entity
                WbfcDataTable datatable = wbfcConfig.getDataTable(tableName);
                WbfcTableConfiguration tc = (WbfcTableConfiguration) joinTable.getTableConfiguration();
                WbfcTableInfo tableInfo = tc.getTableInfo();
                // UI选项优先级<XML
                boolean batchInsert = getBooleanAttr(datatable.getBatchInsert(), tableInfo.getBatchInsert());
                boolean batchUpdate = getBooleanAttr(datatable.getBatchUpdate(), tableInfo.getBatchUpdate());
                Map<String, String> columnMap = GeneratorUtil.convColumnsToMap(otm.getColumns());
                List<String> parJoinVarList = Lists.newArrayList();
                List<String> subJoinSetModNameList = Lists.newArrayList();
                for (String key : columnMap.keySet()) {
                    // 获取主表的关联列
                    Optional<IntrospectedColumn> parColOpt = parnetTable.getColumn(columnMap.get(key));
                    // 获取子表的关联列
                    Optional<IntrospectedColumn> subColOpt = joinTable.getColumn(key);
                    if (parColOpt.isPresent() && subColOpt.isPresent()) {
                        IntrospectedColumn parCol = parColOpt.get();
                        IntrospectedColumn subCol = subColOpt.get();
                        // 获取主表关联列Get方法
                        String parGetMethodName = getGetterMethodName(parCol.getJavaProperty(), parCol.getFullyQualifiedJavaType());
                        // 添加主表列变量
                        String parJoinVarName = parentAttrName + JavaBeansUtil.getCamelCaseString(parCol.getJavaProperty(), true);
                        parJoinVarList.add(parJoinVarName);
                        method.addBodyLine(parCol.getFullyQualifiedJavaType().getShortName() + " " + parJoinVarName + " = " + parentEntityName + "." + parGetMethodName + "();");

                        // 获取关联表Set方法
                        String subSetMethodName = getSetterMethodName(subCol.getJavaProperty());
                        subJoinSetModNameList.add(subSetMethodName);
                    }
                }

                // 子关系
                List<JoinTable> subList = otm.getJoinTableList();
                String parnetTableRemark = ConverterUtil.toString(GeneratorUtil.getTableRemarks(parnetTable), parnetTable.getTableConfiguration().getTableName());
                String joinTableRemark = ConverterUtil.toString(GeneratorUtil.getTableRemarks(joinTable), joinTable.getTableConfiguration().getTableName());
                if ("oneToMany".equalsIgnoreCase(otm.getType())) {
                    // 添加注释
                    method.addBodyLine("// 设置属性的关联关系");
                    if (joinIsEntity) {
                        method.addBodyLine("List<" + poShortName + "> " + paramListAttr + " = " + paramName + "();");
                    } else {
                        topLevelClass.addImportedType(STREAM_UTILS);
                        String paramListAttrPo = paramListAttr + "Po";
                        method.addBodyLine("List<" + poShortName + "> " + paramListAttrPo + " = " + paramName + "();");
                        method.addBodyLine("if(ConverterUtil.isNotEmpty(" + paramListAttrPo + ")) {");
                        method.addBodyLine("List<" + entityShortName + "> " + paramListAttr + " = StreamUtils.doFunc(" + paramListAttrPo + ", (k) -> {");
                        method.addBodyLine(entityShortName + " j = new " + entityShortName + "();");
                        method.addBodyLine("ConverterUtil.copyProperties(k, j);");

                        // 写入关系值
                        for (int i = 0; i < parJoinVarList.size(); i++) {
                            topLevelClass.addImportedType(STREAM_UTILS);
                            String parJoinVarName = parJoinVarList.get(i);
                            String subJoinSetModeName = subJoinSetModNameList.get(i);
                            method.addBodyLine("j." + subJoinSetModeName + "(" + parJoinVarName + ");");
                        }

                        String preType = "";
                        if (type.equals("insert")) {
                            method.addBodyLine("j.preInsert();");
                        } else {
                            switch (updateStrategy) {
                                // 先删后插
                                case DELETE_INSERT:
                                    method.addBodyLine("j.preInsert();");
                                    break;
                                case ONLY_UPDATE:
                                    // 只更新
                                    method.addBodyLine("j.preUpdate();");
                                    break;
                            }
                        }
                        method.addBodyLine("return j;");
                        method.addBodyLine("});");
                    }
                } else {
                    String paramAttrPo = paramListAttr + "Po";
                    if (joinIsEntity) {
                        method.addBodyLine(poShortName + " " + paramListAttr + " = " + paramName + "();");
                        method.addBodyLine("if(ConverterUtil.isNotEmpty(" + paramListAttr + ")) {");
                    } else {
                        method.addBodyLine(poShortName + " " + paramAttrPo + " = " + paramName + "();");
                        method.addBodyLine(entityShortName + " " + paramListAttr + " = new " + entityShortName + "();");
                        method.addBodyLine("if(ConverterUtil.isNotEmpty(" + paramAttrPo + ")) {");
                        method.addBodyLine("ConverterUtil.copyProperties(" + paramAttrPo + ", " + paramListAttr + ");");
                    }
                    if (type.equals("insert")) {
                        method.addBodyLine(paramListAttr + ".preInsert();");
                    } else {
                        switch (updateStrategy) {
                            // 先删后插
                            case DELETE_INSERT:
                                method.addBodyLine(paramListAttr + ".preInsert();");
                                break;
                            case ONLY_UPDATE:
                                // 只更新
                                method.addBodyLine(paramListAttr + ".preUpdate();");
                                break;
                        }
                    }
                    // 写入关系值
                    for (int i = 0; i < parJoinVarList.size(); i++) {
                        String parJoinVarName = parJoinVarList.get(i);
                        String subJoinSetModeName = subJoinSetModNameList.get(i);
                        method.addBodyLine(paramListAttr + "." + subJoinSetModeName + "(" + parJoinVarName + ");");
                    }
                }

                String batParam = paramListAttr;

                // 如果是一对一 或者没有batch 就使用insert|update
                if ("oneToOne".equalsIgnoreCase(otm.getType())) {
                    // 添加注释
                    method.addBodyLine("// 一对一 " + parnetTableRemark + ":" + joinTableRemark);
                    if (type.equals("update")) {
                        switch (updateStrategy) {
                            // 先删后插
                            case DELETE_INSERT:
                                // 添加注释
                                method.addBodyLine("// 先删后插" + joinTableRemark);

                                // 新增关系表数据
                                method.addBodyLine(serviceShotName + ".insert(" + paramListAttr + ");");
                                break;
                            case ONLY_UPDATE:
                                // 只更新
                                // 添加注释
                                method.addBodyLine("// 只更新" + joinTableRemark);
                                method.addBodyLine(serviceShotName + ".updateByPrimaryKeySelective(" + paramListAttr + ");");
                                break;
                        }
                    } else {
                        // 添加注释
                        method.addBodyLine("// 新增" + joinTableRemark);
                        method.addBodyLine(serviceShotName + "." + type + "(" + paramListAttr + ");");
                    }
                    if (joinIsEntity) {
                        addJoinTableList(joinTable, topLevelClass, type, method, subList, paramListAttr, paramListAttr, joinIsEntity);
                    } else {
                        String paramAttrPo = paramListAttr + "Po";
                        addJoinTableList(joinTable, topLevelClass, type, method, subList, paramAttrPo, paramListAttr, joinIsEntity);
                    }
                } else {
                    // 添加注释
                    method.addBodyLine("// 一对多 " + parnetTableRemark
                            + ":" + joinTableRemark);
                    boolean hasType = true;
                    // 一对多
                    if ("insert".equals(type) && batchInsert) {
                        // 添加注释)
                        method.addBodyLine("// 批量新增" + GeneratorUtil.getTableRemarks(joinTable));
                        method.addBodyLine(serviceShotName + ".batchInsert(" + paramListAttr + ");");
                        hasType = false;
                    } else if ("update".equals(type) && batchUpdate) {
                        switch (updateStrategy) {
                            // 先删后插
                            case DELETE_INSERT:
                                // 添加注释
                                method.addBodyLine("// 先删后插" + joinTableRemark);
                                method.addBodyLine(serviceShotName + ".batchInsert(" + paramListAttr + ");");
                                break;
                            case ONLY_UPDATE:
                                // 只更新
                                // 添加注释
                                method.addBodyLine("// 只更新" + joinTableRemark);
                                method.addBodyLine(serviceShotName + ".batchUpdate(" + paramListAttr + ");");
                                break;
                        }
                        hasType = false;
                    }
                    addLoopBaseMethod(topLevelClass, type, method, joinTable, joinIsEntity, poShortName, entityShortName, serviceShotName, paramListAttr, subList, hasType);
                }
                // 结束 if 关联关系属性是否为空的判断
                method.addBodyLine("}");
            }
        }
    }

    /**
     * 调用关联删除逻辑
     *
     * @param method
     * @param parentIsEntity
     * @param otm
     * @param wbfcConfig
     * @param joinTable
     * @param javaType
     * @param serviceShotName
     * @param paramListAttr
     * @param parJoinVarList
     * @param subJoinSetModNameList
     */
    private void relsDelete(Method method, boolean parentIsEntity, JoinTable otm, WbfcConfig wbfcConfig, IntrospectedTable joinTable, String javaType, String serviceShotName, String paramListAttr, List<String> parJoinVarList, List<String> subJoinSetModNameList) {
        String sqlId = "batchDelete" + joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
        String parametertType = "";
        FullyQualifiedJavaType joinType = new FullyQualifiedJavaType(javaType);
        if (this.hasPoVo) {
            // 如果没有主键就没有生成Po vo 用Entity
            if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
                parametertType = joinType.getFullyQualifiedName();
            } else {
                parametertType = joinType.getFullyQualifiedName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            }
        } else {
            parametertType = joinType.getFullyQualifiedName();
        }
        FullyQualifiedJavaType prarmType = new FullyQualifiedJavaType(parametertType);
        String delPo = paramListAttr + "Del";
        method.addBodyLine(prarmType.getShortName() + " " + delPo + " = new " + prarmType.getShortName() + "();");
        // 写入关系值
        for (int i = 0; i < parJoinVarList.size(); i++) {
            String parJoinVarName = parJoinVarList.get(i);
            String subJoinSetModeName = subJoinSetModNameList.get(i);
            method.addBodyLine(delPo + "." + subJoinSetModeName + "(" + parJoinVarName + ");");
        }

        String rootJoinTableName = otm.getRoot();
        if (ConverterUtil.isNotEmpty(rootJoinTableName)) {
            IntrospectedTable rootJoinTable = GeneratorUtil.getIntrospectedTable(context, rootJoinTableName);
            // 获取关联表对应的Mappername
            FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(rootJoinTable.getMyBatis3JavaMapperType());
            String mapperName = fullyQualifiedJavaType2FieldName(mapperType);
            // 添加删除关系表数据
            method.addBodyLine(mapperName + "." + sqlId + "(" + delPo + ");");
        }
    }

    /**
     * 添加带有For循环的普通递归函数
     *
     * @param topLevelClass
     * @param type
     * @param method
     * @param joinTable
     * @param joinIsEntity
     * @param poShortName
     * @param entityShortName
     * @param serviceShotName
     * @param paramListAttr
     * @param subList
     * @param hasType
     */
    private void addLoopBaseMethod(TopLevelClass topLevelClass, String type, Method method, IntrospectedTable joinTable, boolean joinIsEntity, String poShortName, String entityShortName, String serviceShotName, String paramListAttr, List<JoinTable> subList, boolean hasType) {
        // batch时如果没有子关联 就直接返回
        if (!hasType && ConverterUtil.isEmpty(subList)) {
            return;
        }

        // 添加注释
        method.addBodyLine("// 遍历关联表");
        method.addBodyLine("for (int i = 0; i < " + paramListAttr + ".size(); i++) {");
        String subEntityName = paramListAttr + "SubEty";
        String subPoName = subEntityName;
        method.addBodyLine(entityShortName + " " + subEntityName + " = " + paramListAttr + ".get(i);");
        if (hasType) {
            String runMed = type;
            if ("update".equals(type)) {
                switch (updateStrategy) {
                    // 先删后插
                    case DELETE_INSERT:
                        runMed = "insert";
                        break;
                    case ONLY_UPDATE:
                        // 只更新
                        runMed = "updateByPrimaryKeySelective";
                        break;
                }

            }
            method.addBodyLine(serviceShotName + "." + runMed + "(" + subEntityName + ");");
        }

        // 如果有子关系则递归
        if (ConverterUtil.isNotEmpty(subList)) {
            // 如果参数不是entity需要获取po
            if (!joinIsEntity) {
                String paramListAttrPo = paramListAttr + "Po";
                subPoName = paramListAttr + "SubOne";
                method.addBodyLine(poShortName + " " + subPoName + " = " + paramListAttrPo + ".get(i);");
            }
            addJoinTableList(joinTable, topLevelClass, type, method, subList, subPoName, subEntityName, joinIsEntity);
        }
        method.addBodyLine("}");
    }


//    /**
//     * 添加批量递归管理表处理
//     *
//     * @param method
//     * @param list
//     */
//    public void addBatchOneToMany(IntrospectedTable parnetTable, TopLevelClass topLevelClass, String type, Method method, List<JoinTable> list, String doParamName, boolean parentIsEntity, String parentEntityName) {
//        if (ConverterUtil.isNotEmpty(list)) {
//            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
//            for (JoinTable otm : list) {
//                if (!otm.isCasecade()) {
//                    continue;
//                }
//                String tableName = otm.getTableName();
//                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, tableName);
//                // 添加关联表对应的参数和Service
//                String javaType = calculateJavaType(joinTable);
//                String serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getServicePackage()) + "Service";
//                // 如果没有主键 是无法生成Po VO 使用entity service函数都是po 所以直接使用mapper
//                if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
//                    serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getDaoPackage()) + "Mapper";
//                }
//                topLevelClass.addImportedType(serviceAllName);
//                String poName = javaType;
//                String typeName = JavaBeansUtil.getCamelCaseString(type, true);
//                boolean joinIsEntity = false;
//                // 如果是PoVo 那么入参为xxPo
//                if (this.hasPoVo) {
//                    poName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + typeName + "Po";
//                    // 如果没有主键 是无法生成Po VO 使用entity
//                    if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
//                        poName = javaType;
//                    }
//                }
//                FullyQualifiedJavaType poType = new FullyQualifiedJavaType(poName);
//                String poShortName = poType.getShortName();
//                if (!poShortName.endsWith("Po")) {
//                    joinIsEntity = true;
//                }
//                topLevelClass.addImportedType(poName);
//                String serviceShotName = fullyQualifiedJavaType2FieldName(new FullyQualifiedJavaType(serviceAllName));
//                String paramListVar = joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
//                // 如果是一对多要加上List
//                if ("oneToMany".equalsIgnoreCase(otm.getType())) {
//                    paramListVar += "List";
//                }
//                String paramListAttr = paramListVar.substring(0, 1).toLowerCase() + paramListVar.substring(1);
//                String paramName = doParamName + ".get" + paramListVar;
//                WbfcDataTable datatable = wbfcConfig.getDataTable(tableName);
//                Map<String, String> columnMap = GeneratorUtil.convColumnsToMap(otm.getColumns());
//                List<String> parJoinVarList = Lists.newArrayList();
//                List<String> subJoinSetModNameList = Lists.newArrayList();
//                for (String key : columnMap.keySet()) {
//                    // 获取主表的关联列
//                    Optional<IntrospectedColumn> parColOpt = parnetTable.getColumn(columnMap.get(key));
//                    // 获取子表的关联列
//                    Optional<IntrospectedColumn> subColOpt = joinTable.getColumn(key);
//                    if (parColOpt.isPresent() && subColOpt.isPresent()) {
//                        IntrospectedColumn parCol = parColOpt.get();
//                        IntrospectedColumn subCol = subColOpt.get();
//                        // 获取主表关联列Get方法
//                        String parGetMethodName = getGetterMethodName(subCol.getJavaProperty(), parCol.getFullyQualifiedJavaType());
//                        // 添加主表列变量
//                        String parJoinVarName = parCol.getJavaProperty(doParamName);
//                        parJoinVarList.add(parJoinVarName);
//                        method.addBodyLine(parCol.getFullyQualifiedJavaType().getShortName() + " " + parJoinVarName + " = " + parentEntityName + "." + parGetMethodName + "()");
//
//                        // 获取关联表Set方法
//                        String subSetMethodName = getSetterMethodName(subCol.getJavaProperty());
//                        parJoinVarList.add(subSetMethodName);
//                    }
//                }
//                // 一对多
//                if ("oneToMany".equalsIgnoreCase(otm.getType())) {
//                    method.addBodyLine("List<" + poShortName + "> " + paramListAttr + " = " + paramName + "();");
//                    // 写入关系值
//                    for (int i = 0; i < parJoinVarList.size(); i++) {
//                        topLevelClass.addImportedType(STREAM_UTILS);
//                        String parJoinVarName = parJoinVarList.get(i);
//                        String subJoinSetModeName = subJoinSetModNameList.get(i);
//                        method.addBodyLine(paramListAttr + " = StreamUtils.doCum(" + paramListAttr + ",  (k) -> {k." + subJoinSetModeName + "(" + parJoinVarName + ");});");
//                    }
//
//                    // 如果参数是entity 要循环一遍调用preInsert
//                    if (joinIsEntity) {
//                        topLevelClass.addImportedType(STREAM_UTILS);
//                        String preType = "";
//                        if (type.equals("insert")) {
//                            preType = "preInserts";
//                        } else {
//                            preType = "preUpdates";
//                        }
//                        method.addBodyLine(paramListAttr + " = StreamUtils." + preType + "(" + paramListAttr + ");");
//                    }
//                    // 有批量方法就走批量
//                    if (type.equals("insert") && ConverterUtil.toBoolean(datatable.getBatchInsert())) {
//                        //  如果参数是entity 就调mapper批量函数 是PO就直接调service批量函数
//                        method.addBodyLine(serviceShotName + ".batchInsert(" + paramListAttr + ");");
//                    } else if (type.equals("update") && ConverterUtil.toBoolean(datatable.getBatchUpdate())) {
//                        //  如果参数是entity 就调mapper批量函数 是PO就直接调service批量函数
//                        method.addBodyLine(serviceShotName + ".batchUpdate(" + paramListAttr + ");");
//                    } else {
//                        // 非批量
//                        String jPoName = poShortName + "JPo";
//                        method.addBodyLine("for (" + poShortName + " " + jPoName + " : " + paramListAttr + ") {");
//                        // 如果没有主键 是无法生成Po VO 使用entity 要复制成Po
//                        if (joinIsEntity) {
//                            // 如果是entity就是mapper.insert
//                            method.addBodyLine(serviceShotName + "." + type + "(" + jPoName + ");");
//                        } else {
//                            // 如果不是entity 就需要直接调用mapper的函数 因为PO无法获取id等
//                            // 如果是po就是service.insert(po)  废弃 因为一对多的默认先
//                            String insertPo = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + typeName + "Po";
//                            FullyQualifiedJavaType jnsPoType = new FullyQualifiedJavaType(insertPo);
//                            String insPoName = fullyQualifiedJavaType2FieldName(jnsPoType) + "InsPo";
//                            method.addBodyLine(jnsPoType.getShortName() + " " + insPoName + " = new " + jnsPoType.getShortName() + "();");
//                            method.addBodyLine("ConverterUtil.copyProperties(" + jPoName + ", " + insPoName + ");");
//                            method.addBodyLine(serviceShotName + "." + type + "(" + insPoName + ");");
//                        }
//                    }
//                    // 如果有子关系则递归
//                    List<JoinTable> subList = otm.getJoinTableList();
//                    if (ConverterUtil.isNotEmpty(subList)) {
//                        String jPoName = poShortName + "JPo";
//                        // 如果参数是entity 则继续递归调用
//                        if (joinIsEntity) {
//                            addBatchOneToMany(joinTable, topLevelClass, type, method, subList, jPoName, joinIsEntity, jPoName);
//                        }
//                    }
//                    // 不是批量就会有for循环 此处为for的闭包
//                    if (!ConverterUtil.toBoolean(datatable.getBatchInsert())) {
//                        method.addBodyLine("}");
//                    }
//                } else {
//                    // 一对一
//                    method.addBodyLine(poShortName + " " + paramListAttr + " = " + paramName + "();");
//                    // 写入关系值
//                    for (int i = 0; i < parJoinVarList.size(); i++) {
//                        String parJoinVarName = parJoinVarList.get(i);
//                        String subJoinSetModeName = subJoinSetModNameList.get(i);
//                        method.addBodyLine(paramListAttr + "." + subJoinSetModeName + "(" + parJoinVarName + ");");
//                    }
//                    // 如果参数是entity 要pre一下
//                    if (joinIsEntity) {
//                        if (type.equals("insert")) {
//                            method.addBodyLine(paramListAttr + ".preInsert();");
//                        } else {
//                            method.addBodyLine(paramListAttr + ".preUpdate();");
//                        }
//                    }
//                    // 如果service就是inser(po) 如果是mapper就是insert(entity)
//                    method.addBodyLine(serviceShotName + "." + type + "(" + paramListAttr + ");");
//                    // 如果有子关系则递归
//                    List<JoinTable> subList = otm.getJoinTableList();
//                    if (ConverterUtil.isNotEmpty(subList)) {
//                        addBatchOneToMany(joinTable, topLevelClass, type, method, subList, poType, paramListAttr, joinIsEntity);
//                    }
//                }
//            }
//        }
//    }


    /**
     * 批量删除
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperType
     * @param type
     * @param javaDoc
     * @return
     */
    protected Method getServiceSimpleBatDeleleMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String type, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
        Method method = new Method(type);
        // insert方法参数类型
        FullyQualifiedJavaType doParamType = new FullyQualifiedJavaType(SIMPLE_DELETE_PO);
        // 写入数据库类型
        FullyQualifiedJavaType intoDbType = null;
        intoDbType = createMap.get("entity").getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        if (this.batchDelete) {
            topLevelClass.addImportedType(GOOGLE_LIST);
        }
        topLevelClass.addAnnotation("@Service");

        // 事务注解加入
        topLevelClass.addImportedType("org.springframework.transaction.annotation.Transactional");
        method.addAnnotation("@Transactional(readOnly = false)");

        // 转成参数名
        String doParamName = "po";
        // 写入数据库参数名
        String intoDbName = fullyQualifiedJavaType2FieldName(intoDbType);
        topLevelClass.addImportedType(doParamType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(doParamType, doParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType("java.lang.Object");
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);

        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        method.addBodyLine("ResultPoJo<Object> result = new ResultPoJo<>();");
        // 上面创建Po时进行过判断 若没有主键 不会进入这里，一定有一个主键
        IntrospectedColumn priColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        // 主键类型
        FullyQualifiedJavaType priType = priColumn.getFullyQualifiedJavaType();
        // 获取主键Set方法
        String setMethodName = getSetterMethodName(priColumn.getJavaProperty());
        // Po需要进行转换
        topLevelClass.addImportedType(intoDbType);

        String doDelList = doParamName + ".getIds()";
        // 循环列表
        if (this.batchDelete) {
            method.addBodyLine("List<" + intoDbType.getShortName() + "> delDbList = Lists.newArrayList();");
        }
        method.addBodyLine("for(" + priType.getShortName() + " delId : " + doDelList + ") {");
        Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
        // 包含删除标识时 生成逻辑删除
        if (delColumn.isPresent()) {
            // 不包含删除标识生成物理删除
            method.addBodyLine(intoDbType.getShortName() + " " + intoDbName + " = new " + intoDbType.getShortName() + "();");
            method.addBodyLine(intoDbName + "." + setMethodName + "(delId);");
            method.addBodyLine(intoDbName + ".preUpdate();");
            if (!this.batchDelete) {
                method.addBodyLine(mapperParamName + ".deleteLogic(" + intoDbName + ");");
            } else {
                method.addBodyLine("delDbList.add(" + intoDbName + ");");
            }
        } else {
            if (!this.batchDelete) {
                method.addBodyLine(mapperParamName + ".deleteByPrimaryKey(delId);");
            } else {
                method.addBodyLine("delDbList.add(" + intoDbName + ");");
            }
        }
        method.addBodyLine("}");
        if (this.batchDelete) {
            method.addBodyLine(mapperParamName + ".batchDelete(delDbList);");
        }
        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 批量删除
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperType
     * @param type
     * @param javaDoc
     * @return
     */
    protected Method getServiceBatDeleleMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String type, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        Method method = new Method(type);
        // insert方法参数类型
        FullyQualifiedJavaType doParamType = null;
        // 写入数据库类型
        FullyQualifiedJavaType intoDbType = null;

        // 如果是PoVo 那么入参为InsertPo
        if (this.hasPoVo) {
            String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";
            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            doParamType = new FullyQualifiedJavaType(UTIL_LIST);
            // 批量删除的入参是List<entity>
            topLevelClass.addImportedType(createMap.get("entity").getType());
            doParamType.addTypeArgument(createMap.get("entity").getType());
        }
        intoDbType = createMap.get("entity").getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        if (this.batchDelete) {
            topLevelClass.addImportedType(GOOGLE_LIST);
        }
        topLevelClass.addAnnotation("@Service");

        // 事务注解加入
        topLevelClass.addImportedType("org.springframework.transaction.annotation.Transactional");
        method.addAnnotation("@Transactional(readOnly = false)");

        // 转成参数名
        String doParamName = null;
        if (this.hasPoVo) {
            doParamName = fullyQualifiedJavaType2FieldName(doParamType);
        } else {
            doParamName = "list";
        }
        // 写入数据库参数名
        String intoDbName = fullyQualifiedJavaType2FieldName(intoDbType);
        topLevelClass.addImportedType(doParamType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(doParamType, doParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType("java.lang.Object");
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);

        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        method.addBodyLine("ResultPoJo<Object> result = new ResultPoJo<>();");
        // 上面创建Po时进行过判断 若没有主键 不会进入这里，一定有一个主键
        IntrospectedColumn priColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        // 主键类型
        FullyQualifiedJavaType priType = priColumn.getFullyQualifiedJavaType();
        // 获取主键Set方法
        String setMethodName = getSetterMethodName(priColumn.getJavaProperty());
        if (this.batchDelete) {
            method.addBodyLine("List<" + intoDbType.getShortName() + "> delDbList = Lists.newArrayList();");
        }
        Method delRelationsMod = null;
        // 如果是有关联关系的 需要添加删除关联关系的函数
        if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
            delRelationsMod = new Method("delRelations");
            delRelationsMod.addAnnotation("@Transactional(readOnly = false)");
            // 设置方法注释
            commentGenerator.addMethodComment(delRelationsMod, "级联删除关联子表的数据");
            FullyQualifiedJavaType delParam = createMap.get("entity").getType();
            // 设置入参
            delRelationsMod.addParameter(new Parameter(delParam, intoDbName));
            // 生成一个给update函数调用的方法
            addDeleteOneToMany(topLevelClass, introspectedTable, mapperParamName, delRelationsMod, intoDbType, intoDbName);
            topLevelClass.addMethod(delRelationsMod);
        }
        // 如果是PoVo则调用findPage
        if (this.hasPoVo) {
            // Po需要进行转换
            topLevelClass.addImportedType(intoDbType);
            String doDelList = doParamName + ".getDelList()";
            // 循环列表
            method.addBodyLine("for(" + priType.getShortName() + " delId : " + doDelList + ") {");
            Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
            // 包含删除标识时 生成逻辑删除
            if (delColumn.isPresent()) {
                // 不包含删除标识生成物理删除
                method.addBodyLine(intoDbType.getShortName() + " " + intoDbName + " = new " + intoDbType.getShortName() + "();");
                method.addBodyLine(intoDbName + "." + setMethodName + "(delId);");
                // 如果是一对多的 同时也要删除子表
                if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                    // 添加到delete函数
                    addDeleteOneToMany(topLevelClass, introspectedTable, mapperParamName, method, intoDbType, intoDbName);
                }
                method.addBodyLine(intoDbName + ".preUpdate();");
                if (!this.batchDelete) {
                    method.addBodyLine(mapperParamName + ".deleteLogic(" + intoDbName + ");");
                } else {
                    method.addBodyLine("delDbList.add(" + intoDbName + ");");
                }
            } else {
                // 如果是一对多的 同时也要删除子表
                if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                    addDeleteOneToMany(topLevelClass, introspectedTable, mapperParamName, method, intoDbType, intoDbName);
                }
                if (!this.batchDelete) {
                    method.addBodyLine(mapperParamName + ".deleteByPrimaryKey(delId);");
                } else {
                    method.addBodyLine("delDbList.add(" + intoDbName + ");");
                }
            }
            method.addBodyLine("}");
            if (this.batchDelete) {
                method.addBodyLine(mapperParamName + ".batchDelete(delDbList);");
            }
        } else {
            // 入参即为List<entity>
            // 循环列表
            method.addBodyLine("for(" + intoDbType.getShortName() + " " + intoDbName + " : " + doParamName + ") {");
            // 如果是一对多的 同时也要删除子表
            if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                addDeleteOneToMany(topLevelClass, introspectedTable, mapperParamName, method, intoDbType, intoDbName);
            }
            method.addBodyLine(intoDbName + ".preUpdate();");
            Optional<IntrospectedColumn> delColumn = introspectedTable.getColumn("del_flag");
            String getMethodName = getGetterMethodName(priColumn.getJavaProperty(), priType);
            // 包含删除标识时 生成逻辑删除
            if (delColumn.isPresent()) {
                if (!this.batchDelete) {
                    method.addBodyLine(mapperParamName + ".deleteLogic(" + intoDbName + ");");
                } else {
                    method.addBodyLine("delDbList.add(" + intoDbName + ");");
                }
            } else {
                // 不包含删除标识生成物理删除
                if (!this.batchDelete) {
                    method.addBodyLine(mapperParamName + ".deleteByPrimaryKey(" + intoDbName + "." + getMethodName + "());");
                } else {
                    method.addBodyLine("delDbList.add(" + intoDbName + ");");
                }
            }
            method.addBodyLine("}");
            if (this.batchDelete) {
                method.addBodyLine(mapperParamName + ".batchDelete(delDbList);");
            }
        }

        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 添加批量删除递归管理主表处理
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperParamName
     * @param method
     * @param intoDbType
     * @param intoDbName
     */
    public void addDeleteOneToMany(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String mapperParamName, Method method, FullyQualifiedJavaType intoDbType, String intoDbName) {
        String poName = intoDbType.getShortName();
        if (this.hasPoVo) {
            poName += "ListVo";
        }
        method.addBodyLine("List<" + poName + "> list = " + mapperParamName + ".getRelations(" + intoDbName + ");");
        method.addBodyLine("if (ConverterUtil.isNotEmpty(list)) {");
        String doParamName = "delObj";
        method.addBodyLine(poName + " " + doParamName + " = list.get(0);");
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        boolean isParentEntity = ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns());
        addSubDeleteOneToMany(topLevelClass, introspectedTable, mapperParamName, method, tc.getJoinTableList(), doParamName, isParentEntity);
//        if (ConverterUtil.isNotEmpty(res)) {
//            method.addBodyLines(Lists.newArrayList(res.split("\r\n")));
//        }
        method.addBodyLine("}");
    }

    /**
     * 添加批量删除递归管理子表处理
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperParamName
     * @param method
     * @param list
     * @param doParamName
     */
    public void addSubDeleteOneToMany(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String mapperParamName, Method method, List<JoinTable> list, String doParamName, boolean parentIsEntity) {
        if (ConverterUtil.isNotEmpty(list)) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            for (JoinTable otm : list) {
                if (!otm.isCasecade()) {
                    continue;
                }
                boolean isJoinEntity = false;
                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, otm.getTableName());
                FullyQualifiedJavaType doJoinType = null;
                FullyQualifiedJavaType tempType = new FullyQualifiedJavaType(calculateJavaType(joinTable));
                if (this.hasPoVo) {
                    String poName = tempType.getFullyQualifiedName().replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
                    // 如果没有主键 是无法生成GetVo的 使用entity
                    if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
                        doJoinType = tempType;
                    } else {
                        doJoinType = new FullyQualifiedJavaType(poName);
                    }
                } else {
                    // 如果是entity则入参是entity
                    doJoinType = tempType;
                }
                if (!doJoinType.getShortName().endsWith("GetVo")) {
                    isJoinEntity = true;
                }
                topLevelClass.addImportedType(doJoinType);
                String joinGetAttr = joinTable.getTableConfiguration().getDomainObjectName() + "RelsList";
                if ("oneToOne".equalsIgnoreCase(otm.getType())) {
                    joinGetAttr = joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                }
                joinGetAttr = joinGetAttr.substring(0, 1).toLowerCase() + joinGetAttr.substring(1);
                String joinGetMod = getGetterMethodName(joinGetAttr, doJoinType);
                String paramName = doJoinType.getShortName();
                if ("oneToMany".equalsIgnoreCase(otm.getType())) {
                    method.addBodyLine("List<" + doJoinType.getShortName() + "> " + joinGetAttr + " = " + doParamName + "." + joinGetMod + "();");
                    method.addBodyLine("if (ConverterUtil.isNotEmpty(" + joinGetAttr + ")) {");
                    paramName += "Po";
                    paramName = paramName.substring(0, 1).toLowerCase() + paramName.substring(1);
                    method.addBodyLine(doJoinType.getShortName() + " " + paramName + " = " + joinGetAttr + ".get(0);");
                } else {
                    method.addBodyLine("" + doJoinType.getShortName() + " " + joinGetAttr + " = " + doParamName + "." + joinGetMod + "();");
                    paramName = joinGetAttr;
                    paramName = paramName.substring(0, 1).toLowerCase() + paramName.substring(1);
                    method.addBodyLine("if (ConverterUtil.isNotEmpty(" + joinGetAttr + ")) {");
                }

                if (isJoinEntity) {
                    method.addBodyLine(paramName + ".preUpdate();");
                }
                // 如果有子关系则递归
                List<JoinTable> subList = otm.getJoinTableList();
                if (ConverterUtil.isNotEmpty(subList)) {
                    for (JoinTable subOtm : subList) {
                        IntrospectedTable subJoinTable = GeneratorUtil.getIntrospectedTable(context, subOtm.getTableName());
                        addSubDeleteOneToMany(topLevelClass, subJoinTable, mapperParamName, method, subList, paramName, isJoinEntity);
                    }
                }
                String sqlId = "batchDelete" + joinTable.getTableConfiguration().getDomainObjectName() + "Rels";
                method.addBodyLine(mapperParamName + "." + sqlId + "(" + paramName + ");");
                method.addBodyLine("}");
            }
        }
    }

    /**
     * 创建insert函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    protected Method getServiceBaseMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String type, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
        Method method = new Method(type);
        // insert方法参数类型
        FullyQualifiedJavaType doParamType = null;
        // 写入数据库类型
        FullyQualifiedJavaType intoDbType = null;

        // 如果是PoVo 那么入参为xxPo
        if (this.hasPoVo) {
            String poName = type.substring(0, 1).toUpperCase() + type.substring(1) + "Po";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
        }
        intoDbType = createMap.get("entity").getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);

        // 事务注解加入
        topLevelClass.addImportedType("org.springframework.transaction.annotation.Transactional");
        method.addAnnotation("@Transactional(readOnly = false)");

        // 转成参数名
        String doParamName = fullyQualifiedJavaType2FieldName(doParamType);
        // 写入数据库参数名
        String intoDbName = fullyQualifiedJavaType2FieldName(intoDbType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(doParamType, doParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType("java.lang.Object");
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);

        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果有关系就需要autowire对应的service
        if (tc.hasRelation()) {
            // 添加一对多对应Mapper
            addAutowiredService(topLevelClass, tc.getJoinTableList(), false);
        }

        method.addBodyLine("ResultPoJo<Object> result = new ResultPoJo<>();");

        // 如果是PoVo则需要属性拷贝
        if (this.hasPoVo) {
            topLevelClass.addImportedType(intoDbType);
            topLevelClass.addImportedType(CONVERTER_UTIL);
            List<IntrospectedColumn> columnList = introspectedTable.getPrimaryKeyColumns();

            if (this.simplePoVo && ConverterUtil.isNotEmpty(columnList)) {
                IntrospectedColumn priColumn = introspectedTable.getPrimaryKeyColumns().get(0);
                String idGetMethodName = getGetterMethodName(priColumn.getJavaProperty(), priColumn.getFullyQualifiedJavaType());
                method.addBodyLine(intoDbType.getShortName() + " " + intoDbName + " = new " + intoDbType.getShortName() + "();");
                // Po需要进行转换
                method.addBodyLine("ConverterUtil.copyProperties(" + doParamName + ", " + intoDbName + ");");
                method.addBodyLine("if (ConverterUtil.isEmpty(" + doParamName + "." + idGetMethodName + "())) {");
                method.addBodyLine(intoDbName + ".preInsert();");
                method.addBodyLine(mapperParamName + ".insert(" + intoDbName + ");");
                // 如果是一对多的 同时也要写子表
                if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                    addJoinTableList(introspectedTable, topLevelClass, "insert", method, tc.getJoinTableList(), doParamName, intoDbName, false);
                }
                method.addBodyLine("} else {");
                method.addBodyLine(intoDbName + ".preUpdate();");
                method.addBodyLine(mapperParamName + ".updateByPrimaryKeySelective(" + intoDbName + ");");
                // 如果是一对多的 同时也要写子表
                if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                    addJoinTableList(introspectedTable, topLevelClass, "update", method, tc.getJoinTableList(), doParamName, intoDbName, false);
                }
                method.addBodyLine("}");
            } else {
                method.addBodyLine(intoDbType.getShortName() + " " + intoDbName + " = new " + intoDbType.getShortName() + "();");
                // Po需要进行转换
                method.addBodyLine("ConverterUtil.copyProperties(" + doParamName + ", " + intoDbName + ");");
            }
        } else {
            // 入参即为entity
            intoDbName = doParamName;
        }
        if (type.equals("insert")) {
            method.addBodyLine(intoDbName + ".preInsert();");
            method.addBodyLine(mapperParamName + ".insert(" + intoDbName + ");");
        } else if (type.equals("update")) {
            method.addBodyLine(intoDbName + ".preUpdate();");
            method.addBodyLine(mapperParamName + ".updateByPrimaryKeySelective(" + intoDbName + ");");
        }

        if (!type.equals("saveOrUpdate")) {
            // 如果是一对多的 同时也要写子表
            if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
                if ("update".equals(type)) {
                    // 如果是先删后插要先做 放到if 为空判断外面
                    switch (updateStrategy) {
                        // 先删后插
                        case DELETE_INSERT:
                            method.addBodyLine("// 先删后插 删除所有关系表");
                            method.addBodyLine("delRelations(" + intoDbName + ");");
                            break;
                    }
                }
                addJoinTableList(introspectedTable, topLevelClass, type, method, tc.getJoinTableList(), doParamName, intoDbName, false);
            }
        }

        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 增加一个AutoWired的属性
     * <br/>
     * 如果已经存在则跳过
     *
     * @param topLevelClass
     * @param fieldPackageName
     */
    public void addAutowiredFeild(TopLevelClass topLevelClass, String fieldPackageName) {
        addAutowiredFeildWithName(topLevelClass, fieldPackageName, null);
    }

    /**
     * 增加一个AutoWired的属性
     * <br/>
     * 如果已经存在则跳过
     *
     * @param topLevelClass
     * @param fieldPackageName
     * @param fieldName
     */
    public void addAutowiredFeildWithName(TopLevelClass topLevelClass, String fieldPackageName, String fieldName) {
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        // 获取全部属性的classset
        Set<String> serviceSet = topLevelClass.getFields().stream().map(f -> f.getType().getFullyQualifiedName()).collect(Collectors.toSet());
        if (!serviceSet.contains(fieldPackageName)) {
            FullyQualifiedJavaType serviceAttr = new FullyQualifiedJavaType(fieldPackageName);
            Field mapperField = new Field(ConverterUtil.toNotNullString(fieldName, fullyQualifiedJavaType2FieldName(serviceAttr)), serviceAttr);
            mapperField.setVisibility(JavaVisibility.PROTECTED);
            mapperField.addAnnotation("@Autowired");
            topLevelClass.addField(mapperField);
            topLevelClass.addImportedType(serviceAttr);
        }
    }

    /**
     * 添加关联关系的Service到Autowired
     *
     * @param topLevelClass
     * @param list
     */
    public void addAutowiredService(TopLevelClass topLevelClass, List<JoinTable> list, boolean parentIsEntity) {
        if (ConverterUtil.isEmpty(list)) {
            return;
        }
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
        for (JoinTable otm : list) {
            String tableName = otm.getTableName();
            IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, tableName);
            // 添加关联表对应的参数和Service
            String javaType = calculateJavaType(joinTable);
            String serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getServicePackage()) + "Service";
            boolean isJoinEntity = false;
            // 如果没有主键 是无法生成Po VO 使用entity service函数都是po 所以直接使用mapper
            if (ConverterUtil.isEmpty(joinTable.getPrimaryKeyColumns()) || parentIsEntity) {
                serviceAllName = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getDaoPackage()) + "Mapper";
            }
            if (serviceAllName.endsWith("Mapper")) {
                isJoinEntity = true;
            }
            // 添加到autowired
            addAutowiredFeild(topLevelClass, serviceAllName);
            // 如果有子关系则递归
            List<JoinTable> subList = otm.getJoinTableList();
            if (ConverterUtil.isNotEmpty(subList)) {
                addAutowiredService(topLevelClass, subList, isJoinEntity);
            }
        }
    }

    /**
     * 创建insert函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    protected Method getServiceListMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);
        Method method = new Method("findList");
        // insert方法参数类型
        FullyQualifiedJavaType doParamType = null;
        // 返回的数据库类型
        FullyQualifiedJavaType retrunType = null;

        // 如果是PoVo 那么入参为InsertPo
        if (this.hasPoVo) {
            String poName = "ListPo";

            InnerClass doParam = createMap.get(poName);
            if (null == doParam) {
                return null;
            }
            doParamType = doParam.getType();
            retrunType = createMap.get("ListVo").getType();
        } else {
            // 如果是entity则入参是entity
            doParamType = createMap.get("entity").getType();
            retrunType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(doParamType);
        topLevelClass.addImportedType(retrunType);

        // 转成参数名
        String doParamName = fullyQualifiedJavaType2FieldName(doParamType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(doParamType, doParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnListType = new FullyQualifiedJavaType(UTIL_LIST);
        returnListType.addTypeArgument(retrunType);
        returnType.addTypeArgument(returnListType);
        method.setReturnType(returnType);

        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        method.addBodyLine("ResultPoJo<List<" + retrunType.getShortName() + ">> result = new ResultPoJo<>();");
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果是一对多的查询 换成getXXXXRels
        if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
            //            String mdName = "get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
            // 如果是主表 关联函数为getRelations
            String mdName = "getRelations";
            method.addBodyLine("result.setResult(" + mapperParamName + "." + mdName + "(" + doParamName + "));");
        } else {
            method.addBodyLine("result.setResult(" + mapperParamName + ".findList(" + doParamName + "));");
        }
        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 创建Service的Get函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param mapperType
     * @param javaDoc
     * @return
     */
    protected Method getServiceGetMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        Method method = new Method("get");
        FullyQualifiedJavaType getParamType = null;
        FullyQualifiedJavaType getReturnParamType = null;
        FullyQualifiedJavaType entityType = null;

        // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
        if (this.hasPoVo) {
            InnerClass doParam = createMap.get("GetPo");
            if (null == doParam) {
                return null;
            }
            getParamType = doParam.getType();
            getReturnParamType = createMap.get("GetVo").getType();
        } else {
            // 如果是entity则入参和出参都是entity
            getParamType = createMap.get("entity").getType();
            getReturnParamType = createMap.get("entity").getType();
        }
        entityType = createMap.get("entity").getType();

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(getParamType);
        topLevelClass.addImportedType(getReturnParamType);

        String getParamName = fullyQualifiedJavaType2FieldName(getParamType);
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);

        topLevelClass.addImportedType(getParamType);
        topLevelClass.addImportedType(getReturnParamType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(getParamType, getParamName));

        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        returnType.addTypeArgument(getReturnParamType);
        method.setReturnType(returnType);
        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);

        method.addBodyLine("ResultPoJo<" + getReturnParamType.getShortName() + "> result = new ResultPoJo<>();");

        // 上面创建Po时进行过判断 若没有主键 不会进入这里，一定有一个主键
        IntrospectedColumn priColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        String idGetMethodName = getGetterMethodName(priColumn.getJavaProperty(), priColumn.getFullyQualifiedJavaType());
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        topLevelClass.addImportedType(CONVERTER_UTIL);
        // 如果是一对多的查询 换成getXXXXRels
        if (tc.hasRelation()) {
//            String mdName = "get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
            //            String mdName = "get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
            // 如果是主表 关联函数为getRelations
            String mdName = "getRelations";
            FullyQualifiedJavaType poName = null;
            // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
            if (this.hasPoVo) {
                poName = createMap.get("ListVo").getType();
            } else {
                // 如果是entity则入参和出参都是entity
                poName = createMap.get("entity").getType();
            }
            method.addBodyLine("List<" + poName.getShortName() + "> list = " + mapperParamName + "." + mdName + "(" + getParamName + ");");
            method.addBodyLine("if (ConverterUtil.isNotEmpty(list)) {");
            method.addBodyLine(poName.getShortName() + " entity = list.get(0);");
            method.addBodyLine(getReturnParamType.getShortName() + " vo = new " + getReturnParamType.getShortName() + "();");
            method.addBodyLine("ConverterUtil.copyProperties(entity, vo);");
            method.addBodyLine("result.setResult(vo);");
            method.addBodyLine("}");
        } else {
            // get时使用的是默认的selectByPrimaryKey函数 返回的是entity 所以需要一下转型
            method.addBodyLine(entityType.getShortName() + " entity = " + mapperParamName + ".selectByPrimaryKey(" + getParamName + "." + idGetMethodName + "());");
            method.addBodyLine(getReturnParamType.getShortName() + " vo = new " + getReturnParamType.getShortName() + "();");
            method.addBodyLine("ConverterUtil.copyProperties(entity, vo);");
            method.addBodyLine("result.setResult(vo);");
        }

        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 创建翻页函数
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    protected Method getServicePageMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, FullyQualifiedJavaType mapperType, String javaDoc) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        Method method = new Method("findPage");
        FullyQualifiedJavaType pageParamType = null;
        FullyQualifiedJavaType pageReturnParamType = null;

        // 如果是PoVo 那么入参和返回值分别要替换成PagePo和ListVo
        if (this.hasPoVo) {
            pageParamType = createMap.get("PagePo").getType();
            pageReturnParamType = createMap.get("ListVo").getType();
        } else {
            // 如果是entity则入参和出参都是entity
            pageParamType = createMap.get("entity").getType();
            pageReturnParamType = createMap.get("entity").getType();
        }

        topLevelClass.addImportedType(RESULT_POJO);
        topLevelClass.addImportedType(ENTITY_PAGE);
        topLevelClass.addImportedType(UTIL_LIST);
        topLevelClass.addImportedType(pageParamType);
        topLevelClass.addImportedType(pageReturnParamType);

        String pageParamName = fullyQualifiedJavaType2FieldName(pageParamType);
        String mapperParamName = fullyQualifiedJavaType2FieldName(mapperType);

        topLevelClass.addImportedType(pageParamType);
        topLevelClass.addImportedType(pageReturnParamType);

        method.setVisibility(JavaVisibility.PUBLIC);
        // 设置入参
        method.addParameter(new Parameter(pageParamType, pageParamName));
        // 设置返回值
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(RESULT_POJO);
        FullyQualifiedJavaType returnPageType = new FullyQualifiedJavaType(ENTITY_PAGE);
        returnPageType.addTypeArgument(pageReturnParamType);
        returnType.addTypeArgument(returnPageType);
        method.setReturnType(returnType);
        // 设置方法注释
        commentGenerator.addMethodComment(method, javaDoc);
        method.addBodyLine("ResultPoJo<Page<" + pageReturnParamType.getShortName() + ">> result = new ResultPoJo<>();");
        method.addBodyLine("Page<" + pageReturnParamType.getShortName() + "> page = " + pageParamName + ".getPage();");
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 如果是一对多的查询 换成getXXXXRels
        if (ConverterUtil.isNotEmpty(tc.getJoinTableList())) {
//            String mdName = "get" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
            // 如果是主表 关联函数为getRelations
            String mdName = "getRelations";
            method.addBodyLine("List<" + pageReturnParamType.getShortName() + "> list = " + mapperParamName + "." + mdName + "(" + pageParamName + ");");
        } else {
            // 如果是PoVo则调用findPage
            if (this.hasPoVo) {
                method.addBodyLine("List<" + pageReturnParamType.getShortName() + "> list = " + mapperParamName + ".findPage(" + pageParamName + ");");
            } else {
                // 如果是entity则调用findList
                method.addBodyLine("List<" + pageReturnParamType.getShortName() + "> list = " + mapperParamName + ".findList(" + pageParamName + ");");
            }
        }
        method.addBodyLine("page.setList(list);");
        method.addBodyLine("result.setResult(page);");
        method.addBodyLine("return result;");
        return method;
    }

    /**
     * 生成Po和Vo
     *
     * @param plugins
     * @param wbfcConfig
     * @param introspectedTable
     * @param list
     */
    protected void genderPoVo(Plugin plugins, WbfcConfig wbfcConfig, IntrospectedTable introspectedTable, List<GeneratedJavaFile> list) {
        if (this.hasPoVo) {
            // 列表和获取的Vo
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "ListVo", list);
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "GetVo", list);
            // 翻页 获取 列表 新增 更新 批量删除
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "PagePo", list);
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "GetPo", list);
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "ListPo", list);
            if (this.simplePoVo) {
                createPoOrVo(plugins, wbfcConfig, introspectedTable, "SaveOrUpdatePo", list);
            } else {
                createPoOrVo(plugins, wbfcConfig, introspectedTable, "InsertPo", list);
                createPoOrVo(plugins, wbfcConfig, introspectedTable, "UpdatePo", list);
            }
            createPoOrVo(plugins, wbfcConfig, introspectedTable, "BatDeletePo", list);
        }
    }

    /**
     * 创建一个Bean(Po或Vo)
     *
     * @param plugins
     * @param wbfcConfig
     * @param introspectedTable
     * @param type
     * @param list
     */
    protected void createPoOrVo(Plugin plugins, WbfcConfig wbfcConfig, IntrospectedTable introspectedTable, String type, List<GeneratedJavaFile> list) {
        String typeBasePackagte = null;
        if (type.endsWith("Po")) {
            typeBasePackagte = wbfcConfig.getPoPackage();
        } else {
            typeBasePackagte = wbfcConfig.getVoPackage();
        }

        String beanAllName = introspectedTable.getBaseRecordType().replace(wbfcConfig.getEntityPackage(), typeBasePackagte) + type;
        FullyQualifiedJavaType beanType = new FullyQualifiedJavaType(beanAllName);
        TopLevelClass topLevelClass = new TopLevelClass(beanType);
        String beanName = topLevelClass.getType().getShortName();
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // topLevelClass.addImportedType(createMap.get("entityI").getType());
        // topLevelClass.addSuperInterface(createMap.get("entityI").getType());
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        commentGenerator.addJavaFileComment(topLevelClass, createDate, beanName);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass(introspectedTable);

        // 构造函数
        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable);
            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass, introspectedTable);
            }
        }

        // 是否为PagePo
        if (type.endsWith("PagePo")) {
            FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(PAGE_PO);
            superClass.addTypeArgument(createMap.get("ListVo").getType());
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
            makeSerializable(topLevelClass);
        }

        // 增加SwaggerUI import
        topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
        // 增加Check import
        if (type.endsWith("Po")) {
            topLevelClass.addImportedType(ANNOTATION_CHECK);
        }
        // 是否为BatDeletePo
        if (type.endsWith("BatDeletePo")) {
            topLevelClass.addImportedType(UTIL_LIST);
            List<IntrospectedColumn> priList = Lists.newArrayList();
            // 生成属性和方法
            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                // 是主键
                if (isPrimaryKeyColumn(introspectedTable, introspectedColumn)) {
                    priList.add(introspectedColumn);
                }
            }
            // 如果没有主键就不生成BatDeletePo了
            if (priList.size() <= 0) {
                return;
            }

            // 如果只有一个主键就直接使用类型作为泛型
            FullyQualifiedJavaType paramType = new FullyQualifiedJavaType(UTIL_LIST);
            // 只可能有一个主键的，多个主键会被拆分成entity和entityKey 因此没有多个的情况了
            FullyQualifiedJavaType paramArg = priList.get(0).getFullyQualifiedJavaType();
            // if (priList.size() == 1) {
            // paramArg = priList.get(0).getFullyQualifiedJavaType();
            // } else {
            // // 大于一个主键时 创建一个内部类作为泛型
            // InnerClass innerClass = new InnerClass(beanName.replace("BatDeletePo", "BatDeleteParam"));
            // for (IntrospectedColumn introspectedColumn : priList) {
            // String swaggerAnnStr = ConverterUtil.toString(introspectedColumn.getRemarks(), introspectedColumn.getJavaProperty());
            // Field field = new Field();
            // field.setVisibility(JavaVisibility.PRIVATE);
            // field.setType(introspectedColumn.getFullyQualifiedJavaType());
            // field.setName(introspectedColumn.getJavaProperty());
            // field.addAnnotation("@Check(test = \"required\")");
            // field.addAnnotation("@ApiModelProperty(value = \"" + swaggerAnnStr + "\")");
            // commentGenerator.addFieldComment(field, introspectedTable);
            // Method methodGet = getJavaBeansGetter(introspectedTable, introspectedColumn);
            // Method methodSet = getJavaBeansSetter(introspectedTable, introspectedColumn);
            // innerClass.addField(field);
            // innerClass.addMethod(methodGet);
            // innerClass.addMethod(methodSet);
            // }
            // topLevelClass.addInnerClass(innerClass);
            // paramArg = innerClass.getType();
            // }
            paramType.addTypeArgument(paramArg);

            String paramName = "delList";
            Field field = new Field(paramName, paramType);
            field.setVisibility(JavaVisibility.PRIVATE);

            field.addAnnotation("@Check(test = \"required\")");
            field.addAnnotation("@ApiModelProperty(value = \"批量删除列表\")");
            commentGenerator.addFieldComment(field, "批量删除列表");

            Method methodGet = getJavaBeansGetter(paramType, paramName, "获取批量删除列表");
            Method methodSet = getJavaBeansSetter(paramType, paramName, "设置批量删除列表");
            topLevelClass.addField(field);
            topLevelClass.addMethod(methodGet);
            topLevelClass.addMethod(methodSet);

        } else {
            // 如果是GetPo GetVo则和BatDeletePo一样必须有主键才行
            if (type.endsWith("GetPo") || type.endsWith("GetVo")) {
                List<IntrospectedColumn> priList = Lists.newArrayList();
                // 生成属性和方法
                for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                    // 是主键
                    if (isPrimaryKeyColumn(introspectedTable, introspectedColumn)) {
                        priList.add(introspectedColumn);
                    }
                }
                // 如果没有主键就不生成BatDeletePo了
                if (priList.size() <= 0) {
                    return;
                }
            }
            // 生成属性和方法
            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                Field field = getJavaBeansField(introspectedTable, introspectedColumn);
                boolean isPrimaryKey = isPrimaryKeyColumn(introspectedTable, introspectedColumn);
                // 如果是GetPo则单独处理
                if (type.endsWith("GetPo")) {
                    if (isPrimaryKey) {
                        topLevelClass.addImportedType(ANNOTATION_CHECK);
                        String swaggerAnnStr = ConverterUtil.toString(introspectedColumn.getRemarks(), introspectedColumn.getJavaProperty());
                        String colJavaType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
                        // Long型需要加序列化注解
                        if (colJavaType.equals(Long.class.getName())) {
                            topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                            topLevelClass.addImportedType(ID_SERIALIZER);
                            field.addAnnotation("@JsonSerialize(using = IdSerializer.class)");
                        }
                        // OffsetDateTime需要加序列化和反序列化注解
                        if (colJavaType.equals(OffsetDateTime.class.getName())) {
                            topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                            topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
                            topLevelClass.addImportedType(OFFSET_DATE_TIME_SERIALIZER);
                            topLevelClass.addImportedType(OFFSET_DATE_TIME_DESERIALIZER);
                            field.addAnnotation("@JsonSerialize(using = OffsetDateTimeSerializer.class)");
                            field.addAnnotation("@JsonDeserialize(using = OffsetDateTimeDeserializer.class)");
                        }
                        // 增加swaggerUI注解
                        field.addAnnotation("@ApiModelProperty(value=\"" + swaggerAnnStr + "\")");
                        // 增加check注解
                        field.addAnnotation("@Check(test = \"required\")");
                        topLevelClass.addField(field);
                        topLevelClass.addImportedType(field.getType());

                        // get函数
                        Method method = getJavaBeansGetter(introspectedTable, introspectedColumn);
                        if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                            topLevelClass.addMethod(method);
                        }

                        if (!introspectedTable.isImmutable()) {
                            // set函数
                            method = getJavaBeansSetter(introspectedTable, introspectedColumn);
                            if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                                topLevelClass.addMethod(method);
                            }
                        }
                    }
                    continue;
                }

                String fieldName = field.getName();
                // 如果是InsertPo则主键的列跳过(若需要手动设置id，就手动添加) delFlag在新增时也不需要
                if (type.endsWith("InsertPo") && (isPrimaryKey || fieldName.equals("delFlag"))) {
                    continue;
                }
                // 如果列为基础属性 则跳过
                if (fieldName.equals("createBy") || fieldName.equals("updateBy") || fieldName.equals("createDate") || fieldName.equals("updateDate")) {
                    continue;
                }
                if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                    String swaggerAnnStr = ConverterUtil.toString(introspectedColumn.getRemarks(), introspectedColumn.getJavaProperty());
                    String colJavaType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
                    // Long型需要加序列化注解
                    if (colJavaType.equals(Long.class.getName())) {
                        topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                        topLevelClass.addImportedType(ID_SERIALIZER);
                        field.addAnnotation("@JsonSerialize(using = IdSerializer.class)");
                    }
                    // OffsetDateTime需要加序列化和反序列化注解
                    if (colJavaType.equals(OffsetDateTime.class.getName())) {
                        topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                        topLevelClass.addImportedType("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
                        topLevelClass.addImportedType(OFFSET_DATE_TIME_SERIALIZER);
                        topLevelClass.addImportedType(OFFSET_DATE_TIME_DESERIALIZER);
                        field.addAnnotation("@JsonSerialize(using = OffsetDateTimeSerializer.class)");
                        field.addAnnotation("@JsonDeserialize(using = OffsetDateTimeDeserializer.class)");
                    }
                    // 如果是Inser和Update则添加check注解
                    if (type.endsWith("InsertPo") || type.endsWith("UpdatePo") || type.endsWith("SaveOrUpdatePo")) {
                        topLevelClass.addImportedType(ANNOTATION_CHECK);
                    }
                    List<String> swaggerAttrList = Lists.newArrayList();
                    swaggerAttrList.add("value = \"" + swaggerAnnStr + "\"");
                    // 如果是Update则添加check注解
                    if (type.endsWith("InsertPo") || type.endsWith("UpdatePo") || type.endsWith("SaveOrUpdatePo")) {
                        List<String> testList = Lists.newArrayList();
                        List<String> attrList = Lists.newArrayList();
                        List<String> swaggerAllowList = Lists.newArrayList();
                        boolean isNullable = introspectedColumn.isNullable();
                        // 非空的判断
                        if (!isNullable || isPrimaryKey) {
                            // 如果是精简的模式时主键不做非空校验
                            if (!(type.endsWith("SaveOrUpdatePo") && isPrimaryKey)) {
                                testList.add("\"required\"");
                                swaggerAttrList.add("required = true");
                            }
                            // 如果是主键还是LONG型 就添加datatype=string
                            if (isPrimaryKey && colJavaType.equals(Long.class.getName())) {
                                swaggerAttrList.add("dataType = \"java.lang.String\"");
                            }
                        }
                        // 如果是外键或者以Id为结尾的 大部分情况是其他表的关联ID 如果也是LONG型 同样添加datatype=string
                        if (fieldName.endsWith("Id") && colJavaType.equals(Long.class.getName())) {
                            swaggerAttrList.add("dataType = \"java.lang.String\"");
                        }
                        // 长度的判断
                        int colLength = introspectedColumn.getLength();
                        if (colLength > 0 && !isPrimaryKey) {
                            if (colJavaType.equals(String.class.getName())) {
                                testList.add("\"maxLength\"");
                                // string判断中英文混合的最大长度
                                // 新版的mysql和mariaDB varchar已经不区分中英文了 不用再判断混合长度了
                                attrList.add("length = " + colLength);
                                swaggerAllowList.add("length:(," + colLength + "]");
                            } else if (colJavaType.equals(Long.class.getName()) || colJavaType.equals(Integer.class.getName())) {
                                // 整数字判断最大长度
                                testList.add("\"maxLength\"");
                                attrList.add("length = " + colLength);
                                swaggerAllowList.add("length:(," + colLength + "]");
                            } else if (colJavaType.equals(Double.class.getName()) || colJavaType.equals(BigDecimal.class.getName())) {
                                // 小数除了判断长度外还要判断小数
                                testList.add("\"regex\"");
                                int scale = introspectedColumn.getScale();
                                String doubleRegex = "";
                                if (scale == 0) {
                                    doubleRegex = "^0$|^(0\\\\.[0-9]+)$|^[1-9]\\\\d*(\\\\.[0-9]+)?$";
                                } else if (scale > 0) {
                                    int numLength = (colLength - scale) - 1;
                                    if (numLength < 0) {
                                        numLength = 0;
                                    }
                                    doubleRegex += "&^0$|^(0\\\\.[0-9]{0," + scale + "})$|^[1-9]\\\\d{0," + numLength + "}(\\\\.[0-9]{0," + scale + "})?$";
                                }
                                attrList.add("regex = \"" + doubleRegex + "\"");
                                swaggerAllowList.add("regex:" + doubleRegex);
                            }
                            if (swaggerAllowList.size() > 0) {
                                String allowStr = swaggerAllowList.stream().collect(Collectors.joining(";"));
                                swaggerAttrList.add("allowableValues = \"" + allowStr + "\"");
                            }
                        }

                        if (testList.size() > 0) {
                            StringBuffer testStr = new StringBuffer("@Check(test = { " + testList.stream().collect(Collectors.joining(", ")) + " }");
                            if (attrList.size() > 0) {
                                testStr.append(", ");
                                testStr.append(String.join(" ", attrList));
                            }
                            testStr.append(")");
                            // 增加check注解
                            field.addAnnotation(testStr.toString());
                        }
                    }

                    // 增加swaggerUI注解
                    if (swaggerAttrList.size() > 0) {
                        field.addAnnotation("@ApiModelProperty(" + swaggerAttrList.stream().collect(Collectors.joining(", ")) + ")");
                    }
                    topLevelClass.addField(field);
                    topLevelClass.addImportedType(field.getType());
                }

                // get函数
                Method method = getJavaBeansGetter(introspectedTable, introspectedColumn);
                if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }

                if (!introspectedTable.isImmutable()) {
                    // set函数
                    method = getJavaBeansSetter(introspectedTable, introspectedColumn);
                    if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                        topLevelClass.addMethod(method);
                    }
                }
            }
            // 只有SaveOrUpdatePo InsertPo UpdatePo 和 ListVo GetVo 需要加关系属性
            switch (type) {
                case "SaveOrUpdatePo":
                case "InsertPo":
                case "UpdatePo":
                case "ListVo":
                case "GetVo":
                    // 获取表名
                    String tableName = introspectedTable.getTableConfiguration().getTableName();
                    // 查询是否有关联关系
                    List<JoinTable> joinLst = relationInfo.getOneToManys(tableName);
                    // 是否有关联关系
                    if (ConverterUtil.isNotEmpty(joinLst) || ((WbfcTableConfiguration) introspectedTable.getTableConfiguration()).hasRelation()) {
                        // 添加关系属性
                        setRelationFields(tableName, topLevelClass, true, joinLst);
                    }
                    break;
            }
        }

        // 创建GeneratedJavaFile
        String createPath = null;
        if (type.endsWith("Po")) {
            createPath = wbfcConfig.getPoPath();
        } else {
            createPath = wbfcConfig.getVoPath();
        }
        GeneratedJavaFile gjf = new GeneratedJavaFile(topLevelClass, createPath, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        list.add(gjf);

        // 缓存到Map
        createMap.put(type, topLevelClass);
    }

    /**
     * 把class名转小写
     *
     * @param javaType
     * @return
     */
    public static String fullyQualifiedJavaType2FieldName(FullyQualifiedJavaType javaType) {
        String shortName = javaType.getShortName();
        return shortName.substring(0, 1).toLowerCase() + shortName.substring(1);
    }

    /**
     * 是否为主键列
     *
     * @param introspectedTable
     * @param introspectedColumn
     * @return
     */
    protected boolean isPrimaryKeyColumn(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        for (IntrospectedColumn pc : introspectedTable.getPrimaryKeyColumns()) {
            if (pc.equals(introspectedColumn)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        try {
            // 默认的mapper不会覆盖而是merge
            java.lang.reflect.Field field = sqlMap.getClass().getDeclaredField("isMergeable");
            field.setAccessible(true);
            field.setBoolean(sqlMap, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 替换entity
     *
     * @param introspectedTable
     * @param plugins
     * @param list
     */
    protected void genderEntity(IntrospectedTable introspectedTable, Plugin plugins, List<GeneratedJavaFile> list) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        // // 创建实体类接口
        // FullyQualifiedJavaType type = genderEntityI(introspectedTable, list, commentGenerator);

        FullyQualifiedJavaType recordType = calculateJavaModelGenerators(introspectedTable);
        TopLevelClass topLevelClass = new TopLevelClass(recordType);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // // 添加接口
        // topLevelClass.addSuperInterface(type);
        String entityJavaDoc = introspectedTable.getTableConfiguration().getTableName() + " 表实体";
        commentGenerator.addJavaFileComment(topLevelClass, createDate, entityJavaDoc, GeneratorUtil.getTableRemarks(introspectedTable));

        FullyQualifiedJavaType superClass = getSuperClass(topLevelClass, introspectedTable, recordType);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass(introspectedTable);

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable);
        }

        // 默认创建一个无参构造器
        if (!introspectedTable.isImmutable()) {
            addDefaultConstructor(topLevelClass, introspectedTable);
        }

        Class<?> clazz = null;
        // 判断是否需要继承Wbfc的基础entity
        Map<String, java.lang.reflect.Field> supperMap = null;
        try {
            if (null != superClass) {
                String superClassName = superClass.getFullyQualifiedName();
                if (superClassName.contains(DATA_ENTITY)) {

                    clazz = Class.forName(DATA_ENTITY);
                } else {
                    clazz = Class.forName(DATA_LONG_ENTITY);
                }
                supperMap = Lists.newArrayList(ConverterUtil.getAllFields(clazz)).stream().collect(Collectors.toMap(java.lang.reflect.Field::getName, a -> a, (v1, v2) -> v1));

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (containsProperty(supperMap, introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedTable, introspectedColumn);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }

            Method method = getJavaBeansGetter(introspectedTable, introspectedColumn);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addMethod(method);
            }

            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedTable, introspectedColumn);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }
            }
        }

        List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
            compilationUnits.add(topLevelClass);
        }
        for (CompilationUnit compilationUnit : compilationUnits) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit, context.getJavaModelGeneratorConfiguration().getTargetProject(),
                    context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
            list.add(gjf);
        }
        // 若配置了关联关系的话 还要生成关系属性
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        WbfcTableConfiguration tc = (WbfcTableConfiguration) introspectedTable.getTableConfiguration();
        // 添加到缓存
        addRelationInfo(tableName, tc.getJoinTableList());
        // 查询是否有关联关系
        List<JoinTable> joinLst = relationInfo.getOneToManys(tableName);
        // 添加关系属性
        setRelationFields(tableName, topLevelClass, false, joinLst);

        createMap.put("entity", topLevelClass);
    }

    /**
     * 设置有关联关系的属性
     *
     * @param tableName
     * @param topLevelClass
     * @param hasSwigger
     */
    public void setRelationFields(String tableName, TopLevelClass topLevelClass, boolean hasSwigger, List<JoinTable> joinLst) {
        // 如果有关系就要设置属性
        if (ConverterUtil.isNotEmpty(joinLst)) {
            topLevelClass.addImportedType(UTIL_LIST);
            topLevelClass.addImportedType(GOOGLE_LIST);
            for (JoinTable joTab : joinLst) {
                IntrospectedTable joinTable = GeneratorUtil.getIntrospectedTable(context, joTab.getTableName());
                String jtName = joinTable.getTableConfiguration().getDomainObjectName();
                if ("oneToOne".equalsIgnoreCase(joTab.getType())) {
                    // 一对一关系属性
                    makeOneToOneField(topLevelClass, joinTable, "一对一关系表" + jtName, hasSwigger);
                } else {
                    // 一对多关系的属性
                    makeOneToManyField(topLevelClass, joinTable, "一对多关系表" + jtName, hasSwigger);
                }
            }
        }
    }

    /**
     * 添加到关系
     *
     * @param tableName
     * @param otmList
     */
    public void addRelationInfo(String tableName, List<JoinTable> otmList) {
        // 如果onToMany 就生成Rels
        if (ConverterUtil.isNotEmpty(otmList)) {
            for (JoinTable otm : otmList) {
                // 缓存到关系
                relationInfo.addOneToMany(tableName, otm);
                List<JoinTable> subList = otm.getJoinTableList();
                if (ConverterUtil.isNotEmpty(subList)) {
                    addRelationInfo(otm.getTableName(), subList);
                }
            }
        }
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
     * 是否有子包
     *
     * @param propertyHolder
     * @return
     */
    public boolean isSubPackagesEnabled(PropertyHolder propertyHolder) {
        return ConverterUtil.toBoolean(propertyHolder.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES));
    }

    /**
     * 添加一个一对多关系属性
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param javaDocs
     * @param hasSwigger
     * @return
     */
    public Field makeOneToOneField(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String javaDocs, boolean hasSwigger) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        String javaType = calculateJavaType(introspectedTable);
        // 如果是povo 类型要变化为对应的po和vo
        if (this.hasPoVo) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            String topClassName = topLevelClass.getType().getFullyQualifiedName();
            // SaveOrUpdatePo InsertPo UpdatePo 和 ListVo GetVo
            if (topClassName.endsWith("InsertPo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "InsertPo";
            } else if (topClassName.endsWith("UpdatePo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "UpdatePo";
            } else if (topClassName.endsWith("SaveOrUpdatePo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "SaveOrUpdatePo";
            } else if (topClassName.endsWith("Vo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            }
        }
        // 如果没有主键 是无法生成GetVo的 使用entity
        if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns())) {
            javaType = calculateJavaModelGenerators(introspectedTable).getFullyQualifiedName();
        }
        topLevelClass.addImportedType(javaType);
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(javaType);
        // 设置关系属性列表
        String property = introspectedTable.getTableConfiguration().getDomainObjectName() + "Rels";
        property = JavaBeansUtil.getValidPropertyName(property);
        Field field = new Field(property, fqjt);
        field.setVisibility(JavaVisibility.PRIVATE);
        // 一对一关联属性要为null
        //field.setInitializationString("new " + fqjt.getShortName() + "()");
        if (hasSwigger) {
            field.addAnnotation("@ApiModelProperty(value = \"" + javaDocs + "\")");
        }
        topLevelClass.addField(field);
        commentGenerator.addFieldComment(field, javaDocs);
        // 增加getset函数
        topLevelClass.addMethod(getJavaBeansGetter(fqjt, property, "获取" + javaDocs));
        topLevelClass.addMethod(getJavaBeansSetter(fqjt, property, "设置" + javaDocs));
        return field;
    }

    /**
     * 添加一个一对多关系属性
     *
     * @param topLevelClass
     * @param introspectedTable
     * @param javaDocs
     * @param hasSwigger
     * @return
     */
    public Field makeOneToManyField(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String javaDocs, boolean hasSwigger) {
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(UTIL_LIST);
        String javaType = calculateJavaType(introspectedTable);
        // 如果是povo 类型要变化为对应的po和vo
        if (this.hasPoVo) {
            WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();
            String topClassName = topLevelClass.getType().getFullyQualifiedName();
            // SaveOrUpdatePo InsertPo UpdatePo 和 ListVo GetVo
            if (topClassName.endsWith("InsertPo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "InsertPo";
            } else if (topClassName.endsWith("UpdatePo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "UpdatePo";
            } else if (topClassName.endsWith("SaveOrUpdatePo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getPoPackage()) + "SaveOrUpdatePo";
            } else if (topClassName.endsWith("Vo")) {
                javaType = javaType.replace(wbfcConfig.getEntityPackage(), wbfcConfig.getVoPackage()) + "GetVo";
            }
        }
        // 如果没有主键 是无法生成GetVo的 使用entity
        if (ConverterUtil.isEmpty(introspectedTable.getPrimaryKeyColumns())) {
            javaType = calculateJavaType(introspectedTable);
        }
        topLevelClass.addImportedType(javaType);
        fqjt.addTypeArgument(new FullyQualifiedJavaType(javaType));
        // 设置关系属性列表
        String property = introspectedTable.getTableConfiguration().getDomainObjectName() + "RelsList";
        property = JavaBeansUtil.getValidPropertyName(property);
        Field field = new Field(property, fqjt);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setInitializationString("Lists.newArrayList()");
        if (hasSwigger) {
            field.addAnnotation("@ApiModelProperty(value = \"" + javaDocs + "\")");
        }
        topLevelClass.addField(field);
        commentGenerator.addFieldComment(field, javaDocs);
        // 增加getset函数
        topLevelClass.addMethod(getJavaBeansGetter(fqjt, property, "获取" + javaDocs));
        topLevelClass.addMethod(getJavaBeansSetter(fqjt, property, "设置" + javaDocs));
        return field;
    }

//    /**
//     * 创建属性
//     *
//     * @param fullTypeSpecification
//     * @param property
//     * @param visibility
//     * @return
//     */
//    public Field createField(String fullTypeSpecification, String property, JavaVisibility visibility) {
//        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(fullTypeSpecification);
//        fqjt.addTypeArgument();
//        Field field = new Field(property, fqjt);
//        field.setVisibility(visibility);
//        return field;
//    }

    // /**
    // * 创建实体类接口
    // *
    // * @param introspectedTable
    // * @param list
    // * @param commentGenerator
    // * @return
    // */
    // protected FullyQualifiedJavaType genderEntityI(IntrospectedTable introspectedTable, List<GeneratedJavaFile> list, WbfcCommentGenerator commentGenerator) {
    // // 由于entity和PoVo会有交集，为了dao生成方便，在所有entity上都进行了接口的实现
    // FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType() + "I");
    // Interface interfaze = new Interface(type);
    // interfaze.setVisibility(JavaVisibility.PUBLIC);
    // String entityInterfaceDoc = introspectedTable.getTableConfiguration().getTableName() + " 表接口";
    // commentGenerator.addJavaFileComment(interfaze, createDate, entityInterfaceDoc, GeneratorUtil.getTableRemarks(introspectedTable));
    // GeneratedJavaFile gjf = new GeneratedJavaFile(interfaze, context.getJavaModelGeneratorConfiguration().getTargetProject(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
    // context.getJavaFormatter());
    // list.add(gjf);
    //
    // createMap.put("entityI", interfaze);
    // return type;
    // }

    public boolean containsProperty(Map<String, java.lang.reflect.Field> superMap, IntrospectedColumn introspectedColumn) {
        boolean found = false;
        if (null == superMap) {
            return false;
        }
        String propertyName = introspectedColumn.getJavaProperty();
        if (superMap.containsKey(propertyName)) {
            found = true;
        }
        return found;
    }

    public Method getJavaBeansGetter(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method(getGetterMethodName(property, fqjt));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);

        context.getCommentGenerator().addGetterComment(method, introspectedTable, introspectedColumn);

        StringBuilder sb = new StringBuilder();
        sb.append("return "); //$NON-NLS-1$
        sb.append(property);
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }

    public Method getJavaBeansGetter(FullyQualifiedJavaType paramType, String paramName, String... javaDocs) {
        Method method = new Method(getGetterMethodName(paramName, paramType));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(paramType);
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        commentGenerator.addMethodComment(method, javaDocs);

        StringBuilder sb = new StringBuilder();
        sb.append("return "); //$NON-NLS-1$
        sb.append(paramName);
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }

    public Method getJavaBeansSetter(FullyQualifiedJavaType paramType, String paramName, String... javaDocs) {
        Method method = new Method(getSetterMethodName(paramName));
        method.setVisibility(JavaVisibility.PUBLIC);

        method.addParameter(new Parameter(paramType, paramName));
        WbfcCommentGenerator commentGenerator = (WbfcCommentGenerator) context.getCommentGenerator();
        commentGenerator.addMethodComment(method, javaDocs);

        StringBuilder sb = new StringBuilder();
        if (isTrimStringsEnabled() && paramType.getFullyQualifiedName().equals(String.class.getName())) {
            sb.append("this.");
            sb.append(paramName);
            sb.append(" = ");
            sb.append(paramName);
            sb.append(" == null ? null : ");
            sb.append(paramName);
            sb.append(".trim();");
            method.addBodyLine(sb.toString());
        } else {
            sb.append("this.");
            sb.append(paramName);
            sb.append(" = ");
            sb.append(paramName);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        return method;
    }

    public Field getJavaBeansField(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Field field = new Field(property, fqjt);
        field.setVisibility(JavaVisibility.PRIVATE);
        context.getCommentGenerator().addFieldComment(field, introspectedTable, introspectedColumn);

        return field;
    }

    public Method getJavaBeansSetter(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method(getSetterMethodName(property));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(fqjt, property));
        context.getCommentGenerator().addSetterComment(method, introspectedTable, introspectedColumn);

        StringBuilder sb = new StringBuilder();
        if (isTrimStringsEnabled() && introspectedColumn.isStringColumn()) {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(" == null ? null : "); //$NON-NLS-1$
            sb.append(property);
            sb.append(".trim();"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        } else {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        return method;
    }

    public boolean isTrimStringsEnabled() {
        Properties properties = context.getJavaModelGeneratorConfiguration().getProperties();
        boolean rc = isTrue(properties.getProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS));
        return rc;
    }

    public String getRootClass(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        if (rootClass == null) {
            Properties properties = context.getJavaModelGeneratorConfiguration().getProperties();
            rootClass = properties.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
        }

        return rootClass;
    }

    protected void addDefaultConstructor(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.addBodyLine("super();"); //$NON-NLS-1$
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        List<IntrospectedColumn> constructorColumns = includeBLOBColumns(introspectedTable) ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns();

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
        }

        StringBuilder sb = new StringBuilder();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass(introspectedTable);

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            sb.setLength(0);
            sb.append("this."); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" = "); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }

    private boolean includePrimaryKeyColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass() && introspectedTable.hasBLOBColumns();
    }

    private List<IntrospectedColumn> getColumnsInThisClass(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns(introspectedTable)) {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable.getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }

    /**
     * 获取继承的父类
     *
     * @param answer
     * @param introspectedTable
     * @param recordType
     * @return
     */
    private FullyQualifiedJavaType getSuperClass(TopLevelClass answer, IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
        FullyQualifiedJavaType superClass = null;
        Optional<IntrospectedColumn> primaryColumn = introspectedTable.getColumn("id");
        if (primaryColumn.isPresent()) {
            FullyQualifiedJavaType javaType = primaryColumn.get().getFullyQualifiedJavaType();
            if (Long.class.getName().equals(javaType.getFullyQualifiedName())) {
                superClass = new FullyQualifiedJavaType(DATA_LONG_ENTITY);
            } else if (String.class.getName().equals(javaType.getFullyQualifiedName())) {
                superClass = new FullyQualifiedJavaType(DATA_ENTITY);
            }
            if (superClass != null) {
                superClass.addTypeArgument(recordType);
                answer.setSuperClass(superClass);
                answer.addImportedType(superClass);
            }
        }
        makeSerializable(answer);
        return superClass;
    }

    protected void makeSerializable(TopLevelClass topLevelClass) {
        Field field = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        field.setFinal(true);
        field.setInitializationString("1L");
        field.setStatic(true);
        field.setVisibility(JavaVisibility.PRIVATE);

        List<Field> fields = topLevelClass.getFields();
        fields.add(0, field);
    }
    //
    // protected TopLevelClass generateRealRecordClass(IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
    // FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
    // Plugin plugins = context.getPlugins();
    // CommentGenerator commentGenerator = context.getCommentGenerator();
    //
    // FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
    // TopLevelClass topLevelClass = new TopLevelClass(type);
    // topLevelClass.setVisibility(JavaVisibility.PUBLIC);
    // commentGenerator.addJavaFileComment(topLevelClass);
    //
    // FullyQualifiedJavaType superClass = getSuperClass();
    // if (superClass != null) {
    // topLevelClass.setSuperClass(superClass);
    // topLevelClass.addImportedType(superClass);
    // }
    //
    // List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();
    //
    // if (introspectedTable.isConstructorBased()) {
    // addParameterizedConstructor(topLevelClass);
    //
    // if (!introspectedTable.isImmutable()) {
    // addDefaultConstructor(topLevelClass);
    // }
    // }
    //
    // String rootClass = getRootClass();
    // for (IntrospectedColumn introspectedColumn : introspectedColumns) {
    // if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
    // continue;
    // }
    //
    // Field field = getJavaBeansField(introspectedColumn);
    // if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
    // topLevelClass.addField(field);
    // topLevelClass.addImportedType(field.getType());
    // }
    //
    // Method method = getJavaBeansGetter(introspectedColumn);
    // if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
    // topLevelClass.addMethod(method);
    // }
    //
    // if (!introspectedTable.isImmutable()) {
    // method = getJavaBeansSetter(introspectedColumn);
    // if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
    // topLevelClass.addMethod(method);
    // }
    // }
    // }
    //
    // List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
    // if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
    // answer.add(topLevelClass);
    // }
    // return answer;
    // TopLevelClass answer = new TopLevelClass(recordType);
    // IntrospectedColumn primaryColumn = introspectedTable.getColumn("id");
    // if (null != primaryColumn) {
    // FullyQualifiedJavaType parentType = null;
    // FullyQualifiedJavaType javaType = primaryColumn.getFullyQualifiedJavaType();
    // if (Long.class.getName().equals(javaType.getFullyQualifiedName())) {
    // parentType = new FullyQualifiedJavaType("com.wisea.cloud.common.entity.DataLongEntity");
    // } else if (String.class.getName().equals(javaType.getFullyQualifiedName())) {
    // parentType = new FullyQualifiedJavaType("com.wisea.cloud.common.entity.DataEntity");
    // }
    // parentType.addTypeArgument(recordType);
    // answer.setSuperClass(parentType);
    // answer.addImportedType(parentType);
    // } else {
    //
    // }
    // answer.setSuperClass(getAbstractType(answer));
    // answer.setVisibility(JavaVisibility.PUBLIC);
    // makeSerializable(answer, introspectedTable);
    // return answer;
    // }

}
