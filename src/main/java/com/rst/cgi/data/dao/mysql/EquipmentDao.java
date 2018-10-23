package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
@Mapper
public interface EquipmentDao {
    @Select("SELECT e.* FROM equipment e " +
            " LEFT JOIN wallet_address w ON e.equipment_no = w.equipment_no " +
            " WHERE w.wallet_address = #{walletAddress} and w.status_code = 1")
    List<Equipment> queryByWalletAddress( @Param("walletAddress") String walletAddress);

    @Select("SELECT e.* FROM equipment e " +
            " LEFT JOIN wallet w ON e.equipment_no = w.equipment_no " +
            " LEFT JOIN wallet_address wd ON w.id = wd.wallet_id " +
            " WHERE wd.wallet_address = #{walletAddress} AND w.key_status = 1")
    List<Equipment> queryHdByWalletAddress( @Param("walletAddress") String walletAddress);

    List<Equipment> queryByWalletAddressList(@Param("address") List<String> walletAddressList);

    @Select("SELECT DISTINCT e.* FROM equipment e " +
            "JOIN wallet w ON e.equipment_no = w.equipment_no " +
            "WHERE w.key_status = 1 AND w.public_key = #{puk}")
    List<Equipment> queryByPuk(@Param("puk") String puk);
}
