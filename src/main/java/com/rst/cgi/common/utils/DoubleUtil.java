package com.rst.cgi.common.utils;

import java.text.NumberFormat;

public class DoubleUtil {
    public static String format(Double v, Integer decimalNum) {
        NumberFormat nfm = NumberFormat.getInstance();
        nfm.setGroupingUsed(false);

        String value = null;
        if (v.toString().contains("E")) {
            value = nfm.format(v);
        } else {
            value = v.toString();
        }

        if (decimalNum != null) {
            StringBuilder valueBuild = new StringBuilder(value);
            if(value.contains(".")){
                String decimal = value.split("\\.")[1];
                if (decimal.length() < decimalNum) {
                    for (int i = 0; i < decimalNum - decimal.length(); i++) {
                        valueBuild.append("0");
                    }
                }
            } else {
                valueBuild.append(".");
                for (int i = 0; i < decimalNum; i++) {
                    valueBuild.append("0");
                }
            }

            return valueBuild.toString();
        } else {
            return value;
        }

    }
}
