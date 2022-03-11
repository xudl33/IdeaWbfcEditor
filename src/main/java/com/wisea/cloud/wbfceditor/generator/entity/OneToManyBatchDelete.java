package com.wisea.cloud.wbfceditor.generator.entity;

public class OneToManyBatchDelete {
    private String javaType;
    private String joinType;
    private String javaTypeAttr;
    private String doParName;
    private String setPrModName;
    private String getPrModName;
    private String loopRep = "";
    private String mapperName;

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String getJavaTypeAttr() {
        return javaTypeAttr;
    }

    public void setJavaTypeAttr(String javaTypeAttr) {
        this.javaTypeAttr = javaTypeAttr;
    }

    public String getDoParName() {
        return doParName;
    }

    public void setDoParName(String doParName) {
        this.doParName = doParName;
    }

    public String getSetPrModName() {
        return setPrModName;
    }

    public void setSetPrModName(String setPrModName) {
        this.setPrModName = setPrModName;
    }

    public String getGetPrModName() {
        return getPrModName;
    }

    public void setGetPrModName(String getPrModName) {
        this.getPrModName = getPrModName;
    }

    public String getLoopRep() {
        return loopRep;
    }

    public void setLoopRep(String loopRep) {
        this.loopRep = loopRep;
    }

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }
}
