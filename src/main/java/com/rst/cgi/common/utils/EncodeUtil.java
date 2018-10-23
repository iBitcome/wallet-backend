package com.rst.cgi.common.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by hujia on 2017/2/28.
 */
public class EncodeUtil {
    //byte数组转成16进制字符串
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    //16进制字符串转为byte数组
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static String xssEncode(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        } else {
            s = stripXSSAndSql(s);
        }

        //替换成中文字符
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '>':
                    sb.append("＞");// 转义大于号
                    break;
                case '<':
                    sb.append("＜");// 转义小于号
                    break;
                case '\'':
                    sb.append("＇");// 转义单引号
                    break;
                case '\"':
                    sb.append("＂");// 转义双引号
                    break;
                case '&':
                    sb.append("＆");// 转义&
                    break;
                case '#':
                    sb.append("＃");// 转义#
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String stripXSSAndSql(String value) {
        if (value != null) {
            // Avoid anything between script tags
            Pattern scriptPattern = Pattern.compile("<\\s*script\\s*>(.*?)</\\s*script\\s*>",
                    Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid anything in a src="http://www.yihaomen.com/article/java/..." type of e-xpression
            scriptPattern = Pattern.compile("src\\s*=\\s*[\\\"|\\\'](.*?)[\\\"|\\\']",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            scriptPattern = Pattern.compile("</\\s*script\\s*>",
                    Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome <script ...> tag
            scriptPattern = Pattern.compile("<\\s*script(.*?)>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid eval(...) expressions
            scriptPattern = Pattern.compile("eval\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid e-xpression(...) expressions
            scriptPattern = Pattern.compile("e-xpression\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid javascript:... expressions
            scriptPattern = Pattern.compile("javascript\\s*:\\s*",
                    Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid vbscript:... expressions
            scriptPattern = Pattern.compile("vbscript\\s*:\\s*",
                    Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid onload= expressions
            scriptPattern = Pattern.compile("onload(.*?)=",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
        }
        return value;
    }

    public static String letters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String hexNString(long number, int hex, int maxLen) {
        StringBuffer ret = new StringBuffer();
        int remainLen = maxLen;
        while(number > 0 && remainLen > 0) {
            int value = (int)(number % hex);
            ret.append(letters.charAt(value));
            --remainLen;
            number = number / hex;
        }

        for (int i = 0; i < remainLen; i++) {
            ret.append("0");
        }

        return ret.reverse().toString();
    }

    public static long hexStringToNumber(String value, int hex) {
        char[] chars = value.toCharArray();
        long number = 0;
        for (int i = 0; i < chars.length; i++) {
            int currentValue = letters.indexOf(chars[i]);
            number = number * hex + currentValue;
        }

        return number;
    }

    /**
     * 根据用户id生成唯一的用户标识，自行确保id的唯一性
     * @param id 必须小于36*36*14669600L才具有唯一性保证
     * @return 用户显示用的标识ID
     */
    public static String identifyFromId(long id) {
        /**
         * 实测小于14669600(3个字节)的字符串的hashCode不会重复
         */
        Long magicNumber = 14669600L;
        int hashCode = hashCode(id % magicNumber + "");
        String result = String.format("%010d", (long)(hashCode &0x8FFFFFFF) + 0x800000000L);
        result = hexNString(id / magicNumber, 36, 2) + result;

        result = "iBit:" + result;

        return result;
    }

    public static int hashCode(String value) {
        int h = 0;
        for (int i = 0; i < value.length(); i++) {
            h = 31 * h + value.charAt(i);
        }

        return h;
    }

    /**
     * 生成唯一的用户标识,最多每毫秒产生一个id, 600年内不重复
     * @return 用户显示用的标识ID
     */
    public static synchronized String generateIdentify() {
        /**
         * 实测小于14669600(3个字节)的字符串的hashCode不会重复
         */
        long id = System.currentTimeMillis();
        String result = identifyFromId((id - 1532336778275L) % (36*36*14669600L));

        //确保函数执行超过1ms
        long current = System.currentTimeMillis();
        if (current == id) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        Set<String> results = new HashSet<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            String result = generateIdentify();
            if (results.contains(result)) {
                System.out.println("failed:" + result);
            }
            results.add(result);
        }
        System.out.println(results);
        System.out.println("test success(cost:"+ (System.currentTimeMillis() - start) + ")!");
    }
}
