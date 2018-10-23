package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.dto.response.EOSAccountDto;
import com.rst.cgi.data.entity.EosAccount;
import com.rst.cgi.data.entity.WalletAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Administrator
 * @date 2018/4/27
 */
@Mapper
public interface WalletAddressDao {

    /**
     * 修改钱包地址的状态
     *
     * @param equipmentNo
     * @param status
     */
    @Update("UPDATE wallet_address SET status_code = #{status},update_time=NOW() WHERE equipment_no = #{equipmentNo} ")
    void updateAddressStatus(@Param("equipmentNo") String equipmentNo, @Param("status") int status);

    List<WalletAddress> queryBy(@Param("address_list") List<String> addressList,
                                @Param("sync_status") int sync_status);

    List<WalletAddress> queryByAddressList(@Param("address_list") List<String> addressList);

    @Select("select * from wallet_address where wallet_id=#{walletId} and status_code = 1")
    List<WalletAddress> queryByWalletId(@Param("walletId") Integer walletId);

    @Update("update wallet_address set balance = #{balance} where wallet_address = #{address} and token = #{tokenName}")
    void updateAddressBalance(@Param("address") String addressHash,
                              @Param("balance") String balance,
                              @Param("tokenName") String tokenName);
    List<EosAccount> queryEosAccount(@Param("account") String account);
}
