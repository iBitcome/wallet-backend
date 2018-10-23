package com.rst.cgi.common.constant;

import org.springframework.util.StringUtils;

/**
 * @author hujia
 */
public class Error {
    /**
     * 在这里定义错误消息
     */
    public static final Error NO_DATA = make(1008, "暂无数据",
            "No  data");
    public static final Error SESSION_EXPIRE = make(403, "会话已过期，请重新登录",
            "Chat has expired, login in again please");
    public static final Error REQUEST_PARAM_INVALID = make(2000, "无效的请求参数",
            "Invalid request parameter");
    public static final Error EMAIL_NOT_EMPTY = make(2001, "邮箱不能为空",
            "E-mail is required to fill");
    public static final Error EMAIL_FORMAT_WRONG = make(2025, "邮箱格式填写错误",
            "Wrong email format");
    public static final Error PASSWORD_NOT_EMPTY = make(2002, "密码不能为空",
            "Password is required to fill");
    public static final Error NEW_PASSWORD_NOT_EMPTY = make(2003, "新密码不能为空",
            "New password is required to fill");
    public static final Error USER_NOT_EXIST = make(2004, "用户不存在",
            "User does not exist");
    public static final Error OLD_PASSWORD_INVALID = make(2005, "原密码输入错误",
            "incorrect Original password");
    public static final Error MAIL_SEND_ERROR = make(2006, "邮件发送失败，请稍后再试",
            "Email Sending failed. Please try again later");
    public static final Error EMAIL_EXIST = make(2007, "邮箱已被绑定，请更换邮箱后再试",
            "Email address has occupied，switch please");
    public static final Error VALIDATE_CODE_INVALID = make(2009, "验证码不正确或已失效",
            "Verification code is incorrect or expired");
    public static final Error USERNAME_OR_PASSWORD_INVALID = make(2009, "账号或密码错误，请重新输入",
            "incorrect account or password, please try again ");
    public static final Error USERNAME_NOT_EMPTY = make(2009, "用户名不能为空",
            "Username is required to fill");
    public static final Error ACCOUNT_FROZEN = make(2009, "账号已被冻结",
            "Account has been frozen");
    public static final Error LOGIN_TYPE_INVALID = make(2010, "无效的登录类型",
            "you  are reseting your password");
    public static final Error PASSWORD_MAX_ERROR = make(2011, "您的密码输入错误次数过多，请于24小时后再次登录",
            "Your password is entered incorrectly too many times, please log in again after 24 hours");
    public static final Error CONTACTS_NAME_NOT_EMPTY = make(2012, "联系人名称不能为空",
            "Contact name is required to fill");
    public static final Error CONTACTS_TOKEN_OR_ADDRESS_NOT_EMPTY = make(2013, "联系人代币或地址不能为空" ,
            "token or address of Contact  is required to fill");
    public static final Error CONTACTS_NOT_EMPTY = make(2014, "联系人不存在",
            "Contact does not exist");
    public static final Error INVITATION_CODE_NOT_EXIST = make(2015, "邀请码有误，请确认后再试",
            "Invitation code is incorrect. Please try again.");
    public static final Error PHONE_EXIST = make(2016, "手机号已被使用，请更换手机号再试",
            "The phone number has been used. Please try again.");
    public static final Error PHONE_NOT_EMPTY = make(2017, "手机号不能为空",
            "Phone number is required to fill");
    public static final Error REGISTER_TYPE_NOT_SUPPORT = make(2018, "注册方式暂不支持",
            "Registration method is not supported at present");
    public static final Error EXCHANGE_NOT_EXIST = make(3000, "暂没接入该交易所",
            "No access to the exchange yet");
    public static final Error TOKEN_PAIR_NOT_EMPTY = make(3001, "交易对不能为空",
            "Trading pairs cannot be empty");
    public static final Error SERVER_EXCEPTION = make(500, "服务器繁忙，请稍后再试",
            "Server internal error, please try again later");
    public static final Error HTTP_CONTENT_INVALID = make(-10003, "无法解析请求内容",
            "Unable to parse request content");
    public static final Error SID_NOT_PRESENT = make(-10004, "拒绝访问，缺少serviceId",
            "Access denied, missing serviceId");
    public static final Error REQUEST_EXPIRED = make(-10005, "拒绝访问，请求过期",
            "Access denied request expired");
    public static final Error PUB_HASH_INVALID = make(-10006, "无效的公钥哈希",
            "Invalid public key hash");
    public static final Error IP_INVALID = make(-10007, "该IP禁止访问",
            "The IP is forbidden to access");
    public static final Error ERR_MSG_PARAM_ERROR = make(-10008, "请求参数有误，请确认后再试",
            "Request parameter is wrong, please confirm and try again");
    public static final Error ERR_MSG_KEY_ERROR = make(-10009, "无法解析请求内容",
            "Unable to resolve request content");
    public static final Error ERR_MSG_SERVICE_ERROR = make(-10010, "服务内部错误",
            "Server internal error");
    public static final Error ERR_MSG_REQUEST_FAIL = make(-10011, "请求失败，请稍后重试",
            "Request failed, please try again later");
    public static final Error ERR_MSG_PRICE_ERROR = make(-10012, "获取代币价格失败，请稍后再试",
            "Get token price failed, please try again later");
    public static final Error ERR_MSG_RATE_ERROR = make(-10013, "获取汇率失败，请稍后再试",
            "Get the swap rate failed, please try again later");
    public static final Error ERR_MSG_GET_CNY_ERROR = make(-10014, "请求数据失败，请稍后再试",
            "Request data failed, please try again later");
    public static final Error ERR_MSG_MONEY_ERROR = make(-10015, "获取法币列表失败，请稍后再试",
            "Get legal tender list failed, please try again later");
    public static final Error ERR_MSG_GET_ASSETS_FAIL = make(-10016, "获取资产失败，请稍后再试",
             "failed to obtain assets, please try again later");

