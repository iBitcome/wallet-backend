package com.rst.cgi.data.entity;

import java.util.Date;

public class Equipment implements Entity{
    private String equipmentNo;

    private Date creatTime;

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Equipment) {
            Equipment other = (Equipment) obj;
            return equipmentNo.equals(other.equipmentNo);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.equipmentNo.hashCode();
    }
}