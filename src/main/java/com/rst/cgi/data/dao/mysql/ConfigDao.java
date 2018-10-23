package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.AppVersionRecord;
import com.rst.cgi.data.entity.OperationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConfigDao {
    @Select("select * from app_version_record where is_delete = 0 and channel = #{channel}")
    List<AppVersionRecord> getVersionRecordByClient(@Param("channel") String channel);

    @Select("select * from operation_message where message_type = #{messageType} and is_delete = 0 and " +
            " lang = #{lang} and NOW() between valid_start_time and valid_end_time")
    List<OperationMessage> getOperationMsgByType(@Param("messageType") Integer messageType,
                                                 @Param("lang") Integer lang);

    @Select("select * from operation_message where message_type = #{messageType} and is_delete = 0 " +
            " and lang = #{lang} and NOW() between valid_start_time and valid_end_time limit #{num}")
    List<OperationMessage> getOperationMsgByTypeAndNum(@Param("messageType") Integer messageType,
                                                       @Param("lang") Integer lang,
                                                       @Param("num") Integer num);

    @Select("select * from operation_message where id = #{id} and is_delete = 0 ")
    OperationMessage getOperationMsgById(@Param("id") Integer id);
}
