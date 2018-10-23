package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.DappInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/18 下午5:25
*/
@Mapper
public interface ActivityDao {

    @Select("select * from dapp_info where lang = #{lang} and in_market = 1 and is_delete = 0 order by sort_info, create_time asc")
    List<DappInfo> GetDapps(@Param("lang")  Integer languageType);
}
