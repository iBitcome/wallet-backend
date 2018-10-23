package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.ExRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/11 下午12:41
*/
@Mapper
public interface ExRecordDao {

    @Select("select * from ex_record " +
            " where wallet_hash = #{walletHash} and (from_name = #{tokenName} or to_name = #{tokenName}) order by create_time desc")
    List<ExRecord> queryByWalletHash(@Param("walletHash") String walletHash,
                                     @Param("tokenName") String tokenName);

    @Select("select * from ex_record where from_hash = #{fromHash}")
    List<ExRecord> queryByFromHash(@Param("fromHash") String fromHash);

    void updateRecordStatusAndTxFee(@Param("success") Integer success,
                                    @Param("fromFee") String fromFee,
                                    @Param("ids") List<Integer> exRecordIds);
}
