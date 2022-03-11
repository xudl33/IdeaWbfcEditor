# WbfcEditor (Intellij IDEA Plugin)

WbfcEditor是一个基于WBFC架构的代码生成器。可以通过DB进行反向映射生成Java代码。

单表的增删改查、批量增删改、数据基础校验、SwaggerUI文档、等功能均可生成。

生成的SW文档还可以通过 [wbfc-vue-cli](https://github.com/xudl33/wbfc-vue-cli) 生成基于 [wbfc-vue-components](https://github.com/xudl33/wbfc-vue-components) 的Vue页面与组件。

## 开发本插件的目的
+ 提高JavaWeb服务端开发人员的生产效率；
+ 为开发人员丰富IDEA插件；
+ 避免一些简单的BUG；
+ 学习com.intellij.openapi；

## 简介
原始版为Eclipse插件，本插件为移植版。除了原有WbfcEditor的代码生成功能外(Entity DAO Mapper Service Controller PO VO)，还增加了一些特有的功能，比如【结构精简】【批量 Insert | Update | Delete】【自定义XML】等，这些功能以后会逐渐反向更新到Eclipse的插件当中去。

## 使用
本插件依赖 [JavaFX Runtime for Plugins JetBrains](https://plugins.jetbrains.com/plugin/14250-javafx-runtime-for-plugins) 和 Database 插件

1. 需要现在DataBase中新建数据源
2. Database窗口中打开对应的数据库
3. 单选或多选表
4. 右键 -> 弹出菜单选择 WbfcEditor 或 工具条 Tools -> WbfcEditor
5. 配置信息
6. 生成代码

## 安装和发布版本
1. 选择发布版本点击进行 [下载](https://github.com/xudl33/IdeaWbfcEditor/releases)
2. 进入IDEA -> Settings -> Plugins -> 右侧小齿轮 -> Install Plugin from disk -> 选择 WbfcEditor-x.x.x.zip -> 重启IDEA

本插件要求IDEA版本在2020.2以上，可以直接安装使用, 2020.2以下只能使用 [1.0.3](https://github.com/xudl33/IdeaWbfcEditor/releases/tag/1.0.3) 且IDEA Runtime Version必须为JDK1.8.0_181及以上版本

    
## 配置
成功安装了WbfcEditor插件的IDEA Settings -> WbfcEditor 可以进行工作目录的配置。WbfcEditor使用到的缓存和临时文件，都会使用该配置作为工作目录。
默认值为 `${workspace}/.idea/WbfcEditor`

## 表的特殊性
WbfcEditor是为基于Wbfc框架生成的相关类。表结构需要基于以下标准创建，主表必须有如下属性，子表必须有`id`属性。如果需要生成含有删除功能的代码，则`del_flag`是必须存在的。

属性|主键|说明
---|---|---
id|是|主键
create_by|否|创建者
create_date|否|创建时间
update_by|否|修改者
update_date|否|修改时间
remarks|否|备注
del_flag|否|删除标志 0:未删除 1:已删除

## 功能说明
默认的生成器会生成Entity和DAO和Mapper.xml文件。其他的Java文件根据需要进行配置后可以生成。

### 1 - 配置路径
#### 自定义XML 
开启该项后,在第三步骤时会根据前两步的配置生成XML,点击【生成】按钮时将使用XML进行生成
#### Controller
开启该项，则必须填写Controller包路径和包名，生成时会创建对应的Controller。
#### Service
开启该项，则必须填写Service包路径和包名，生成时会创建对应的Service。
#### PO&VO
开启该项，则必须填写PO和VO的包路径、包名，生成时会创建对应的PO和VO。
#### 结构简化
全局配置。开启该选项后，Controller的insert和update会简化成一个saveOrUpdate函数, delete会使用固定的com.wisea.cloud.model.po.LongIdPo 
这里为全局配置，也可以在下一步中对表单独进行配置,精简模式下无法生成【批量新增】和【批量修改】。

### 2 - 配置生成规则
#### 映射名
默认使用表名的驼峰命名法 例： gen_test -> GenTest
#### 结构简化
表对应的Controller的insert和update会简化成一个saveOrUpdate函数, delete会使用固定的com.wisea.cloud.model.po.LongIdPo
精简模式下无法生成【批量新增】和【批量修改】。
#### 批量新增
开启该项，除了默认的insert还会在Controller、Service、DAO、XML中新增batchInsert函数。
#### 批量修改
开启该项，除了默认的update还会在Controller、Service、DAO、XML中新增batchInsert函数。
#### 批量删除
默认的删除方法是伪批量,在Service中使用for循环进行循环操做。开启该选项后，将会XML会生成batchDelete函数,Service中也会直接调用batchDelete。
### 3 - 开始生成
#### 自定义XML
在第一步中如果开启了自定义XML的开关，就会显示出即将执行的XML。点击生成时就会按照自定义的XML进行生成。
#### Generator Log Console
生成过程中的控制台日志输出

### 特殊说明
#### OneToOne(一对一）和 OneToMany(一对多)
在1.0.5版本之后增加了一对一、一对多关系表配置，并且可以混合使用，无限嵌套，但这个功能只能在自定义XML中通过手动配置<joinTable>来实现。例：
```xml
以上为省略的generatorConfiguration
......

<table tableName="ser_list_mage" domainObjectName="SerListMage" enableCountByExample="false" enableSelectByExample="false" enableUpdateByExample="false" enableDeleteByExample="false">
    <columnOverride column="ser_date" javaType="java.time.OffsetDateTime" />
    <columnOverride column="subscribe_date" javaType="java.time.OffsetDateTime" />
    <columnOverride column="create_date" javaType="java.time.OffsetDateTime" />
    <columnOverride column="update_date" javaType="java.time.OffsetDateTime" />
    <!-- joinTable type=OneToOne 一对一 type=oneToMany(默认) 一对多 -->
    <!-- tableName关联的表名  columns 关联的列 结构:{关联表列名 = 主表列名} joinTable可以无限嵌套和混合使用 -->    
    <joinTable tableName="ser_list_opreate_info" columns="{ser_list_id = id}">
    </joinTable>
</table>

<table tableName="ser_list_opreate_info" domainObjectName="SerListOpreateInfo" enableCountByExample="false" enableSelectByExample="false" enableUpdateByExample="false" enableDeleteByExample="false">
    <!-- isRelation="true"意为仅关系表 不生成Controller -->
    <tableInfo isRelation="true"></tableInfo>
    <columnOverride column="opreat_date" javaType="java.time.OffsetDateTime" />
    <columnOverride column="create_date" javaType="java.time.OffsetDateTime" />
    <columnOverride column="update_date" javaType="java.time.OffsetDateTime" />
</table>
......
```
#### joinTable
关联表节点，使用在`<table>`内，可以多层嵌套使用。

属性|必填|默认值|说明
---|---|---|---
type|是|OneToMany|OneToOne(一对一) / OneToMany(一对多)
tableName|是|null|关联表名称
columns|是|null|格式:{主表列名 =  关联表列名} 多个关联条件用逗号(,)分隔
isCasecade|否|true|是否级联 false:不级联(新增/修改/删除) true:级联上级操作 false时依然会进行关联查询，但不会关联新增/修改/删除操作。

#### tableInfo
表信息节点,使用在`<table>`内。

属性|必填|默认值|说明
---|---|---|---
simplePoVo|否|false|是否精简POVO 开启该项后(=true)，表对应的Controller的insert和update会简化成一个saveOrUpdate函数, delete会使用固定的com.wisea.cloud.model.po.LongIdPo精简模式下无法生成【批量新增】和【批量修改】。
batchInsert|否|false|是否有批量新增函数 开启该项后(=true)，除了默认的insert还会在Controller、Service、DAO、XML中新增batchInsert函数。
batchUpdate|否|false|是否有批量更新函数 开启该项后(=true)，除了默认的update还会在Controller、Service、DAO、XML中新增batchInsert函数。
batchDelete|否|false|是否有批量删除函数 默认的删除方法是伪批量,在Service中使用for循环进行循环操做。开启该项后(=true)，将会XML会生成batchDelete函数,Service中也会直接调用batchDelete。
isRelation|否|false|是否为关系表 开启该项后(=true)，将不会生成Controller。

## 发布版本
时间|版本|说明
---|---|---
20201126|1.0.5|增加了外键或主键的LONG型,SwaggerUI文档dataType=String的功能;增加一对一、一对多生成(手动配置);增加纯关系表生成不包括Conroller的选项;
20200826|1.0.4|增加了输出文件自选编码格式的功能;修正了自定义映射名不正确的问题;修正Settings找不到默认工程目录的问题；修正MAC OSX无法读取配置路径的问题;
20200812|1.0.3|增加配置路径表单重置功能;优化视觉效果、表单按钮美化;
20200810|1.0.2|增加DiyXml的功能;生成功能全部完成;
20200731|1.0.1|增加simplePoVo和batchInsert/update/delete相关的代码生成功能;
20200727|1.0.0|基本功能移植完成;