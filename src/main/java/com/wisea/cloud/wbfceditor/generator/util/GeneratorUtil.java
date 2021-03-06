package com.wisea.cloud.wbfceditor.generator.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wisea.cloud.common.exception.VerifyException;
import com.wisea.cloud.common.mybatis.generator.MybatisGeneratorTables;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.common.util.DataCheckUtil;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorGenerator;
import com.wisea.cloud.wbfceditor.generator.entity.*;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GeneratorUtil {
    /**
     * table详细模板
     */
    public static String TABLE_DETAIL_TEMPLETE = "<columnOverride column=\"{0}\" {1} />";
    public static String TABLE_DETAIL_DATE_TEMPLETE = "<columnOverride column=\"{0}\" javaType=\"java.time.OffsetDateTime\"/>";

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
                tableStrList.add(MybatisGeneratorTables.getTableDom(dTable.getTableName(), dTable.getEntityName(), detailTemplete, false, false, false, false));
            } else {
                // 默认的列属性
                List<TableColumn> tableColumnList = GeneratorUtil.getWbfcEditorGenerator().getTableColumn(dTable.getTableName());
                String detailTemplete = getDetailTemple(tableColumnList);
                tableStrList.add(MybatisGeneratorTables.getTableDom(dTable.getTableName(), dTable.getEntityName(), detailTemplete, false, false, false, false));
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
        StringBuilder sf = new StringBuilder();
        for (WbfcDataColumn over : colList) {
            String prop = "";
            for (WbfcColunmOverrideProperty pro : over.getProperties()) {
                prop += (pro.getName() + "=\"" + pro.getValue() + "\"");
            }
            sf.append(MessageFormat.format(TABLE_DETAIL_TEMPLETE, over.getColName(), prop));
        }
        return sf.toString();
    }

    public static String getDetailTemple(List<TableColumn> tcList) {
        StringBuilder sf = new StringBuilder();
        if (ConverterUtil.isNotEmpty(tcList)) {
            Iterator var2 = tcList.iterator();

            while (var2.hasNext()) {
                TableColumn tc = (TableColumn) var2.next();
                String dataType = tc.getTypeName().toLowerCase();
                if (dataType.contains("date") || dataType.contains("datatime")) {
                    sf.append(MessageFormat.format(TABLE_DETAIL_DATE_TEMPLETE, tc.getColumnName()));
                }
            }
        }

        return sf.toString();
    }
}
