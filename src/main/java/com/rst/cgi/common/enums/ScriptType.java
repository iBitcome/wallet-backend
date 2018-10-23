package com.rst.cgi.common.enums;

public enum ScriptType {
    P2PKH ("P2PKH"),
    P2SH ("P2SH");

    private  final String scriptType;

    ScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public  String getScriptType(){
        return this.scriptType;
    }
}
