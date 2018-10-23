package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.ExTxInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GatewayDao {
    @Select("select * from dgateway_tx where from_tx_hash = #{fromHash}")
    ExTxInfo queryByFromHash(@Param("fromHash") String fromHash);
}
