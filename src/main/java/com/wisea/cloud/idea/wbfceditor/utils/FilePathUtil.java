package com.wisea.cloud.idea.wbfceditor.utils;

import com.intellij.openapi.util.io.FileUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FilePathUtil {
    /**
     * 转换为系统路径
     *
     * @param paths
     * @return
     */
    public static String getSyStemFilePath(String... paths) {
        return FileUtil.toSystemDependentName(Arrays.stream(paths).collect(Collectors.joining()));
    }
}
