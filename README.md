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
本插件依赖Database插件。
1. 需要现在DataBase中新建数据源
2. Database窗口中打开对应的数据库
3. 单选或多选表
4. 右键 -> 弹出菜单选择 WbfcEditor 或 工具条 Tools -> WbfcEditor
5. 配置信息
6. 生成代码

## 安装和发布版本
1. 选择发布版本点击进行 [下载](https://github.com/xudl33/IdeaWbfcEditor/releases)
2. 进入IDEA -> Settings -> Plugins -> 右侧小齿轮 -> Install Plugin from disk -> 选择 WbfcEditor-x.x.x.zip -> 重启IDEA

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
这里为全局配置，也可以在下一步中对表单独进行配置,精简模式下无法生成【批量新增】和【批量修改】

### 2 - 配置生成规则
#### 映射名
默认使用表名的驼峰命名法 例： gen_test -> GenTest
#### 结构简化
单表配置。开启该选项后，表对应的Controller的insert和update会简化成一个saveOrUpdate函数, delete会使用固定的com.wisea.cloud.model.po.LongIdPo 
精简模式下无法生成【批量新增】和【批量修改】
#### 批量新增
开启该项，除了默认的insert还会在Controller、Service、DAO、XML中新增batchInsert函数。

#### 批量修改
开启该项，除了默认的update还会在Controller、Service、DAO、XML中新增batchInsert函数。
#### 批量删除
默认的删除方法是伪批量,在Service中使用for循环进行循环操做。开启该选项后，将会XML会生成batchDelete函数,Service中也会直接调用batchDelete

### 3 - 开始生成
#### 自定义XML
在第一步中如果开启了自定义XML的开关，就会显示出即将执行的XML。点击生成时就会按照自定义的XML进行生成。
#### Generator Log Console
生成过程中的控制台日志输出

## 发布版本
时间|版本|说明
---|---|---
20200812|1.0.3|增加配置路径表单重置功能;优化视觉效果、表单按钮美化；
20200810|1.0.2|增加DiyXml的功能;生成功能全部完成
20200731|1.0.1|增加simplePoVo和batchInsert/update/delete相关的代码生成功能；
20200727|1.0.0|基本功能移植完成