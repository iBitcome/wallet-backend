package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.WalletAssistant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by mtb on 2018/4/27.
 */
@Mapper
public interface WalletAssistantDao {

    /**
     *
     */
    @Select("select wamt.type_cn, wamt.type_en, wamt.sort_info type_sort, wam.question_cn, wam.question_en," +
            " wam.sort_info msg_sort, wam.id question_id, wam.key_word from wallet_assistant_message_type wamt " +
            " left join wallet_assistant_message wam  on wam.type_id = wamt.id " +
            " where wamt.is_delete = 0 and wam.is_delete = 0")
    List<WalletAssistant> findAssistantMsg();
}
