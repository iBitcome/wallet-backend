package com.rst.cgi.data.entity;

import lombok.Data;

@Data
public class InvitationCodePhone implements Entity {
    private int id;
    private String phone;
    private int codeId;
}
