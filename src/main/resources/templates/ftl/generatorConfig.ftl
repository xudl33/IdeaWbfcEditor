<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <#if logProperties?? >
        <!-- 引入配置文件 -->
        <properties resource="${logProperties}"/>
    </#if>

    <!-- 指定数据连接驱动jar地址 -->
    <classPathEntry location="${classPathEntry}" />

    <!-- 一个数据库一个context -->
    <context id="MySqlTables" targetRuntime="MyBatis3">
        <property name="javaFileEncoding" value="${charset}"/>

        <!-- Wbfc生成插件 -->
        <plugin type="com.wisea.cloud.wbfceditor.generator.WbfcModelGenerator">
            <property name="hasPoVo" value="${hasPoVo}"/>
            <property name="hasService" value="${hasService}"/>
            <property name="hasController" value="${hasController}"/>
            <!-- 精简Po和Vo -->
            <property name="simplePoVo" value="${simplePoVo}"/>
            <!-- 更新策略 -->
            <property name="updateStrategy" value="${updateStrategy}"/>
        </plugin>

        <!-- 注释 -->
        <commentGenerator type="com.wisea.cloud.wbfceditor.generator.WbfcCommentGenerator">
            <!-- 是否取消注释 -->
            <property name="suppressAllComments" value="false"/>
            <!-- 是否生成注释代时间戳 -->
            <property name="suppressDate" value="true"/>
            <!-- 文件编码格式 -->
        </commentGenerator>

        <!--配置数据库链接 -->
        <jdbcConnection driverClass="${dbDriver}"
                        connectionURL="${dbUrl}"
                        userId="${dbUser}" password="${dbPassword}"><#list dbUrlPropertyMap?keys as key>
            <property name="${key}" value="${dbUrlPropertyMap[key]}"></property></#list>
        </jdbcConnection>

        <!-- 类型转换 -->
        <javaTypeResolver>
            <!-- 是否使用bigDecimal， false可自动转化以下类型（Long, Integer, Short, etc.） -->
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>

        <!-- 生成实体类地址 -->
        <javaModelGenerator targetPackage="${entityPackage}"
                            targetProject="${entityPath}">
            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->
            <property name="enableSubPackages" value="true"/>
            <!-- 是否针对string类型的字段在set的时候进行trim调用 -->
            <property name="trimStrings" value="true"/>
        </javaModelGenerator>

        <!-- 生成mapxml文件 改类执行在WbfcMapperGenerator中覆盖了默认的生成器 使用的是WbfcXMLMapperGenerator-->
        <sqlMapGenerator targetPackage="${xmlPackage}"
                         targetProject="${xmlPath}">
            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->
            <property name="enableSubPackages" value="true"/>
        </sqlMapGenerator>

        <!-- 生成mapxml对应client，也就是接口dao type="XMLMAPPER" -->
        <javaClientGenerator type="com.wisea.cloud.wbfceditor.generator.WbfcMapperGenerator"
                             targetPackage="${daoPackage}"
                             targetProject="${daoPath}">
            <!-- 是否在当前路径下新加一层schema,eg：fase路径com.oop.eksp.user.model， true:com.oop.eksp.user.model.[schemaName] -->
            <property name="enableSubPackages" value="true"/>
            <!-- 是否有Po和Vo -->
            <property name="hasPoVo" value="${hasPoVo}"/>
            <!-- 精简Po和Vo -->
            <property name="simplePoVo" value="${simplePoVo}"/>
        </javaClientGenerator>

        <!-- 配置表信息 -->
        <!-- schema即为数据库名 tableName为对应的数据库表 domainObjectName是要生成的实体类 enable*ByExample是否生成example类 -->
        <!-- <table tableName="sys_user" domainObjectName="SysUser"
               enableCountByExample="false" enableSelectByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false">
            <columnOverride column="create_date"
                            javaType="java.time.OffsetDateTime"/>
            <columnOverride column="update_date"
                            javaType="java.time.OffsetDateTime"/>
        </table> -->

        <!-- tableInfo isRelation=true说明是关系表 不生成Controller -->
        <!-- <table tableName="sys_user_role" domainObjectName="SysUserRole"
               enableCountByExample="false" enableSelectByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false">
            <tableInfo isRelation="true"></tableInfo>
        </table> -->

        <!-- joinTable type=OneToOne 一对一 type=oneToMany(默认) 一对多 -->
        <!-- tableName关联的表名  columns 关联的列 结构:{关联表列名 = 主表列名} joinTable可以无限嵌套和混合使用 -->
        <!-- <table tableName="sys_user" domainObjectName="SysUser"
               enableCountByExample="false" enableSelectByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false">
            <columnOverride column="create_date"
                            javaType="java.time.OffsetDateTime"/>
            <columnOverride column="update_date"
                            javaType="java.time.OffsetDateTime"/>
            <joinTable tableName="sys_user_role" columns="{user_id = id}">
                <joinTable type="OneToOne" tableName="sys_role" columns="{id = role_id}" >
                </joinTable>
            </joinTable>
        </table> -->
<#list tablesList as tbl>
${tbl}
</#list>
    </context>
</generatorConfiguration>
