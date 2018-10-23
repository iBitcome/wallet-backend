package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.common.utils.StringUtil;
import com.rst.cgi.data.dao.mysql.sql.CommonSQLProvider;
import com.rst.cgi.data.entity.Entity;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertySetStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.annotations.*;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by hujia on 2017/9/4.
 */
@Mapper
public interface CommonDao {
    /**
     * 插入一行记录：只处理entity里面值不为null的属性
     * @param entity 待处理的记录
     */
    @InsertProvider(type = CommonSQLProvider.class, method = "insert")
    @Options(useGeneratedKeys = true)
    <T extends Entity> void insert(T entity);

    /**
     * mysql批量保存接口
     * @author huangxiaolin
     * @date 2018-05-17 18:00
     */
    @InsertProvider(type = CommonSQLProvider.class, method = "batchInsert")
    <T extends Entity> void batchInsert(List<T> list, Class<T> entityClass);

    /**
     * 更新指定记录:entity的id必须设置成要更新的记录，只更新entity里面值不为null的属性
     * @param entity 待更新的属性值
     */
    @UpdateProvider(type = CommonSQLProvider.class, method = "update")
    <T extends Entity> void update(T entity);

    /**
     * 查询记录:entity里面值不为null的属性与查询出来的记录属性一致
     * @param entity 记录条件值
     * @return 满足条件的第一条记录
     */
    @SuppressWarnings("unchecked")
    default <T extends Entity>  T queryFirstBy(T entity) {
//        List<Map<String, Object>> resultList = _queryBy(entity);
        List<Map<String, Object>> resultList = _queryBy(entity);
        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }
        return getBean(resultList.get(0),entity.getClass());

    }

    /**
     * 查询记录:entity里面值不为null的属性与查询出来的记录属性一致
     * @param entity 记录条件值
     * @return 满足条件的所有记录
     */
    @SuppressWarnings("unchecked")
    default <T extends Entity>  List<T> queryBy(T entity) {
        List<Map<String, Object>> results = _queryBy(entity);
        List<T> entityRst = new ArrayList<T>();
        if (results != null && results.size() > 0) {
            results.forEach(item -> entityRst.add(getBean(item, entity.getClass())));
//            for (Map<String, Object> map : results){
//                entityRst.add(getBean(map, entity.getClass()));
//            }
            return entityRst;
        }

        return new ArrayList<>(0);
    }

    /**
     * 查询记录:entity里面值不为null的属性与查询出来的记录属性一致
     * @param entity 记录条件值
     * @return 满足条件的所有记录
     */
    @SuppressWarnings("unchecked")
    default <T extends Entity>  List<T> queryNBy(T entity, int limit) {
        List<Map<String, Object>> results = _queryNBy(entity, limit);
        List<T> entityRst = new ArrayList<T>();
        if (results != null && results.size() > 0) {
            results.forEach(item -> entityRst.add(getBean(item, entity.getClass())));
            return entityRst;
        }

        return null;
    }

    /**
     * 查询记录:指定id的记录
     * @param id 指定记录的id
     * @param entityClass 记录对应的java实体类
     * @return 满足条件的所有记录
     */
    default <T extends Entity>  T queryById(Integer id, Class<T> entityClass) {
        try {
            T entity = entityClass.newInstance();
            Map<String, Object> result = _queryById(id, entity.table());
            if (result != null) {
                return getBean(result, entityClass);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询记录:指定id列表的记录
     * @param ids 指定的id列表
     * @param entityClass 记录对应的java实体类
     * @return 满足条件的所有记录
     */
    default <T extends Entity>  List<T> queryByIds(List<Integer> ids, Class<T> entityClass) {
        try {
            T entity = entityClass.newInstance();
            List<JSONObject> results = _queryByIds(ids, entity.table());

            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.setRootClass(entityClass);
            jsonConfig.setPropertySetStrategy(new CommonPropertySetStrategy());

            return results.stream()
                    .map(item -> (T) JSONObject.toBean(item, jsonConfig))
                    .collect(Collectors.toList());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 以下API用于内部使用，不建议直接调用
     */

    @SelectProvider(type = CommonSQLProvider.class, method = "queryBy")
    <T extends Entity> List<Map<String, Object>> _queryBy(T entity);

    @SelectProvider(type = CommonSQLProvider.class, method = "queryNBy")
    <T extends Entity> List<Map<String, Object>> _queryNBy(T entity, int limit);

    @SelectProvider(type = CommonSQLProvider.class, method = "queryByIds")
    List<JSONObject> _queryByIds(List<Integer> ids, String table);

    @Select("SELECT * FROM ${table} WHERE id = #{id}")
    Map<String, Object> _queryById(@Param("id") Integer id, @Param("table") String table);

    final class CommonPropertySetStrategy extends PropertySetStrategy {

        public void setProperty(Object bean, String key, Object value) throws JSONException {
            this.setProperty(bean, key, value, new JsonConfig());
        }

        public void setProperty(Object bean, String key, Object value, JsonConfig jsonConfig) throws JSONException {
            if(bean instanceof Map) {
                ((Map)bean).put(key, value);
            } else if(!jsonConfig.isIgnorePublicFields()) {
                try {
                    Field e = bean.getClass().getField(key);
                    if(e != null) {
                        e.set(bean, value);
                    }
                } catch (Exception var6) {
                    this._setProperty(bean, key, value);
                }
            } else {
                this._setProperty(bean, key, value);
            }

        }

        private void _setProperty(Object bean, String key, Object value) {
            try {
                PropertyUtils.setSimpleProperty(bean, key, value);
            } catch (NoSuchMethodException e) {
            } catch (Exception var5) {
                throw new JSONException(var5);
            }
        }
    }

    /**
     * 转化map的键为驼峰，并返回一个对应的实体
     */
    default <T extends Entity> T getBean (Map<String, Object> map, Class entityClass) {
        Map<String, Object> rstEntity = new HashedMap();
        map.forEach((k, v) -> rstEntity.put(StringUtil.underlineToCamel(k), v));
        try {
            T obj = (T)entityClass.newInstance();
            BeanUtils.populate(obj, rstEntity);
            return obj;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
