package com.wisea.cloud.idea.wbfceditor.ui;

import com.intellij.database.psi.DbTable;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.StartupUiUtil;
import com.sun.javafx.webkit.WebConsoleListener;
import com.wisea.cloud.common.util.ConverterUtil;
import com.wisea.cloud.idea.wbfceditor.generator.WbfcGenerator;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.List;

public class WbfcFxApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static WbfcGenerator WbfcGenerator = new WbfcGenerator();
    private static List<DbTable> tableList = Lists.newArrayList();
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static AnActionEvent currentEvent = null;
    private static Stage stage = null;
    private static WebEngine webEngine = null;

    public static Stage getStage() {
        return stage;
    }

    public static void setAnActionEvent(AnActionEvent e) {
        currentEvent = e;
    }

    public static AnActionEvent getAnActionEvent() {
        if (null != currentEvent) {
            return currentEvent;
        }
        return null;
    }

    public static Project getProject() {
        if (null != currentEvent) {
            return currentEvent.getData(PlatformDataKeys.PROJECT);
        }
        return null;
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

    public static void setTableList(List<DbTable> tableList) {
        WbfcFxApplication.tableList = tableList;
    }

    public static List<DbTable> getTableList() {
        return tableList;
    }


    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        this.stage = primaryStage;
        GeneratorUtil.setWbfcEditorGenerator(WbfcGenerator);

        //primaryStage.setAlwaysOnTop(true);

        final WebView browser = new WebView();
        webEngine = browser.getEngine();

        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                 Worker.State newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        // 设置风格
                        setTheme();
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("wbfcGenerator", WbfcGenerator
                        );//设置变量
                    }
                });

        try {
            // webEngine.load(new File(getClass().getResource("/templete/index.html").toURI()).toURI().toURL().toString());
            webEngine.load(getClass().getResource("/templates/index.html").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 注册控制台日志监听器
        WebConsoleListener.setDefaultListener(new WebConsoleListener() {
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                logger.debug("WebView Console.log: {} 【{} - {}】", message, sourceId, lineNumber);
            }
        });


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                hide();
            }
        });

        /*System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                String text = String.valueOf((char) b);
                Platform.runLater(() -> {
                    if( text.startsWith("WebView Console.log")) return;
                    text.replaceAll("\\s*|\t|\r", "\n");
                    webEngine.executeScript("appendText('" + text +"')");
                });
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                Platform. runLater(() -> {
                    if( text.startsWith("WebView Console.log")) return;
                    text.replaceAll("\\s*|\t|\r", "\n");
                    webEngine.executeScript("appendText('" + text +"')");
                });
            }
        }, true));*/

//        final SwingNode swingNode = new SwingNode();
//        createAndSetSwingContent(swingNode);
//        StackPane stackPane = new StackPane();
//        stackPane.getChildren().addAll(swingNode, browser);
        Scene scene = new Scene(browser, 1300, 800);
        // 如果是暗黑风格就使用暗黑css
//        if (cssName.equalsIgnoreCase("Darcula")) {
//            // webEngine.setUserStyleSheetLocation(cssFile);
////            try {
//            Document doc = webEngine.getDocument();
//            Element node = doc.createElement("link");
//            node.setAttribute("type", "text/css");
//            node.setAttribute("rel", "stylesheet");
//            node.setAttribute("href", "css/darcula.css");
//            doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(node);
//
////            webEngine.getDocument().createElement()
////            browser.getStylesheets().setAll(getClass().getResource("/templates/css/darcula.css").toString());
//            //webEngine.setUserStyleSheetLocation(getClass().getResource("/templates/css/darcula.css").toString());
////            browser.getStylesheets();
////            browser.getStylesheets().addAll("css/darcula.css");
//            //StyleManager.loadStylesheet("/templates/css/" + cssName + ".css");
//            //StyleManager.getInstance().addUserAgentStylesheet(scene, "/templates/css/" + cssName + ".css");
////                String cssFile = new File(getClass().getResource("/templates/css/" + cssName + ".css").toURI()).toURI().toURL().toString();
////                cssFile = cssFile.replace("file://", "file:///");
//            //scene.getStylesheets().addAll("templates/);
////            } catch (URISyntaxException e) {
////                e.printStackTrace();
////            }
//        }
        //设置窗口的图标.
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icons/icon_16x16.png")));
        primaryStage.setTitle("WbfcEditor - Java codes generator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void setTheme() {
        if (null != webEngine) {
            UIManager.LookAndFeelInfo info = LafManager.getInstance().getCurrentLookAndFeel();
            String cssName = info.getName().toLowerCase();
            // 如果是暗黑风格就使用暗黑css
            if (StartupUiUtil.isUnderDarcula()) {
                Document doc = webEngine.getDocument();
                Element node = doc.createElement("link");
                node.setAttribute("type", "text/css");
                node.setAttribute("rel", "stylesheet");
                node.setAttribute("href", "css/darcula.css");
                doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(node);
            }
        }
    }

    private void createAndSetSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(new JButton("Click me!"));
            }
        });
    }

    public static void hide() {
        if (null != stage) {
            stage.hide();
        }
    }

    public static void reload() {
        if (null != webEngine) {
            setTheme();
            webEngine.reload();
        }
    }

    public static void appendLog(String text) {
        if (ConverterUtil.isNotEmpty(webEngine, text)) {
            doScript("appendText('" + text + "')");
        }
    }

    public static void setDiyXml(String text) {
        if (ConverterUtil.isNotEmpty(webEngine, text)) {
            doScript("setDiyXml('" + text + "')");
        }
    }

    public static void setGenerateStatus(String text) {
        if (ConverterUtil.isNotEmpty(webEngine, text)) {
            doScript("setGenerateStatus('" + text + "')");
        }
    }

    public static void setFormVal(String name, String val) {
        if (ConverterUtil.isNotEmpty(webEngine, name)) {
            doScript("setFormVal('" + name + "', '" + val + "')");
        }
    }

    public static void doScript(String javascriptCmd) {
        Platform.runLater(() -> {
            webEngine.executeScript(javascriptCmd);
        });
    }
}
