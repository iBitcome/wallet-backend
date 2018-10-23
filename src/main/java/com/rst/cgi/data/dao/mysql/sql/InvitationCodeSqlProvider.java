package com.rst.cgi.data.dao.mysql.sql;

import com.rst.cgi.data.entity.InvitationCode;
import com.rst.cgi.data.entity.InvitationCodeConsumer;

/**
 * Created by hujia on 2017/8/29.
 */
public class InvitationCodeSqlProvider {
    public String insert(final InvitationCode invitationCode) {
        return new DynamicSQL() {
            {
                INSERT_INTO("invitation_code");
                BUILD_VALUES(invitationCode);
            }
        }.toString();
    }

    public String insertConsumer(final InvitationCodeConsumer invitationCodeConsumer) {
        return new DynamicSQL() {
            {
                INSERT_INTO("invitation_code_consumer");
                BUILD_VALUES(invitationCodeConsumer);
            }
        }.toString();
    }

    public String updateById(final InvitationCode invitationCode) {
        return new DynamicSQL() {
            {
                UPDATE("invitation_code");
                BUILD_SET(invitationCode);
                WHERE("id = #{id}");
            }
        }.toString();
    }

    public String updateByCode(final InvitationCode invitationCode) {
        return new DynamicSQL() {
            {
                UPDATE("invitation_code");
                BUILD_SET(invitationCode);
                WHERE("code = #{code}");
            }
        }.toString();
    }
}
