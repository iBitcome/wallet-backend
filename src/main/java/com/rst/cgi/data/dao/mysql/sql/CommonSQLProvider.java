package com.rst.cgi.data.dao.mysql.sql;


import com.rst.cgi.common.utils.StringUtil;
import com.rst.cgi.data.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by hujia on 2017/3/13.
 */
public class CommonSQLProvider {
    public <T extends Entity> String queryBy(final T item) {
        return new DynamicSQL() {
            {
                SELECT("*");
                FROM(item.table());
                WHERE_OBJECT(item);
            }
        }.toString();
    }

    public <T extends Entity> String queryFirstBy(final T item) {
        return new DynamicSQL() {
            {
                SELECT("*");
                FROM(item.table());
                WHERE_OBJECT(item);
            }
        }.toString() + " limit 1";
    }

    public <T extends Entity> String queryNBy(final T item, int limit) {
        return new DynamicSQL() {
            {
                SELECT("*");
                FROM(item.table());
                WHERE_OBJECT(item);
            }
        }.toString() + " limit " + limit;
    }

    public String queryByIds(List<Integer> ids, String table) {
        return new DynamicSQL() {
            {
                SELECT("*");
                FROM(table);
                WHERE_LIST(ids, "id");
            }
        }.toString();
    }

    public <T extends Entity> String insert(final T item) {
        return new DynamicSQL() {
            {
                INSERT_INTO(item.table());
                BUILD_VALUES(item);
            }
        }.toString();
    }

    public <T extends Entity> String update(final T item) {
        return new DynamicSQL() {
            {
                UPDATE(item.table());
                BUILD_SET(item);
                WHERE("id = #{id}");
            }
        }.toString();
    }


    /**
     * mysql 批量保存 sql 语句拼接
     * @author huangxiaolin
     * @date 2018-05-17 18:29
     */
    public <T extends Entity> String batchInsert(List<T> list, Class<T> entityClass) {
        int size = (list == null) ? 0 : list.size();
        if (size <= 0) {
            throw new IllegalArgumentException("List is empty");
        }
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        //Class<?> entityClass = list.get(0).getClass();
        Field[] objFields = entityClass.getDeclaredFields();
        for (int i = 0; i < size; i++) {
            valueBuilder.append(", (");
            StringBuilder eachValueBuilder = new StringBuilder();//保存值参数
            for (Field field : objFields) {
                //忽略被注解Ignore字段
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (i == 0) {
                    columnBuilder.append(",").append(StringUtil.camelToUnderline(field.getName()));
                }
                eachValueBuilder.append(",")
                        .append("#{param1[").append(i).append("].")
                        .append(field.getName())
                        .append("}");
            }
            valueBuilder.append(eachValueBuilder.substring(1));
            valueBuilder.append(")");
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(list.get(0).table())
            .append(" (")
            .append(columnBuilder.substring(1))
            .append(") VALUES ")
            .append(valueBuilder.substring(1));
        return sql.toString();
    }

}
