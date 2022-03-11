package com.wisea.cloud.wbfceditor.generator.constants;

/**
 * /* *
 * 更新函数生成策略枚举
 *
 * @Author XuDL(Wisea)
 * @Date 2020/12/9 17:58
 */
public enum UpdateStrategyEnum {
    DELETE_INSERT("DELETE_INSERT", "先删后插"), ONLY_UPDATE("ONLY_UPDATE", "只更新");
    private String label;
    private String value;

    UpdateStrategyEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
