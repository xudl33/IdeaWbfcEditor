package com.wisea.cloud.idea.wbfceditor.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class WbfcFxController  implements Initializable {
    @FXML
    private WebView webView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final WebEngine webengine = webView.getEngine();
        String url = WbfcFxApplication.class.getResource("/templete/index.html").toExternalForm();
        webengine.load(url);
    }

}
