package com.wisea.cloud.idea.wbfceditor.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.wisea.cloud.idea.wbfceditor.setting.WbfcEditorPersistentState;
import com.wisea.cloud.wbfceditor.generator.util.GeneratorUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.io.File;

import static com.wisea.cloud.idea.wbfceditor.constants.Constants.SETTING_TITLE;

public class WbfcEditorSettingConfigurable implements SearchableConfigurable {

    private WbfcEditorPersistentState storeState = WbfcEditorPersistentState.getInstance();

    public String path = "";
    protected JPanel settingPanel;
    protected JLabel pathLabel;
    protected JButton brownBtn;
    protected JBTextField pathField;


    @Override
    public @NotNull String getId() {
        return SETTING_TITLE;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return SETTING_TITLE;
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (null != settingPanel) {
            settingPanel.repaint();
            return settingPanel;
        }
        settingPanel = new JPanel();
        pathLabel = new JLabel("Wbfc Editor Cache Path");
        pathField = (new ExtendableTextField(50)).addBrowseExtension(() -> {
            showDialog();
        }, (Disposable) null);
        Project project = ProjectUtil.guessCurrentProject(settingPanel.getRootPane());
        if (null != project) {
            VirtualFile virtualFile = project.getWorkspaceFile();
            if (null != virtualFile) {
                String path = GeneratorUtil.getWbfcConfigPath();
                pathField.setText(path);
            }
        }
//        pathField.setTextToTriggerEmptyTextStatus("Default");
//        pathField.getEmptyText().appendText(workspaceDir.getAbsolutePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        settingPanel.add(pathLabel);
        settingPanel.add(pathField);
        return settingPanel;
    }

    private void showDialog() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), (Project) null, (VirtualFile) null);
        if (file != null) {
            pathField.setText(FileUtil.toSystemDependentName(file.getPath()) + File.separator + "wbfceditor");
        }
    }

    @Override
    public boolean isModified() {
        return !pathField.getText().equals(storeState.getPath());
    }

    @Override
    public void apply() throws ConfigurationException {
        storeState.setPath(pathField.getText());
    }
}
