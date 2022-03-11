package com.wisea.cloud.wbfceditor.generator;

import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Context;

import java.util.List;

public class WbfcOneToAnyPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }
}
