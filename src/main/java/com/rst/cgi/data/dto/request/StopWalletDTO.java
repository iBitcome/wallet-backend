package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb on 2018/4/27.
 */
public class StopWalletDTO extends Readable{
    @ApiModelProperty(value = "设备标识", required = true)
    private String equipmentNo;

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }
}
