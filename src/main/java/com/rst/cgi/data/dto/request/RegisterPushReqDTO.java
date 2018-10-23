package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

public class RegisterPushReqDTO extends Readable {
    @ApiModelProperty(value = "iOS APNS需要的deviceToken", required = true)
    private String deviceToken;
    @ApiModelProperty(value = "设备编号", required = true)
    private String equipmentNo;

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }
}
