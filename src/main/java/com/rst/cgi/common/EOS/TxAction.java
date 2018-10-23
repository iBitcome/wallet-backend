package com.rst.cgi.common.EOS;

import java.util.ArrayList;
import java.util.List;

public class TxAction extends BaseVo {
    public TxAction() {

    }

    public TxAction(String actor, String account, String name, Object data) {
        this.account = account;
        this.name = name;
        this.data = data;
        this.authorization = new ArrayList<>();
        this.authorization.add(new TxActionAuth(actor, "active"));
    }

    private String account;

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<TxActionAuth> getAuthorization() {
        return authorization;
    }

    public void setAuthorization(List<TxActionAuth> authorization) {
        this.authorization = authorization;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    private String name;

    private List<TxActionAuth> authorization;

    private Object data;

    @Override
    public String toString() {
        return "TxAction{" +
                "account='" + account + '\'' +
                ", name='" + name + '\'' +
                ", authorization=" + authorization +
                ", data=" + data +
                '}';
    }
}
