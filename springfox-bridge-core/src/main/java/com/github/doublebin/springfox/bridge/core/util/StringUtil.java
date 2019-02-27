package com.github.doublebin.springfox.bridge.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@Slf4j
public class StringUtil extends StringUtils
{
    private static final char SEPARATOR = '_';

    public static String bytes2HexString(byte buf[])
    {
        if (null == buf || buf.length <= 0)
        {
            log.error("Convert failed, parameter byte buf[] is null.");
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++)
        {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() < 2)
            {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString)
    {
        if (StringUtils.isEmpty(hexString))
        {
            log.error("Convert failed, parameter String hexStringis empty.");
            return new byte[0];
        }
        hexString = hexString.toUpperCase(Locale.US);
        int length = hexString.length() / 2;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int high = Integer.parseInt(hexString.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexString.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte)(high * 16 + low);
        }
        return result;
    }

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        //s = s.toLowerCase();

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;

        char first = s.charAt(0);
        if(SEPARATOR != first){
            sb.append(Character.toLowerCase(first));
        } else {
            upperCase = true;
        }

        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);

            if (upperCase && c != SEPARATOR) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else if (c == SEPARATOR) {
                upperCase = true;
            }else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String toUnderScoreCase(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;

        char current = '0';
        char last = current;

        for (int i = 0; i < s.length(); i++) {
            current = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(current) && last != SEPARATOR) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(current));
            last = current;
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String a = "HelloWord";
        System.out.println(toCamelCase(a));
        System.out.println(toCapitalizeCamelCase(a));
        String b = "Hello_Word";
        System.out.println(toCamelCase(b));
        System.out.println(toCapitalizeCamelCase(b));
        String c = "hello_word";
        System.out.println(toCamelCase(c));
        System.out.println(toCapitalizeCamelCase(c));

        System.out.println(toUnderScoreCase(a));
        System.out.println(toUnderScoreCase(b));
        System.out.println(toUnderScoreCase(c));
        System.out.println(toUnderScoreCase("helloWOrd"));
        System.out.println(toCamelCase(toUnderScoreCase("helloWOrd")));
    }

}
