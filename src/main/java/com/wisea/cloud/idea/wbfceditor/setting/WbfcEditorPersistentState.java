package com.wisea.cloud.idea.wbfceditor.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jdom.Element;

import static com.wisea.cloud.idea.wbfceditor.constants.Constants.SETTING_TITLE;

@State(name = SETTING_TITLE, storages = {
        @com.intellij.openapi.components.Storage("$APP_CONFIG$/wbfc_editor_setting.xml")})
public class WbfcEditorPersistentState implements PersistentStateComponent<Element> {
    private String path = "";

    WbfcEditorPersistentState() {

    }

    public static WbfcEditorPersistentState getInstance() {
        return ServiceManager.getService(WbfcEditorPersistentState.class);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    @Override
    public Element getState() {
        Element saveElem = new Element(SETTING_TITLE);
        saveElem.setAttribute("path", getPath());
        return saveElem;
    }

    @Override
    public void loadState(@NotNull Element state) {
        this.path = state.getAttributeValue("path");
    }
}
