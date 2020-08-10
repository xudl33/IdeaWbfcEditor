package com.wisea.cloud.idea.wbfceditor.logger;

import com.wisea.cloud.idea.wbfceditor.ui.TestCharsetApplication;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import javafx.scene.web.WebEngine;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.text.BadLocationException;

public class LoggerAppender extends AppenderSkeleton {


    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    protected void append(LoggingEvent event) {
        String text = this.layout.format(event);
        WbfcFxApplication.appendLog(text);
        //TestCharsetApplication.appendLog(text);
    }
}
