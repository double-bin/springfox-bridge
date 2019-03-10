package com.github.doublebin.springfox.bridge.core.builder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BridgeClassNameBuilder {
    private static ConcurrentHashMap<String, AtomicInteger> classNameMap = new ConcurrentHashMap<String, AtomicInteger>();

    public static final String NEW_REQUEST_CLASS_NAME_PRE = "bridge.model.request.";

    public static final String NEW_CONTROLLER_CLASS_NAME_PRE = "bridge.controllers.Bridge";

    public static final String NEW_SUB_CLASS_NAME_PRE = "bridge.model.sub.";

    public static final String NEW_SUB_CLASS_NAME_SUF = "Sub";

    public static String buildNewSubClassName(String simpleClassName) {
        return buildNewClassName(NEW_SUB_CLASS_NAME_PRE + simpleClassName + NEW_SUB_CLASS_NAME_SUF);
    }

    public static String buildNewClassName(String pre, String simpleClassName) {
        String orignFullClassName = pre + simpleClassName;
        return buildNewClassName(orignFullClassName);
    }

    public static String buildNewClassName(String orignFullClassName) {
        classNameMap.putIfAbsent(orignFullClassName, new AtomicInteger(0));
        int count = classNameMap.get(orignFullClassName).incrementAndGet();

        String newClassName = orignFullClassName;
        if (count > 1) {
            newClassName = orignFullClassName + (count - 1);
        }

        return newClassName;
    }


}
