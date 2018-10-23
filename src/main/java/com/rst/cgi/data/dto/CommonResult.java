package com.rst.cgi.data.dto;

import com.rst.cgi.common.constant.Error;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author hujia
 * @date 2017/2/28
 */
//标准的json数据传输对象
@Data
@ApiModel("标准的json数据传输对象")
public class CommonResult<T> extends Readable implements Serializable {
	@ApiModelProperty("请求是否成功")
    private boolean success = true;
	@ApiModelProperty("状态码")
    private int code = 1;
	@ApiModelProperty("描述信息")
    private String msg = "";
	@ApiModelProperty("返回的请求数据")
	private T data;

    public CommonResult() {

    }

    public CommonResult(final String msg) {
        this.success = false;
        this.code = -1;
        this.msg = msg;
    }

    public CommonResult(boolean success, int code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }
    
    public CommonResult(boolean success, int code, String msg, T data) {
		super();
		this.success = success;
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

    public void setError(int code, String msg) {
        this.success = false;
        this.code = code;
        this.msg = msg;
    }

    public void setOK() {
        this.success = true;
        this.code = 1;
        this.msg = "";
    }

    public static  <T> CommonResult<T> make(Error error) {
        CommonResult<T> result = new CommonResult<>();
        result.code = error.getCode();
        result.msg = error.getMsg();
        return result;
    }

    public static  <T> CommonResult<T> make(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = 1;
        result.msg = "";
        result.data = data;
        return result;
    }

    public static  <T> CommonResult<T> make(int code, String msg, T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }
}
