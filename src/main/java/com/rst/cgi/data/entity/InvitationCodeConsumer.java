package com.rst.cgi.data.entity;

/**
 * Created by hujia on 2017/8/29.
 */
public class InvitationCodeConsumer implements Entity {
    private Integer id;
    private Integer invitationCodeId;
    private Integer consumerId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getInvitationCodeId() {
        return invitationCodeId;
    }

    public void setInvitationCodeId(Integer invitationCodeId) {
        this.invitationCodeId = invitationCodeId;
    }

    public Integer getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Integer consumerId) {
        this.consumerId = consumerId;
    }
}
