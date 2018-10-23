package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
@Mapper
public interface RechargeDao {

    List<RechargeRecord> findByTxId(@Param("txIds") List<String> txIds);

    @Update("update recharge_record set status = #{status},update_time = #{updateTime}, " +
            " trader_address = #{address}, height=#{height} where tx_id = #{txId}")
    void updateStatusByTxId(@Param("txId") String txId,
                            @Param("updateTime") Long updateTime,
                            @Param("status") int status,
                            @Param("address") String address,
                            @Param("height") Integer height);

    @Update("update recharge_record set height = #{height}, update_time = #{updateTime}, " +
            "trader_address = #{address} where tx_id = #{txId}")
    void updateHeightByTxId(@Param("txId") String txId,
                            @Param("updateTime") Long updateTime,
                            @Param("height") Integer height,
                            @Param("address") String address);

    @Select("select * from recharge_record where type in (1, 2) " +
            " and trader_address = #{address}")
    List<RechargeRecord> findCashRecordsByAddress(@Param("address") String address);


    @Select("select * from recharge_record where type = #{type} " +
            " and trader_address = #{address}")
    List<RechargeRecord> findCashRecordsByType(@Param("type") Integer type,
                                                        @Param("address") String address);
}
