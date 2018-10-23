package com.rst.cgi.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by mtb on 2018/4/4.
 * 文本模板信息
 */
@Service
public class Hint {
   //交易打包进区块及未打包到区块友情推送提示
   public static final Hint TAT_PACKED = make("您的#TOKEN交易已打包进区块，请等待。#TIME",
           "#TOKEN transaction has been packaged,waiting please.#TIME");
   //交易确认友情推送提示
    public static final Hint TAT_CONFIRMED = make("恭喜用户大人，您有一笔交易已完成。 #TIME",
            "Congratulations, you have a transaction completed,  #TIME");
    //交易失败推送提示
    public static final Hint TAT_Fail = make("用户大人，您有一笔交易失败，请重新处理。#TIME",
            "You have a transaction fails, please re-processing, #TIME");
    //等待打包进区块超时，60分钟 (1.2.0版本不实现)
    public static final Hint TAT_TIME_OUT = make("用户大人，您有一笔交易失效，请重新处理。 #TIME",
            "You have a transaction fails, please re-processing, #TIME");
    //网关兑换成功友情推送提示
    public static final Hint EXCHANGE_SUCCESS = make("您有一笔#SYMBOL已兑换成功。#TIME",
            "#SYMBOL transaction succeed. #TIME");

    /**============邮箱验证码相关的文本配置 start================**/

    //发件人的昵称
    public static final Hint NICK_NAME = make("iBitcome【iBitcome】", "iBitcome wallet");
    public static final Hint REGISTER_HELLOWORD_WORD = make("您好，您正在注册iBitcome【iBitcome】",
            "You are signing up iBitcome");
    public static final Hint LOGIN_HELLOWORD_WORD = make("您好，您正在登录iBitcome【iBitcome】",
            "You are logging iBitcome");
    public static final Hint CHANGE_PWD_HELLOWORD_WORD = make("您好，您正在进行修改密码操作",
            "You are setting a password");
    public static final Hint BINGD_EMAIL_HELLOWORD_WORD = make("亲爱的iBitcomer，欢迎进行身份升级",
            "Dear iBitcomer, Welcome to upgrade your identity");
    public static final Hint MAIL_SUBJECT = make("【iBitcome】安全验证",
            "【iBitcome】 security verification ");
    public static final Hint CODE_VERIFY_NAME = make("验证码",
            "Verification code");
    public static final Hint ACTIVE_WORD = make("#ACTIVE_TIME分钟内有效。请勿向任何人泄露您的验证码。",
            "This code will expire #ACTIVE_TIME minutes after this email was sent.");
    public static final Hint ACTIVE_EMAIL = make("请在30分钟内完成激活","Please complete the activation within 30 minutes");
    public static final Hint TEAM_NAME = make("iBitcome【iBitcome】团队",
            "The iBitcome Team");
    public static final Hint VALID_EMAIL = make("用户大人,请点击如下激活链接 :","Please click the following activation link");

    /**============邮箱验证码相关的文本配置 end================**/




    /**============手机短信相关的文本配置 start================**/

//    public static final Hint PHONE_VERIFY_CODE = make("您的手机验证码为#CODE ,#ACTIVE_TIME分钟内有效，请不要告诉任何人。",
//            "Your code is #CODE,it will expire ,#ACTIVE_TIME minutes.");
    public static final Hint PHONE_VERIFY_CODE = make("您的手机验证码为#CODE ,#ACTIVE_TIME分钟内有效，请不要告诉任何人。",
            "iBitcome Verification Code：#CODE  This code expires in #ACTIVE_TIME minutes");

    /**============手机短信相关的文本配置 end================**/



    private InternationalizedString origin;
    private String msg;

    public Hint() {
    }

    public Hint(String msg) {
        this.msg = msg;
    }

    Hint(String chsMsg, String engMsg){
        origin = new InternationalizedString();
        origin.put(InternationalizedString.CHS, chsMsg);
        origin.put(InternationalizedString.ENG, engMsg);
    }

    @Value("${push.server.message.type}")
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String pushMsg(int type){
        Map<Integer, String> map = origin.typeToMsg;
        return map.get(type);

    }

    public String getMsg() {
        if (StringUtils.isEmpty(msg)) {
            return origin.get();
        }

        return msg;
    }


    public static Hint make(String chsMsg, String engMsg){
        return new Hint(chsMsg, engMsg);
    }
}