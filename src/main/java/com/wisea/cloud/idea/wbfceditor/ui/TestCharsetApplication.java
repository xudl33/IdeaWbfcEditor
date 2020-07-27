package com.wisea.cloud.idea.wbfceditor.ui;


import com.sun.javafx.webkit.WebConsoleListener;
import com.wisea.cloud.idea.wbfceditor.generator.TestGreneator;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
import java.net.URL;


public class TestCharsetApplication extends Application {
    private static TestGreneator wbfcGenerator = new TestGreneator();
    /**
     * 用于与Javascript引擎通信。
     */
    private JSObject javascriptConnector;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = new File(getClass().getResource("/templates/index.html").toURI()).toURI().toURL();

        WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();

        // 设置Java的监听器
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (Worker.State.SUCCEEDED == newValue) {
                // 在web引擎页面中设置一个名为“javaConnector”的接口对象
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("wbfcGenerator",wbfcGenerator);//设置变量
                //window.setMember("wbfcGenerator", new Wb);
                // 获取Javascript连接器对象。
                //javascriptConnector = (JSObject) webEngine.executeScript("getJsConnector()");
            }
        });
        WebConsoleListener.setDefaultListener(new WebConsoleListener() {
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                System.out.println("来自webview: " + message + " 【" + sourceId + " - " + lineNumber + "】");
            }
        });

        Scene scene = new Scene(webView, 1300, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 这里加载页面
        webEngine.load(getClass().getResource("/templates/index.html").toExternalForm());
    }
}
