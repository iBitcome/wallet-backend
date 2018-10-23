package com.rst.cgi.data.dao.mysql.sql;

import com.rst.cgi.common.utils.StringUtil;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by hujia on 2017/3/13.
 */
public class DynamicSQL extends SQL {

    protected void BUILD_VALUES(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Field.setAccessible(fields, true);
        try {
            for (Field field : fields) {
                String name = field.getName();
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                if (field.get(obj) != null) {
                    VALUES(StringUtil.camelToUnderline(name), fieldValue(field));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void BUILD_SET(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Field.setAccessible(fields, true);
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                if (field.get(obj) != null) {
                    SET(StringUtil.camelToUnderline(field.getName())
                            + " = " + fieldValue(field));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void WHERE_OBJECT(Object item) {
        if (item != null) {
            Field[] fields = item.getClass().getDeclaredFields();
            Field.setAccessible(fields, true);

            try {
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())
                            || Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }

                    if (field.get(item) != null) {
                        WHERE(StringUtil.camelToUnderline(field.getName())
                                + " = #{" + field.getName() + "}");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected void WHERE_LIST(List items, String keyName) {
        StringBuilder whereString =
                new StringBuilder();
        if (items != null && items.size() > 0) {
            whereString.append(keyName + " in (" + items.get(0));
            for (int i = 1; i < items.size(); i++) {
                whereString.append("," + items.get(i));
            }
            whereString.append(")");
            WHERE(whereString.toString());
        }
    }

    private String fieldValue(Field field) {
        String value = "#{" + field.getName() + "}";

        if (field.isAnnotationPresent(Encodes.class)
                || field.isAnnotationPresent(Encode.class)) {
            Encode[] encodeMethods = field.getAnnotationsByType(Encode.class);

            for (int i = encodeMethods.length - 1; i >= 0; i--) {
                value = encodeMethods[i].value() + "(" + value + ")";
            }
        }

        return value;
    }
}
