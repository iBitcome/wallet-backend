package com.rst.cgi.common.constant;

import okhttp3.MediaType;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.math.ec.FixedPointPreCompInfo;
import org.spongycastle.math.ec.FixedPointUtil;

import java.math.BigInteger;

/**
 * 项目常量定义
 * @author huangxiaolin
 * @date 2018-04-20 下午3:50
 */
public interface Constant {

    String CHARSET_UTF8 = "UTF-8";

    String CLIENT_VERSION = "client-version";

    /**控制层响应页面信息的键*/
    String MESSAGE_KEY = "msg";
    /**控制层响应页面编码的键*/
    String CODE_KEY = "code";
    /**响应成功时的编码*/
    int SUCCESS_CODE = 0;
    /**响应错误时的默认编码*/
    int ERROR_CODE = -1;
    /**session有效时间（秒）*/
    int DEFAULT_SESSION_LIVE_TIME = 60 * 60 * 24 * 3;
    /**语种选择请求头*/
    String LANGUAGE_TYPE = "LANGUAGE-TYPE";
    /**本地块落后块的数量（检查以太坊订阅）*/
    int CHECK_SUBSCRIBE_BLOCK_NUM = 5;

    //等价1美元的代币
    String USDT_TOKEN = "USDT";
    //redis代币价格的键
    String TOKEN_THRID_KEY = "token.thrid.data";
    //交易所
    String[] TOKEN_EXCHANGES = {"okex", "liqui", "binance", "hitbtc2", "gateio"};

    //丢失充值交易缓存健
    String LOST_RECHARGE_RECORD_KEY = "lost.recharge.key";

    //钱包地址最新nonce健的前缀
    String LAST_NONCE = "last.nonce.";

    //redis中区块高度key的前缀
    String BLOCK_CURRENT_HEIGHT = "BLOCK_CURRENT_HEIGHT.";

    String EMAIL_REGEX="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";

    /**============用户相关配置 begin===============**/

    //用户注册放入到redis验证码的key前缀
    String USER_REGIST_KEY_PREFIX = "user.regist.";
    //用户登录放入到redis验证码的key前缀
    String USER_LOGIN_KEY_PREFIX = "user.login.";
    //用户重置密码放入到redis验证码的key前缀
    String USER_RESET_PWD_KEY_PREFIX = "user.reset.pwd.";
    //用户绑定手机号的key前缀
    String USER_BIND_PHONE = "user.bind.phone.";
    //用户发送验证码间隔时间前缀
    String USER_VALID_CODE_TIME = "user.valid.code.time.";
    //活动预约名额
    String ACTIVITY_RESERVE_NUMBER = "activity.open.control.";
    //用户绑定邮箱的key前缀
    String USER_BIND_EMAIL_KEY_PREFIX = "user.bind.email.";
    //邀请人用户id前缀
    String INVITATION_TOP = "INVITATION_TOP";
    //用户的邮箱和短信验证码的有效分钟数
    int VALIDATECODE_VALID_TIME = 10;
    //邮箱超链接有效分钟数
    int SUPER_LINK = 30;
    //用户密码输入错误次数的redis的key
    String USER_PASSWORD_COUNT_KEY_PREFIX = "user.password.count.";
    //登录时当日内密码输入错误次数上限
    int PASSWORD_ERROR_MAX_COUNT = 5;
    //用户自动解除冻结状态的时间，单位为毫秒
    long USER_FROZEN_TIME = 24 * 60 * 60000L;
    //短信地址
    String SIM_URL = "http://123.58.255.70:8860/sendSms";

    /**============用户相关配置 end================**/

    MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");


    /**============推送的消息类型常量配置 start================**/

    //交易失败
    String TRANSACTION_FAIL = "-1";
    //交易被打包
    String TRANSACTION_PACKED = "0";
    //交易确认
    String TRANSACTION_CONFIRM = "1";
    //快讯类型
    String NEWS_FLASH = "2";
    //充值和提现类型
    String RECHARGE_WITHDRA = "3";
    //网关兑换成功
    String EXCHANGE_SUCCESS = "4";

    /**============推送的消息类型常量配置 end================**/




    /**
     * The parameters of the secp256k1 curve that Bitcoin uses.
     */
    X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    FixedPointPreCompInfo FIXED_POINT_PRE_COMP_INFO = FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
    ECDomainParameters CURVE= new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
            CURVE_PARAMS.getH());
    BigInteger HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    BigInteger LARGEST_PRIVATE_KEY = new BigInteger
            ("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    BigInteger Q = new BigInteger(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

}
