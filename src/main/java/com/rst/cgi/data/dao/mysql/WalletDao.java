package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.EosAccount;
import com.rst.cgi.data.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/4/27.
 */
@Mapper
public interface WalletDao {

    /**
     *
     * @param equipmentNo
     * @param status
     */
    @Update("UPDATE wallet SET key_status = #{status},update_time=NOW() WHERE equipment_no = #{equipmentNo} ")
    void updatePKStatus(@Param("equipmentNo") String equipmentNo, @Param("status") int status);

    Set<String> queryPbkByAddress(@Param("queryList") List<String> queryList);

    List<String> queryEquByAddressList(@Param("queryList") List<String> queryList);

    List<Integer> queryWalletIdByAddressList(@Param("queryList") List<String> addressList);

    @Select("select  distinct w.owner from wallet w left join wallet_address wa on w.id = wa.wallet_id " +
            " where wa.wallet_address = #{fromAddress} and w.key_status = 1")
    List<Integer> queryByAddress(@Param("fromAddress") String fromAddress);


    @Select("select  * from wallet where owner = #{userId} and  key_status = 1")
    List<Wallet> queryByOwnerId(@Param("userId") Integer userId);
    @Select("select * from eos_account where wallet_address = #{address} and eos_account = #{account}")
    EosAccount queryEosAccount(@Param("address") String walletAddress,@Param("account") String account);

    List<Integer> queryByWallets(@Param("queryList") List<String> wallets);

    @Select("select distinct w.* from wallet w left join wallet_address wa on w.id = wa.wallet_id " +
            " where wa.wallet_address=#{address} and w.type != 2")
    List<Wallet> queryOwnerCreateByAddress(@Param("address") String address);
}
