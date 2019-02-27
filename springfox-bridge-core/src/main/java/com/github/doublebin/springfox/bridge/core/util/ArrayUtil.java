package com.github.doublebin.springfox.bridge.core.util;

import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

    public static List<?> asList(Object array) {
        if (null == array) {
            return null;
        }
        if (array instanceof Object[]) {
            return Arrays.asList((Object[]) array);
        } else if (array instanceof boolean[]) {
            return Arrays.asList((boolean[]) array);
        } else if (array instanceof byte[]) {
            return Arrays.asList((byte[]) array);
        } else if (array instanceof char[]) {
            return Arrays.asList((char[]) array);
        } else if (array instanceof double[]) {
            return Arrays.asList((double[]) array);
        } else if (array instanceof float[]) {
            return Arrays.asList((float[]) array);
        } else if (array instanceof int[]) {
            return Arrays.asList((int[]) array);
        } else if (array instanceof long[]) {
            return Arrays.asList((long[]) array);
        } else if (array instanceof short[]) {
            return Arrays.asList((short[]) array);
        }
        return null;
    }

    public static boolean isArray(Object array) {
        if (null == array) {
            return false;
        }
        if (array instanceof Object[]) {
            return true;
        } else if (array instanceof boolean[]) {
            return true;
        } else if (array instanceof byte[]) {
            return true;
        } else if (array instanceof char[]) {
            return true;
        } else if (array instanceof double[]) {
            return true;
        } else if (array instanceof float[]) {
            return true;
        } else if (array instanceof int[]) {
            return true;
        } else if (array instanceof long[]) {
            return true;
        } else if (array instanceof short[]) {
            return true;
        }
        return false;
    }
}
