package com.wisea.cloud.wbfceditor.generator;

import com.google.common.collect.Lists;
import com.wisea.cloud.common.exception.VerifyException;
import com.wisea.cloud.common.mybatis.generator.MybatisGeneratorTables;
import com.wisea.cloud.common.mybatis.generator.TableColumn;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.common.util.DataCheckUtil;
import com.wisea.cloud.common.util.FtlManagerUtil;
import com.wisea.cloud.common.util.IOUtils;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcColunmOverrideProperty;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcConfig;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataColumn;
import com.wisea.cloud.wbfceditor.generator.entity.WbfcDataTable;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class WbfcEditorRunner {
    private static final Logger logger = LoggerFactory.getLogger(WbfcEditorRunner.class);
    /** table详细模板 */
    public static String TABLE_DETAIL_TEMPLETE = "<columnOverride column=\"{0}\" {1} />";
    public static String TABLE_DETAIL_DATE_TEMPLETE = "<columnOverride column=\"{0}\" javaType=\"java.time.OffsetDateTime\"/>";

    public static void generatorFiles() {
        // 表
        GeneratorUtil.setDbInfo();
        WbfcConfig wbfcConfig = GeneratorUtil.getWbfcConfig();

//        wbfcConfig.setHasController(ConverterUtil.toString(conf.isHasController()));
//        wbfcConfig.setHasService(ConverterUtil.toString(conf.isHasService()));
//        wbfcConfig.setHasMapper(ConverterUtil.toString(conf.isHasMapper()));
//        wbfcConfig.setHasPoVo(ConverterUtil.toString(conf.isHasPoVo()));
//        List<String> tablesList = Lists.newArrayList();
//        // 设置选中的tables
//        if (null != tableConf) {
//            WbfcDataTable wbfcDataTable = conf.getSelectTables().get(0);
//            String detailTemplete = getColumnOverrideTempletes(tableConf);
//            tablesList.add(MybatisGeneratorTables.getTableDom(wbfcDataTable.getName(), tableConf.getEntityName(), detailTemplete, false, false, false, false));
//        } else {
//            for (WbfcDataTable wbfcDataTable : conf.getSelectTables()) {
//                String detailTemplete = MybatisGeneratorTables.getDetailTemple(wbfcDataTable.getTableColumns());
//                tablesList.add(MybatisGeneratorTables.getTableDom(wbfcDataTable.getName(), detailTemplete, false, false, false, false));
//            }
//        }
//        wbfcConfig.setTablesList(tablesList);
        List<String> checkRes = DataCheckUtil.checkResultMsg(wbfcConfig);
        if(checkRes.size() > 0) {
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

        // 在插件运行目录生成临时文件
        String rootPath = new File("WbfcEditorTempFiles").getAbsolutePath();

        File newGenerator = new File(rootPath + File.separator + "mybatisGenerator.xml");
        IOUtils.createFileParents(newGenerator);
        try {
            if(newGenerator.exists()){
                newGenerator.delete();
            }
            FtlManagerUtil.ftlPath = "/templates/";
            FtlManagerUtil.createTotal(newGenerator, wbfcConfig, "generatorConfig.ftl");

            // 生成各种代码文件
            List<String> warnings = Lists.newArrayList();
            boolean overwrite = true;
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(newGenerator);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);

        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error("exception with WbfcEditorRunner.generatorFiles {}", e1);
            throw new RuntimeException(e1);
        } finally {
            //newGenerator.delete();
        }
        // 删除临时文件
        // if (null != newGenerator) {
        // newGenerator.delete();
        // }
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

            while(var2.hasNext()) {
                TableColumn tc = (TableColumn)var2.next();
                String dataType = tc.getTypeName().toLowerCase();
                if (dataType.contains("date") || dataType.contains("datatime")) {
                    sf.append(MessageFormat.format(TABLE_DETAIL_DATE_TEMPLETE, tc.getColumnName()));
                }
            }
        }

        return sf.toString();
    }
}
