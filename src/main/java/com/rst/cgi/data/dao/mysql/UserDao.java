package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.dao.mysql.sql.CommonSQLProvider;
import com.rst.cgi.data.dto.TaskData;
import com.rst.cgi.data.dto.request.ContactQuery;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.data.vo.QueneUserInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * 用户dao
 * @author huangxiaolin
 * @date 2018-05-14 下午3:33
 */
@Mapper
public interface UserDao {

    @Select("select * from users where id = #{id}")
    UserEntity findById(int id);

    @UpdateProvider(type = CommonSQLProvider.class, method = "update")
    void update(UserEntity entity);

    /**
     * 根据邮箱查询未删除的用户
     * @author huangxiaolin
     * @date 2018-05-15 11:42
     */
    @Select("select * from users where email=#{email} and status in (0, 2)")
    UserEntity findByEmail(@Param("email") String email);

    /**
     * 根据手机号码查询未删除的用户
     * @param phone
     * @return
     */
    @Select("select * from users where phone=#{phone} and status in (0, 2)")
    UserEntity findByPhone(@Param("phone") String phone);

    /**
     * 根据用户ID更新密码
     * @author hxl
     * 2018/5/23 下午5:31
     */
    @Update("update users set password=#{password}, update_time=#{updateTime} where id=#{userId}")
    void updatePassword(@Param("userId") int userId, @Param("password") String password, @Param("updateTime") Date updateTime);

    /**
     * 更新用户的冻结状态
     * @author hxl
     * 2018/5/23 下午7:09
     */
    @Update("update users set is_frozen=#{frozen}, update_time=#{updateTime} where id=#{userId}")
    void updateFrozen(@Param("userId")int userId, @Param("frozen") int frozen, @Param("updateTime") Date updateTime);

    /**
     * 根据邮箱更新用户冻结状态
     * @author hxl
     * 2018/5/30 下午6:18
     */
    @Update("update users set is_frozen=#{frozen}, update_time=#{updateTime} where email=#{email} and status in (0, 2)")
    void updateFrozenByEmail(@Param("email")String email, @Param("frozen") int frozen, @Param("updateTime") Date updateTime);

    /**
     * 查询所有的冻结用户
     * @author hxl
     * 2018/5/30 下午5:49
     */
    @Select("select email, update_time frozen_time from users where is_frozen=1 and status in (0, 2) ")
    List<QueneUserInfo> findFrozenUsers();

    /**
     * 通过邮箱或手机号查询用户
     * @param account
     * @return
     */
    @Select("select * from users where (email = #{account} or phone = #{account}) and status in (0, 2)")
    UserEntity findByPhoneOrEmail(@Param("account") String account);

    /**
     * 修改二次验证信息
     * @param hand
     * @param fingerPrint
     * @param handWord
     * @param handStatus
     * @param fingerStatus
     */

    void updateSecond(@Param("hand") Integer hand, @Param("fingerPrint") Integer fingerPrint, @Param("handWord") String handWord, @Param("handStatus") Integer handStatus, @Param("fingerStatus") Integer fingerStatus,@Param("handPath") Integer handPath,@Param("userId") Integer userId);

    List<TaskData> getCountData(@Param("time")String time);

    List<ContactQuery> getUserAddressList(@Param("userId") int userId);

    /**
     * 通过活动地址查询有效用户
     * @param address
     * @return
     */
    @Select("select * from users where active_address = #{address} and  status != 1")
    UserEntity findbyActiveAddrss(String address);

    /**
     * 通过登录名查询用户
     * @param identify
     * @return
     */
    @Select("select * from users where login_name = #{loginName}")
    UserEntity findByLoginName(@Param("loginName") String identify);

    @Update("update users set email = null where id = #{id}")
    void deleteEmail(@Param("id") Integer id);

    @Update("update users set phone = null where id = #{id}")
    void deletePhone(@Param("id") Integer id);

    List<Integer> getwallletIds(@Param("owner") int owner);

    List<InvitationCode> queryByCode(@Param("code") String code);

    @Select("select * from users where phone = #{account} and status in (0, 2)")
    List<UserEntity> queryByPhone(@Param("account") String phone);

    @Select("select * from users where email = #{account} and status in (0, 2)")
    List<UserEntity> queryByEmail(@Param("account") String phone);

    @Select("SELECT count(*) from invitation_code_consumer icc " +
            "  inner join users u on icc.consumer_id = u.id " +
            "where invitation_code_id =(SELECT id from invitation_code where owner_id = #{userId}) and u.status = 0")
    Integer queryInviPeopleByUserId(@Param("userId") int userId);

    @Select("select * from invitation_code_phone where phone =#{phone}")
    List<InvitationCodePhone> queryInviId(@Param("phone") String phone);

    @Select("select code_id from invitation_code_phone where phone =#{phone}")
    Integer queryInviIdByphone(@Param("phone") String phone);

    @Select("select * from ac_user_phone where phone =#{phone} and ptype = #{ptype}")
    List<AcUserPhone> queryAcByPhone(@Param("phone") String phone,@Param("ptype") int ptype);

    @Select("select * from invitation_code_consumer where consumer_id = #{userId}")
    InvitationCodeConsumer queryConsumerByUserId(@Param("userId") int userId);

    @Select("select owner_id from invitation_code where id = #{id}")
    Integer queryUserIdByInvicodeId(@Param("id") int inviId);

    void insertAcUser(AcUserPhone user);

    @Select("select u.* from invitation_code ic " +
            " inner join invitation_code_consumer icc on ic.id = icc.invitation_code_id " +
            " inner join users u on icc.consumer_id = u.id " +
            " where ic.owner_id = #{userId} and u.status = 0")
    List<UserEntity> queryInvitedListByUserId(@Param("userId") Integer userId);
}