    public static final Error ERR_MSG_ADD_WALLET_FAIL = make(-10017, "添加钱包失败",
            "failed  to add a wallet");
    public static final Error TOKEN_NOT_EMPTY = make(-10018, "代币不能为空",
            "Tokens cannot be empty");
    public static final Error ERR_MSG_WALLET_SHOULD_NOT_EMPTY = make(-10019, "钱包地址不能为空",
            "Wallet address cannot be empty");
    public static final Error ERR_MSG_COIN_TYPE_ERROR = make(-10020, "币种类型有误",
            "Wrong type of tender");
    public static final Error ERR_MSG_TOKEN_NOT_SUPPORT = make(-10021, "该币种暂不支持",
            "The token is not supported");
    public static final Error REQUEST_DATA_ERROR = make(-10022, "请求数据出错",
            "Request data error");
    public static final Error ERR_MSG_WALLET_NOT_EXIST = make(-10023, "该用户不存在此钱包",
            "Wallet does not exist.");
    public static final Error ERR_MSG_WALLET_HAS_EXIST = make(-10023, "此钱包已被创建",
            "Wallet existed.");
    public static final Error ERR_MSG_WALLET_BIND_OTHER = make(-10024, "钱包已绑定到其他用户，请使用钱包所属用户解除绑定",
             "The wallet is bound to other users, please unbind the user who belongs to the wallet");
    public static final Error ERR_MSG_PHONE_CODE_TYPE_NOT_SUPPORT = make(-10025, "短信类型暂不支持",
            "SMS type not supported");
    public static final Error ERR_MSG_EMAIL_NOT_REGISTER = make(-10026, "该邮箱还未注册,请注册后再试",
            "This email has not been registered yet. Please register and try again");
    public static final Error VALIDATE_CODE_TYPE_NOT_SUPPORT = make(-10027, "不支持该类型验证码",
            "This type of verification code is not supported");
    public static final Error ACCOUNT_NOT_EXIST = make(-10028, "账号不存在，请注册后再试",
            "The account does not exist. Please sign up");
    public static final Error ERR_MSG_ACCOUNT_LOCKING = make(-10029, "您的账户还剩#arg0次登录机会，切换验证码登录",
            "#arg0 chance left to try or switch to verification code.");
    public static final Error EXCHANGE_ACCOUNT_NOT_EXIST = make(-10030, "请先绑定交易所账号信息",
            "Please binding exchange account information");
    public static final Error EXCHANGE_NOT_CONNECTED = make(-10031, "需要先连接到交易所",
            "Need to connect to the exchange first");
    public static final Error ERR_MSG_SEND_TRANSACTION_FAI = make(-10032, "发起交易失败",
            "failure transaction");
    public static final Error CONTACTS_ADDRESS_EXIST = make(2014, "联系人地址已存在,请重新添加",
            "The contact address has already existed, please add it again");
    public static final Error GET_GAS_LIMIT_FAIL = make(-10033, "获取gasLimit失败",
            "gasLimit failed");
    public static final Error WITHDRAW_MONEY_NOT_ENOUGH = make(-10034, "最小提现金额需大于0.01ETH ",
            "Amount should be more than 0.01ETH");
    public static final Error ACTIVE_ADDRESS_EMPTIY = make(-10035, "活动地址不能为空",
            "Entered nothing here");
    public static final Error ADDRESS_IS_USED_BY_OTHER = make(-10036, "该活动地址已被其他用户使用",
            "The receive address  has occupied");
    public static final Error ERR_MSG_INVITATION_SELF = make(-10037, "不能邀请您自己哦",
            "You cannot invite yourself");
    public static final Error ERR_MSG_VERSION_TOO_OLD = make(-10038, "请前往官网下载最新版本以支持此操作",
            "version is too old");
    public static final Error ERR_MSG_INVITATIONED_TWICE = make(-10039, "邀请码只能提交一次",
            "Invitation code only can  be submitted once");
    public static final Error ERR_MSG_WAILT_OT_REPLACE = make(-10040, "交易矿工费过低，请重新设置需大于前一交易以便覆盖",
            "replacement transaction underpriced");
    public static final Error ERR_MSG_EOS_TRAN = make(-10041,"账户名已被占用，请重新输入","account name has existed");
    public static final Error ERR_MSG_ADDRESS_NOT_IN = make(-10042,"请提交由iBitcome创建或导入的BCH地址",
            "Please submit BCH address created or imported in iBitcome");
    public static final Error ERR_MSG_ADDRESS_EXIST = make(-10043,"地址已存在，无需重复提交",
            "The address existed.NO need to submit it repeatedly");
    public static final Error ERR_MSG_GATEWAY_NOT_SUPPROT = make(-10044,"暂不支持：#arg0",
            "Not supported yet：#arg0");
    public static final  Error ERR_MSG_RESERVED = make(-10045,"手机号已成功预约，无需再预约。",
            "The phone number has been successfully reserved. No further reservation is required.");
    public static final  Error ERR_MSG_VALID_TIME = make(-10046,"发送频率太快了，休息下，60秒后再试试。",
            "The frequency is too fast. Take a break and try again after 60 seconds.");
    public static final  Error ERR_MSG_ALLREADY_VIP = make(-10047,"你已经是会员啦，请前往分享页面",
            "You are already a member,Please go to the sharing page.");
    public static final  Error ERR_MSG_SEND_PHONE = make(-10048,"短信发送失败，请稍后再试。",
            "Message sending failed. Please try again later.");


    private InternationalizedString origin;
    private String msg;
    private int code = -1;

    Error(int code, String chsMsg, String engMsg) {
        this.code = code;
        this.origin = new InternationalizedString();
        this.origin.put(InternationalizedString.CHS, chsMsg);
        this.origin.put(InternationalizedString.ENG, engMsg);
    }

    Error(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    Error() {}

    public Error format(String... args) {
        Error error = new Error();
        error.code = this.code;
        if (StringUtils.isEmpty(msg) && !StringUtils.isEmpty(origin)) {
            msg = origin.get();
        }

        for (int i = 0; i < args.length; i++) {
            error.msg = this.msg.replaceAll("#arg" + i, args[i]);
        }

        return error;
    }

    public String getMsg() {
        if (StringUtils.isEmpty(msg)) {
            return origin.get();
        }

        return msg;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "{code:" + code + ",msg:" + msg + "}";
    }

    public static Error make(int code, String msg) {
        return new Error(code, msg);
    }

    public static Error make(int code, String chsMsg, String engMsg) {
        return new Error(code, chsMsg, engMsg);
    }


}
