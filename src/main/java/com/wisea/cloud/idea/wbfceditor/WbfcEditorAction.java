package com.wisea.cloud.idea.wbfceditor;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.wisea.cloud.idea.wbfceditor.constants.Constants;
import com.wisea.cloud.idea.wbfceditor.ui.WbfcFxApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.Lists;

import java.net.MalformedURLException;
import java.util.List;


public class WbfcEditorAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {

//    // 获取工程上下文
//        Project project = e.getData(PlatformDataKeys.PROJECT);
//// 获取当前类文件
//        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
//        // 获取当前类文件的路径
//        //String classPath = psiFile.getVirtualFile().getPath();
//        String title = "Hello World!";
//
//        // 显示对话框
//        Messages.showMessageDialog(project, "这是一个对话框",title, Messages.getInformationIcon());
//        //Messages.showMessageDialog(project, classPath, title, Messages.getInformationIcon());
        PsiElement[] psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            Messages.showMessageDialog("至少选择一张表", Constants.TITLE_NOTICE, Messages.getInformationIcon());
            return;
        }

        List<DbTable> tableList = Lists.newArrayList();
        for (PsiElement psiElement : psiElements) {
            if (!(psiElement instanceof DbTable)) {
                Messages.showMessageDialog("所选择的必须是【表】类型", Constants.TITLE_NOTICE,  Messages.getInformationIcon());
                return;
            } else {
                tableList.add((DbTable)psiElement);
            }
        }

        boolean isSingleTable = psiElements.length == 1;
        //new MainUI(e, isSingleTable);
        WbfcFxApplication.setAnActionEvent(e);
        WbfcFxApplication.setTableList(tableList);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Stage stage = WbfcFxApplication.getStage();
                if(null == stage){
                    Application.launch(WbfcFxApplication.class);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                            WbfcFxApplication.reload();
                        }
                    });

                }
            }
        }).start();

    }




}
