package com.rst.cgi.common.EOS;

import lombok.Data;

@Data
public class TxActionAuth extends BaseVo {

    public TxActionAuth() {

    }

    public TxActionAuth(String actor, String permission) {
        this.actor = actor;
        this.permission = permission;
    }

    private String actor;

    private String permission;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "TxActionAuth{" +
                "actor='" + actor + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}

