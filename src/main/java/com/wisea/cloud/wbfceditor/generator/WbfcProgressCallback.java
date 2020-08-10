package com.wisea.cloud.wbfceditor.generator;

import com.intellij.openapi.diagnostic.Logger;
import org.mybatis.generator.internal.NullProgressCallback;
import org.mybatis.generator.logging.Log;


public class WbfcProgressCallback extends NullProgressCallback {

    private Logger log;
    private boolean verbose;

    public WbfcProgressCallback(Logger log, boolean verbose) {
        super();
        this.log = log;
        this.verbose = verbose;
    }

    @Override
    public void startTask(String subTaskName) {
        if (verbose) {
            log.info(subTaskName);
        }
    }
}
