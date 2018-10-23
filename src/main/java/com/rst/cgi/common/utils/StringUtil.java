package com.rst.cgi.common.utils;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hujia on 2016/12/15.
 */
public class StringUtil {
    public static boolean isStringInArray(String str, String[] array){
        for (String val:array){
            if(str.equals(val)){
                return true;
            }
        }
        return false;
    }

    public static final char UNDERLINE='_';
    public static String camelToUnderline(String param){
        if (param == null||"".equals(param.trim())){
            return "";
        }

        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        sb.append(Character.toLowerCase(param.charAt(0)));
        for (int i = 1; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)){
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String underlineToCamel(String param){
        if (param == null || "".equals(param.trim())){
            return "";
        }

        int len = param.length();
        StringBuilder sb=new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE){
                if (++i<len){
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String underlineToCamel2(String param){
        if (param == null || "".equals(param.trim())){
            return "";
        }

        StringBuilder sb = new StringBuilder(param);
        Matcher mc= Pattern.compile("_").matcher(param);
        int i = 0;
        while (mc.find()){
            int position = mc.end() - (i++);
            sb.replace(position-1, position+1, sb.substring(position, position+1).toUpperCase());
        }
        return sb.toString();
    }

    public static String toHexString(byte[] input, int offset, int length, boolean withPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (withPrefix) {
            stringBuilder.append("0x");
        }
        for (int i = offset; i < offset + length; i++) {
            stringBuilder.append(String.format("%02x", input[i] & 0xFF));
        }

        return stringBuilder.toString();
    }

    public static String toHexString(byte[] b) {
        return toHexString(b, 0, b.length, false);
    }

    public static String Bit32(String SourceString) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(SourceString.getBytes());
        byte messageDigest[] = digest.digest();
        return toHexString(messageDigest);
    }

    public static String Bit16(String SourceString) throws Exception {
        return Bit32(SourceString).substring(8, 24);
    }

    private static String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
    private static Pattern p = Pattern.compile(regExp);

    public static boolean isChinaPhoneLegal(String str) {
        return p.matcher(str).matches();
    }
}
