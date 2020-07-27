package com.wisea.cloud.wbfceditor.generator.util;

import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.WbfcEditorGenerator;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDbInfo;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.JavaElement;

import java.io.File;
import java.util.Date;

public class GeneratorUtil {

    private static WbfcConfig wbfcConfig = new WbfcConfig();

    private static WbfcEditorGenerator wbfcEditorGenerator = null;

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

    public static void setWbfcConfig(WbfcConfig conf){
        wbfcConfig = conf;
    }

    public static WbfcConfig getWbfcConfig(){
        return wbfcConfig;
    }

    public static void setWbfcEditorGenerator(WbfcEditorGenerator wbg){
        wbfcEditorGenerator = wbg;
    }
    public static WbfcEditorGenerator getWbfcEditorGenerator(){
        return wbfcEditorGenerator;
    }

    public static String getTableRemarks(IntrospectedTable introspectedTable) {
        WbfcEditorGenerator gor = getWbfcEditorGenerator();
        if(null != gor){
            String tableName = introspectedTable.getTableConfiguration().getTableName();
            return gor.getTableRemarks(tableName);
        }
        return "";
    }

    public static void setDbInfo() {
        if(null != wbfcConfig && null != wbfcEditorGenerator){
            WbfcDbInfo dbInfo = wbfcEditorGenerator.getWbfcDbInfo();
            ConverterUtil.copyProperties(dbInfo, wbfcConfig);
        }
    }

}
