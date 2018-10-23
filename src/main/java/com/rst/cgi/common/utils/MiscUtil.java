package com.rst.cgi.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author hujia
 */
public class MiscUtil {

    public static void fill(Object receiver, Object data) {
        Field[] fields = data.getClass().getDeclaredFields();
        Field.setAccessible(fields, true);
        try {
            for (Field field : fields) {
                if (Modifier.isFinal(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String name = field.getName();
                Object value = field.get(data);
                if (value != null) {
                    field.set(receiver, field.get(data));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
