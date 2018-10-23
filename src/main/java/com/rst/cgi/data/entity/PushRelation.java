package com.rst.cgi.data.entity;

import java.util.List;

public class PushRelation {
    private String address;
    private List<Equipment> equipments;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }
}
