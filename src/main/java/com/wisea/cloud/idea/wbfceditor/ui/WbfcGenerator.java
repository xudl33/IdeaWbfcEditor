package com.wisea.cloud.idea.wbfceditor.ui;

import java.util.ArrayList;
import java.util.List;

public class WbfcGenerator {
    public List<String> getTableColumn(String tableName){
        List<String> res = new ArrayList<>();
        res.add("sys_dict");
        res.add("sys_system");
        res.add("sys_user");
        return res;
    }
}
