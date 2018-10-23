package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.dto.SecondIdentify;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author hujia
 */
@Data
public class LoginResDTO extends Readable {
	@ApiModelProperty(value = "邮箱")
	private String email;
	@ApiModelProperty(value = "手机")
	private String phone;
	private int id;
	@ApiModelProperty(value = "后续请求的x-auth-token")
	private String session;
	@ApiModelProperty(value = "自动登录的token")
	private String autoToken;
	@ApiModelProperty(value = "钱包数据的最后修改时间")
	private Map<String, Long> walletLastModified;
	@ApiModelProperty(value = "用户账户的额外信息")
	private Map<String, Object> extra;
//	@ApiModelProperty(value = "手势密码(0 未设置 1 已设置)")
//	private Integer hand;
//	@ApiModelProperty(value = "指纹(0 未设置 1 已设置)")
//	private Integer fingerPrint;
//	@ApiModelProperty(value = "手势密码记录")
//	private String handWord;
//	@ApiModelProperty(value = "手势状态 0关闭 1开启")
//	private Integer handStatus;
//	@ApiModelProperty(value = "指纹状态 0关闭 1开启")
//	private Integer fingerStatus;
	private SecondIdentify secondIdentify;
}
