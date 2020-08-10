package com.wisea.cloud.wbfceditor.generator.logger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.wisea.cloud.common.util.Exceptions;
import com.wisea.cloud.common.util.LoggerUtil;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.logging.Log;

public class WbfcEditorLogger extends Logger implements Log {
    private Logger logger = null;

    public WbfcEditorLogger(Class<?> targetClass) {
        logger = Logger.getInstance(targetClass);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        String tempMsg = LoggerUtil.getLogger() + " DEBUG " + message;
        WbfcFxApplication.appendLog(tempMsg);
        if (isDebugEnabled()) {
            logger.debug(message);
        }
    }

    @Override
    public void debug(@Nullable Throwable t) {
        String tempMsg = LoggerUtil.getLogger() + " DEBUG " + Exceptions.getStackTraceAsString(t);
        WbfcFxApplication.appendLog(tempMsg);
        if (isDebugEnabled()) {
            logger.debug(t);
        }
    }

    @Override
    public void debug(String message, @Nullable Throwable t) {
        String tempMsg = LoggerUtil.getLogger() + " DEBUG " + message + ": " + Exceptions.getStackTraceAsString(t);
        WbfcFxApplication.appendLog(tempMsg);
        if (isDebugEnabled()) {
            logger.debug(message, t);
        }
    }

    @Override
    public void info(String message) {
        String tempMsg = LoggerUtil.getLogger() + " INFO " + message;
        WbfcFxApplication.appendLog(tempMsg);
        logger.info(message);
    }

    @Override
    public void info(String message, @Nullable Throwable t) {
        String tempMsg = LoggerUtil.getLogger() + " INFO " + message + ": " + Exceptions.getStackTraceAsString(t);
        WbfcFxApplication.appendLog(tempMsg);
        logger.info(message, t);
    }

    @Override
    public void warn(String message, @Nullable Throwable t) {
        String tempMsg = LoggerUtil.getLogger() + " WARN " + message + ": " + Exceptions.getStackTraceAsString(t);
        WbfcFxApplication.appendLog(tempMsg);
        logger.warn(message, t);
    }

    @Override
    public void error(String message, @Nullable Throwable t, @NotNull String @NotNull ... details) {

        String fullMessage = details.length > 0 ? message + "\nDetails: " + StringUtil.join(details, "\n") : message;
        String tempMsg = LoggerUtil.getLogger() + " ERROR " + fullMessage + ": " + (Exceptions.getStackTraceAsString(t));
        WbfcFxApplication.appendLog(tempMsg);
        logger.error(fullMessage, t);
    }

    @Override
    public void setLevel(Level level) {
        logger.setLevel(level);
    }
}
