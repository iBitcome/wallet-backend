package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.dao.mysql.sql.InvitationCodeSqlProvider;
import com.rst.cgi.data.dto.response.InvitationTopRepDTO;
import com.rst.cgi.data.entity.InvitationCode;
import com.rst.cgi.data.entity.InvitationCodeConsumer;
import com.rst.cgi.data.entity.UserEntity;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by hujia on 2017/8/29.
 */
@Mapper
public interface InvitationCodeDao {
    @InsertProvider(type = InvitationCodeSqlProvider.class, method = "insert")
    void insertCode(InvitationCode invitationCode);

    void batchInsert(List<InvitationCode> invitationCodes);

    @Select("SELECT * FROM invitation_code WHERE code = #{code}")
    InvitationCode queryCode(@Param("code") String code);

    @Select("SELECT * FROM invitation_code WHERE owner_id = #{ownerId} AND status = 1 limit 1")
    InvitationCode queryCodeByOwnerId(@Param("ownerId") Integer ownerId);

    @UpdateProvider(type = InvitationCodeSqlProvider.class, method = "updateById")
    boolean updateById(InvitationCode invitationCode);

    @UpdateProvider(type = InvitationCodeSqlProvider.class, method = "updateByCode")
    boolean updateByCode(InvitationCode invitationCode);

    @InsertProvider(type = InvitationCodeSqlProvider.class, method = "insertConsumer")
    boolean insertCodeConsumer(InvitationCodeConsumer codeConsumer);

    @Select("SELECT invitation_code_consumer.consumer_id FROM invitation_code\n" +
            "    JOIN invitation_code_consumer\n" +
            "    ON invitation_code.id = invitation_code_consumer.invitation_code_id\n" +
            "WHERE invitation_code.owner_id = #{ownerId}")
    List<Integer> queryConsumersByOwnerId(@Param("ownerId") Integer ownerId);

    @Select("select u.* from invitation_code inv " +
            "join invitation_code_consumer con on inv.id = con.invitation_code_id " +
            "join users u on u.id = con.consumer_id " +
            "where inv.owner_id = #{ownerId}")
    List<UserEntity> getCodeConsumersList(@Param("ownerId") Integer ownerId);

    @Select("select ic.*,icc.consumer_id from invitation_code ic " +
            " left join invitation_code_consumer icc on ic.id = icc.invitation_code_id " +
            " where icc.consumer_id = #{userId}")
    InvitationCode findInviterByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM invitation_code_consumer WHERE consumer_id = #{consumerId} LIMIT 1")
    InvitationCodeConsumer queryFirstByConsumer(@Param("consumerId") Integer consumerId);

    @Select("select phone from invitation_code_phone where code_id = #{codeId}")
    List<String> findInvitedPhonesByCodeId(@Param("codeId") Integer codeId);

    @Select("select u.id,u.login_name, count(1) invitationNum from users u " +
            "  inner join invitation_code ic on u.id = ic.owner_id " +
            "  inner join invitation_code_consumer icc on ic.id = icc.invitation_code_id " +
            "  inner join users u2 on icc.consumer_id = u2.id " +
            " where u.login_name is not null and u2.login_name is not null and u2.status = 0 " +
            " group by u.id order by invitationNum desc")
    List<InvitationTopRepDTO> getInvitationTop();
}
