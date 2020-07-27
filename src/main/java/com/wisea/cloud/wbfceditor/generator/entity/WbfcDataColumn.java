package com.wisea.cloud.wbfceditor.generator.entity;

import com.google.common.collect.Lists;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.model.annotation.Check;

import java.util.List;

public class WbfcDataColumn {
    @Check(test = "required", requiredMsg = "列名不能为空")
    private String colName;

    @Check(test = {"required", "regex"}, regex = "^([\\w]+=([^\\.][\\w]+[\\.]*)+;?)*$", requiredMsg = "配置属性不能为空", regexMsg = "配置属性格式不正确(多个使用逗号分隔 例:aa=yy;bb=nn)")
    private String colProperties;

    private List<WbfcColunmOverrideProperty> properties = Lists.newArrayList();

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColProperties() {
        return colProperties;
    }

    public void setColProperties(String colProperties) {
        this.colProperties = colProperties;
        this.properties.clear();
        if (ConverterUtil.isNotEmpty(colProperties)) {
            String[] propertyArray = colProperties.trim().split(";");
            for (String property : propertyArray) {
                if (ConverterUtil.isEmpty(property)) continue;
                String[] attrs = property.split("=");
                if (attrs.length == 2) {
                    addProperty(attrs[0], attrs[1]);
                }
            }
        }
    }


    public List<WbfcColunmOverrideProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<WbfcColunmOverrideProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(String name, String value) {
        WbfcColunmOverrideProperty add = new WbfcColunmOverrideProperty(name, value);
        this.properties.removeIf(a -> {
            return a.getName().equals(add.getName());
        });
        this.properties.add(add);

    }

}
