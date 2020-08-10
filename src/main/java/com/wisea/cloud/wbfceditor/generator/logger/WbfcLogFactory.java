package com.wisea.cloud.wbfceditor.generator.logger;

import com.intellij.openapi.diagnostic.Logger;
import org.mybatis.generator.logging.AbstractLogFactory;
import org.mybatis.generator.logging.Log;


public class WbfcLogFactory implements AbstractLogFactory {

    public WbfcLogFactory() {
    }

    @Override
    public Log getLog(Class<?> targetClass) {
        return new WbfcEditorLogger(targetClass);
    }
}