package com.wisea.cloud.idea.wbfceditor.ui;

import com.wisea.cloud.idea.wbfceditor.generator.WbfcGenerator;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class WbfcFxApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * 从inputStream转字符串
     *
     * @param inputStream
     * @param charset
     * @return
     */
    public static String getContent(InputStream inputStream, String charset) {
        try {
            StringBuilder sf = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(inputStream, charset);
            BufferedReader br = new BufferedReader(isr);
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                sf.append(readLine);
            }
            br.close();
            return sf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        /*Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/templete/WbfcFx.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("JAVAFX嵌入html测试");
        primaryStage.setScene(new Scene(root, 1270, 860));
        primaryStage.show();*/
        final WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();

//        String content = null;
//        try {
//            content = getContent(new FileInputStream(new File(getClass().getResource("/templete/index.html").getFile())), "utf-8");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        //webEngine.loadContent(content, "text/html");

        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                 Worker.State newState) -> {

                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("WbfcGenerator",
                                new WbfcGenerator());//设置变量
                    }
                });
//        String content = null;
//        try {
//            content = getContent(new FileInputStream(new File(getClass().getResource("/templete/index.html").getFile())), "utf-8");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        webEngine.loadContent(content, "text/html;charset=UTF-8");
//        try {
//            webEngine.load(new File(getClass().getResource("/templete/index.html").toURI()).toURI().toURL().toString());
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
        webEngine.load("http://203.86.72.233:8088/wisea_dev/wbf-cloud/blob/master/wbf-cloud-common/src/main/resources/templates/emailFile/emailOverage.ftl");
        /*Button button1 = new Button("java调JS方法");
        button1.setOnAction(event -> {
            try {

                JSObject win = (JSObject) webEngine.executeScript("window");
                //webEngine.executeScript("show()");//执行js函数
                //win.call("show","a","b");
                win.eval("show('a','b')");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
        VBox stackPane = new VBox();
        stackPane.setSpacing(20);
//        stackPane.getChildren().addAll(button1,browser);
        stackPane.getChildren().addAll(browser);
        Scene scene = new Scene(stackPane, 600, 400);

        scene.setRoot(stackPane);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
