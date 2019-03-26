package com.github.doublebin.springfox.bridge.core.util;

import java.io.File;

import com.github.doublebin.springfox.bridge.core.exception.BridgeException;
import org.apache.commons.io.FileUtils;

public class FileUtil {
    private static String currentFilePath = "";

    public static String getCurrentFilePath() {
        if (StringUtil.isBlank(currentFilePath)) {
            File file = new File("");
            currentFilePath = file.getAbsolutePath();
        }
        return currentFilePath;
    }

    public static void main(String[] args) throws Exception {
        String classFilePath = FileUtil.getCurrentFilePath() + File.separator + "bridge-classes";
        FileUtils.deleteQuietly(new File(classFilePath));
        FileUtils.forceMkdir(new File(classFilePath));
    }
}
