package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.UserContacts;
import com.rst.cgi.data.entity.UserContactsAddress;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户联系人
 * @author hxl
 * @date 2018/5/29 下午3:40
 */
@Mapper
public interface UserContactsDao {

    /**
     * 删除某个用户的联系人
     * @author hxl
     * 2018/5/29 下午3:43
     */
    @Delete("delete from user_contacts where id=#{contactsId} and user_id=#{userId}")
    int delete(@Param("userId") int userId, @Param("contactsId") int contactsId);

    /**
     * 通过联系人删除地址
     * @author hxl
     * 2018/5/29 下午3:45
     */
    @Delete("delete from user_contacts_address where contacts_id=#{contactsId}")
    void deleteContactsAddress(int contactsId);

    @Delete("delete from user_contacts_address where id=#{addressId}")
    void deleteContactsAddressById(@Param("addressId") int addressId);



    /**
     * 查询联系人地址
     * @author hxl
     * 2018/5/29 下午6:17
     */
    List<UserContactsAddress> findContactsAddress(@Param("contactsList") List<UserContacts> contactsList);

    /**
     * 查询某个联系人的地址列表
     * @author hxl
     * 2018/5/29 下午6:29
     */
    UserContacts findContacts(@Param("contactsId") int contactsId);

    @Select("select * from user_contacts where id=#{contactsId}")
    UserContacts findById(@Param("contactsId") int contactsId);



    /**
     * 根据用户ID查询联系人信息，通过参数isContainAddress控制是否查询联系人地址信息
     * @author hxl
     * 2018/6/4 下午1:37
     * @param userId 用户ID
     * @param isContainAddress 是否查询联系人的地址，true代表查询地址，false代表不查询地址
     */
    List<UserContacts> findContactsByUserId(@Param("userId") int userId, @Param("isContainAddress") boolean isContainAddress);

    UserContacts findOneContactsByUserId(@Param("contacts_id") int userId, @Param("isContainAddress") boolean isContainAddress);

}
