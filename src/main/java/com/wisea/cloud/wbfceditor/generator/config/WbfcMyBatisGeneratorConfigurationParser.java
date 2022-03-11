package com.wisea.cloud.wbfceditor.generator.config;

import com.google.common.collect.Lists;
import com.wisea.cloud.common.util.ConverterUtil;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.MyBatisGeneratorConfigurationParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Mybatis转换器 覆盖原MyBatisGeneratorConfigurationParser
 */
public class WbfcMyBatisGeneratorConfigurationParser extends MyBatisGeneratorConfigurationParser {
    public WbfcMyBatisGeneratorConfigurationParser(Properties extraProperties) {
        super(extraProperties);
    }

    @Override
    protected void parseTable(Context context, Node node) {
        super.parseTable(context, node);
        // 重新读取生成的config
        int lastIndex = context.getTableConfigurations().size() - 1;
        TableConfiguration tc = context.getTableConfigurations().get(lastIndex);
        WbfcTableConfiguration wbfcTd = new WbfcTableConfiguration(context);
        ConverterUtil.copyProperties(tc, wbfcTd);
        // 进行自定义的属性设置
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("joinTable".equals(childNode.getNodeName())) {
                parseJoinTable(wbfcTd, childNode, Lists.newArrayList(tc.getTableName()));
            } else if ("tableInfo".equals(childNode.getNodeName())) {
                parseTableInfo(wbfcTd, childNode);
            }
        }
        // 替换TableConfiguration
        context.getTableConfigurations().set(lastIndex, wbfcTd);
    }

    /**
     * tableInfo转换
     *
     * @param tc
     * @param node
     */
    protected void parseTableInfo(WbfcTableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String simplePoVo = attributes.getProperty("simplePoVo");
        tc.getTableInfo().setSimplePoVo(simplePoVo);
        String batchInsert = attributes.getProperty("batchInsert");
        tc.getTableInfo().setBatchInsert(batchInsert);
        String batchUpdate = attributes.getProperty("batchUpdate");
        tc.getTableInfo().setBatchUpdate(batchUpdate);
        String batchDelete = attributes.getProperty("batchDelete");
        tc.getTableInfo().setBatchDelete(batchDelete);
        String isRelation = attributes.getProperty("isRelation");
        tc.getTableInfo().setRelation(ConverterUtil.toBoolean(isRelation));
    }


    /**
     * oneToMany转换
     *
     * @param tc
     * @param node
     */
    protected void parseJoinTable(WbfcTableConfiguration tc, Node node, List<String> parents) {
        Properties attributes = parseAttributes(node);
        String tableName = attributes.getProperty("tableName");

        JoinTable co = getJoinTable(attributes, tableName);
        co.addParents(parents);
        // 递归设置子项
        setSubJoinTable(tc, node, co);

        tc.addJoinTable(co);
    }

    /**
     * 递归设置子项
     *
     * @param tc
     * @param node
     * @param co
     */
    private void setSubJoinTable(WbfcTableConfiguration tc, Node node, JoinTable co) {
        // 循环子元素 如果有子查询则继续添加
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node subNode = node.getChildNodes().item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE && subNode.getNodeName().equalsIgnoreCase("joinTable")) {
                Properties subAttributes = parseAttributes(subNode);
                String subTableName = subAttributes.getProperty("tableName");
                JoinTable subCo = getJoinTable(subAttributes, subTableName);
                subCo.addParents(co.getParents());
                subCo.addParent(co.getTableName());
                co.addJoinList(subCo);
                if (subNode.getChildNodes().getLength() > 0) {
                    setSubJoinTable(tc, subNode, subCo);
                }
            }
        }
    }

    @NotNull
    public JoinTable getJoinTable(Properties attributes, String tableName) {
        JoinTable co = new JoinTable();
        if (stringHasValue(tableName)) {
            co.setTableName(tableName);
        }

        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            co.setType(type);
        }

        String columns = attributes.getProperty("columns");
        if (stringHasValue(columns)) {
            co.setColumns(columns);
        }

        String isCasecode = attributes.getProperty("isCasecode");
        if (stringHasValue(isCasecode)) {
            co.setCasecade(ConverterUtil.toBoolean(isCasecode));
        }

        return co;
    }
}
