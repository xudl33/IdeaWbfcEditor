package com.wisea.cloud.wbfceditor.generator.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.wisea.cloud.common.exception.VerifyException;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.common.util.DataCheckUtil;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorGenerator;
import com.wisea.cloud.wbfceditor.generator.entity.*;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.config.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GeneratorUtil {
    /**
     * table详细模板
     */
    public static String TABLE_DETAIL_TEMPLETE = "            <columnOverride column=\"{0}\" {1} />";
    public static String TABLE_DETAIL_DATE_TEMPLETE = "            <columnOverride column=\"{0}\" javaType=\"java.time.OffsetDateTime\"/>";

    private static WbfcConfig wbfcConfig = new WbfcConfig();

    private static WbfcEditorGenerator wbfcEditorGenerator = null;

    private static Logger logger = LoggerFactory.getLogger(GeneratorUtil.class);

    protected static Map<String, String> paramMap = new LinkedHashMap() {
        {
            put("useUnicode", "true");
            put("nullCatalogMeansCurrent", "true");
            put("characterEncoding", "UTF-8");
            put("zeroDateTimeBehavior", "convertToNull");
        }
    };

    /**
     * 获取配置路径
     *
     * @return
     */
    public static String getWbfcConfigPath() {
        if (null != wbfcEditorGenerator) {
            return wbfcEditorGenerator.getWbfcPath();
        }
        return "";
    }

    /**
     * 添加JavaDoc
     *
     * @param elem
     * @param doc
     */
    public static void addJavaDoc(JavaElement elem, String... doc) {
        if (ConverterUtil.isNotEmpty(elem, doc)) {
            StringBuilder sb = new StringBuilder();
            elem.addJavaDocLine("/**");
            for (String str : doc) {
                if (ConverterUtil.isNotEmpty(str)) {
                    sb.setLength(0);
                    sb.append(" * ");
                    sb.append(str);
                    elem.addJavaDocLine(sb.toString().replace("\n", " "));
                }
            }
            elem.addJavaDocLine(" */");
        }
    }

    /**
     * 添加JavaDoc
     *
     * @param elem
     * @param doc
     */
    public static void addJavaDoc(JavaElement elem, Date date, String... doc) {
        if (ConverterUtil.isNotEmpty(elem, doc)) {
            StringBuilder sb = new StringBuilder();
            elem.addJavaDocLine("/**");
            for (String str : doc) {
                if (ConverterUtil.isNotEmpty(str)) {
                    sb.setLength(0);
                    sb.append(" * ");
                    sb.append(str);
                    elem.addJavaDocLine(sb.toString().replace("\n", " "));
                }
            }
            if (ConverterUtil.isNotEmpty(date)) {
                sb.setLength(0);
                sb.append(" * ");
                sb.append(ConverterUtil.dateToString(date, ConverterUtil.FORMATE_DATE_TIME_24H));
                elem.addJavaDocLine(sb.toString().replace("\n", " "));
            }
            elem.addJavaDocLine(" */");
        }
    }

    public static void setWbfcConfig(WbfcConfig conf) {
        wbfcConfig = conf;
    }

    public static WbfcConfig getWbfcConfig() {
        return wbfcConfig;
    }

    public static void setWbfcEditorGenerator(WbfcEditorGenerator wbg) {
        wbfcEditorGenerator = wbg;
        logger.debug("test logger");
    }

    public static WbfcEditorGenerator getWbfcEditorGenerator() {
        return wbfcEditorGenerator;
    }

    public static String getTableRemarks(IntrospectedTable introspectedTable) {
        WbfcEditorGenerator gor = getWbfcEditorGenerator();
        if (null != gor) {
            String tableName = introspectedTable.getTableConfiguration().getTableName();
            return gor.getTableRemarks(tableName);
        }
        return "";
    }

    public static void setDbInfo() {
        if (null != wbfcConfig && null != wbfcEditorGenerator) {
            WbfcDbInfo dbInfo = wbfcEditorGenerator.getWbfcDbInfo();
            ConverterUtil.copyProperties(dbInfo, wbfcConfig);
        }
    }

    public static boolean getSimplePoVo(String tableName) {
        WbfcConfig config = getWbfcConfig();
        WbfcDataTable dataTable = config.getDataTable(tableName);
        if (null != dataTable) {
            return ConverterUtil.toBoolean(dataTable.getSimplePoVo());
        }
        return false;
    }

    /**
     * 生成代码前校验并制作具体配置
     *
     * @return
     */
    public static WbfcConfig beforeGenMakeConfig() {
        // 表
        setDbInfo();
        WbfcConfig wbfcConfig = getWbfcConfig();
        List<String> checkRes = DataCheckUtil.checkResultMsg(wbfcConfig);
        if (checkRes.size() > 0) {
            throw new VerifyException("005", checkRes.stream().collect(Collectors.joining(";")));
        }
        // 设置列
        List<String> tableStrList = Lists.newArrayList();
        for (WbfcDataTable dTable : wbfcConfig.getTablesCloumnList()) {
            List<WbfcDataColumn> colList = dTable.getColumns();
            // 在页面中自定义的列属性
            if (colList.size() > 0) {
                String detailTemplete = getColumnOverrideTempletes(colList);
                tableStrList.add(GeneratorUtil.getTableDom(dTable, detailTemplete, false, false, false, false));
            } else {
                // 默认的列属性
                List<TableColumn> tableColumnList = GeneratorUtil.getWbfcEditorGenerator().getTableColumn(dTable.getTableName());
                String detailTemplete = getDetailTemple(tableColumnList);
                tableStrList.add(GeneratorUtil.getTableDom(dTable, detailTemplete, false, false, false, false));
            }
        }
        wbfcConfig.setTablesList(tableStrList);

        return wbfcConfig;
    }

    public static void createDir(File f) {
        if (!f.exists()) {
            // 如果是绝对地址就获取父目录并创建
            if (f.isAbsolute()) {
                f.mkdirs();
            } else {
                // 如果是相对目录就获取绝对地址转换一下
                new File(f.getAbsolutePath()).mkdirs();
            }
        }
    }

    public static void makeAllPathDirs(WbfcConfig wbfc) {
        if (ConverterUtil.toBoolean(wbfc.getHasController())) {
            File cp = new File(wbfc.getControllerPath());
            createDir(cp);
        }
        if (ConverterUtil.toBoolean(wbfc.getHasService())) {
            File sp = new File(wbfc.getServicePath());
            createDir(sp);
        }
        if (ConverterUtil.toBoolean(wbfc.getHasPoVo())) {
            File pp = new File(wbfc.getPoPath());
            createDir(pp);
            File vp = new File(wbfc.getVoPath());
            createDir(vp);
        }
        File ep = new File(wbfc.getEntityPath());
        createDir(ep);
        File dp = new File(wbfc.getDaoPath());
        createDir(dp);
        File xp = new File(wbfc.getXmlPath());
        createDir(xp);

        Map<String, String> tempMap = Maps.newLinkedHashMap(paramMap);
        String url = wbfc.getDbUrl();
        // 拆分URL参数
        if (url.contains("?")) {
            // 覆盖默认参数
            tempMap.putAll(Splitter.on("&").withKeyValueSeparator("=").split(url));
            // 如果有参数就截取 只保留url部分
            url = url.substring(0, url.indexOf("?"));
            wbfc.setDbUrl(url);
        }
        wbfc.setDbUrlPropertyMap(tempMap);
    }


    public static String getColumnOverrideTempletes(List<WbfcDataColumn> colList) {
        List<String> sf = Lists.newArrayList();
        for (WbfcDataColumn over : colList) {
            String prop = "";
            for (WbfcColunmOverrideProperty pro : over.getProperties()) {
                prop += (pro.getName() + "=\"" + pro.getValue() + "\"");
            }
            sf.add(MessageFormat.format(TABLE_DETAIL_TEMPLETE, over.getColName(), prop));
        }
        return sf.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    public static String getDetailTemple(List<TableColumn> tcList) {
        List<String> sf = Lists.newArrayList();
        if (ConverterUtil.isNotEmpty(tcList)) {
            Iterator var2 = tcList.iterator();

            while (var2.hasNext()) {
                TableColumn tc = (TableColumn) var2.next();
                String dataType = tc.getTypeName().toLowerCase();
                if (dataType.contains("date") || dataType.contains("datatime")) {
                    sf.add(MessageFormat.format(TABLE_DETAIL_DATE_TEMPLETE, tc.getColumnName()));
                }
            }
        }

        return sf.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    public static IntrospectedTable getIntrospectedTable(Context context, String tableName) {
        try {
            Field f = context.getClass().getDeclaredField("introspectedTables");
            f.setAccessible(true);
            List<IntrospectedTable> introspectedTables = (List<IntrospectedTable>) f.get(context);
            for (IntrospectedTable et : introspectedTables) {
                if (tableName.equals(et.getTableConfiguration().getTableName())) {
                    return et;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("expcetion in getIntrospectedTable {}", e);
        }
        return null;
    }

    /**
     * 把column字符串转成Map
     *
     * @param columnStr
     * @return
     */
    public static Map<String, String> convColumnsToMap(String columnStr) {
        String columns = columnStr.trim().replace("=", ":");
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        // column结构 = {关联表.列名 = 主表.列名} 多个逗号分隔
        Map<String, String> columnMap = ConverterUtil.gson.fromJson(columns, type);
        return columnMap;
    }

    public static String getTableDom(WbfcDataTable dTable, String detail, boolean enableCountByExample, boolean enableSelectByExample, boolean enableUpdateByExample, boolean enableDeleteByExample) {
        List<String> sf = Lists.newArrayList();
        String tName = dTable.getTableName();
        String eName = dTable.getEntityName();
        sf.add(MessageFormat.format("        <table tableName=\"{0}\" domainObjectName=\"{1}\" enableCountByExample=\"{2}\" enableSelectByExample=\"{3}\" enableUpdateByExample=\"{4}\" enableDeleteByExample=\"{5}\">", tName, eName, enableCountByExample, enableSelectByExample, enableUpdateByExample, enableDeleteByExample));
        StringBuilder tableInfoBu = new StringBuilder();
        if (dTable.isRelation()) {
            tableInfoBu.append("isRelation=\"true\"");
            tableInfoBu.append(" ");
        }
        if (ConverterUtil.toBoolean(dTable.getBatchInsert())) {
            tableInfoBu.append("batchInsert=\"true\"");
            tableInfoBu.append(" ");
        }
        if (ConverterUtil.toBoolean(dTable.getBatchUpdate())) {
            tableInfoBu.append("batchUpdate=\"true\"");
            tableInfoBu.append(" ");
        }
        if (ConverterUtil.toBoolean(dTable.getBatchDelete())) {
            tableInfoBu.append("batchDelete=\"true\"");
            tableInfoBu.append(" ");
        }
        String tableAttr = tableInfoBu.toString();
        if (ConverterUtil.isNotEmpty(tableAttr)) {
            sf.add(MessageFormat.format("            <tableInfo {0}></tableInfo>", tableAttr));
        }

        if (ConverterUtil.isNotEmpty(detail)) {
            sf.add(detail);
        }
        sf.add("        </table>");
        return sf.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * 名称驼峰转换
     *
     * @param name
     * @return
     */
    public static String col2Pro(String name) {
        if (null != name && !"".equals(name)) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.toLowerCase());
        }
        return "";
    }
}
