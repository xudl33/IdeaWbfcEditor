package com.wisea.cloud.wbfceditor.generator.entity;

import com.google.common.collect.Maps;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.wbfceditor.generator.config.JoinTable;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;

public class RelationInfo {
    private Map<String, List<JoinTable>> oneToManyMaps = Maps.newHashMap();
    private Map<String, List<String>> oneToOneMaps = Maps.newHashMap();

    /**
     * 获取OneToMany
     *
     * @param table
     * @return
     */
    public List<JoinTable> getOneToManys(String table) {
        return oneToManyMaps.get(table);
    }

    /**
     * 添加到OneToMany
     *
     * @param table
     * @param joinTable
     * @return
     */
    public Map<String, List<JoinTable>> addOneToMany(String table, JoinTable joinTable) {
        if (ConverterUtil.isNotEmpty(table, joinTable)) {
            List<JoinTable> joinLst = oneToManyMaps.get(table);
            if (ConverterUtil.isEmpty(joinLst)) {
                joinLst = Lists.newArrayList();
            }
            joinLst.add(joinTable);
            oneToManyMaps.put(table, joinLst);
        }
        return oneToManyMaps;
    }
}
