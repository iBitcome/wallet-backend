package com.rst.cgi.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Hint;
import com.rst.cgi.common.utils.IpUtil;
import com.rst.cgi.common.utils.RandomUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.UserDao;
import com.rst.cgi.data.dao.mysql.WalletAddressDao;
import com.rst.cgi.data.dao.mysql.WalletDao;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.entity.Rate;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.data.entity.Wallet;
import com.rst.cgi.data.entity.WalletAddress;
import com.rst.cgi.service.BlockChainService;
import com.rst.cgi.service.InvitationCodeService;
import com.rst.cgi.service.ThirdService;
import com.rst.cgi.service.UserService;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.thrift.export.ThriftClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jdk.nashorn.internal.runtime.RewriteException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import service.AddressClient;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * 仅测试接口调用
 * @author huangxiaolin
 * @date 2018-05-07 下午4:15
 */
@Api(tags = "仅供测试接口调用")
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ThriftClient<PushService.Iface> pushServerClient;
    @Autowired
    private ThirdService thirdService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private UserService userService;
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private WalletAddressDao walletAddressDao;
    @Autowired
    private Hint hint;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Value("${ac.now.type}")
    private String type;
    @Autowired
    private BlockChainService blockChainService;

    /**
     * 测试推送
     * @author huangxiaolin
     * @date 2018-05-07 16:17
     */
    @GetMapping("/push")
    @ApiOperation("配合APP端测试推送消息")
    public CommonResult<List<Rate>> testPush(@RequestParam String id, @RequestParam(required = false) String message) {
        String pushMsg = StringUtils.isEmpty(message) ? "测试推送信息" : message;
        try {
            pushServerClient.get(PushService.Iface.class).
                    push("", id, pushMsg, "1",3);
            return new CommonResult<>();
        } catch (TException e) {
            e.printStackTrace();
        }
        return new CommonResult<>("pushServer连接失败");
    }

    @ApiOperation("测试代币价格")
    @GetMapping("/token")
    public double testTokenPrice(@RequestParam(required = false) String token) {
        double price = thirdService.getUSDByToken(StringUtils.isEmpty(token) ? "BTC" : token);
        return price;
    }


    @Value("${net.work.type:test_reg}")
    private String networkType;
    @ApiOperation("将指定钱包地址转换成公钥hash字符串")
    @GetMapping("/turnAddressToHash")
    public CommonResult<String> turnAddressToHash (@RequestParam(value = "coinType") Integer coinType,
                                                   @RequestParam(value = "钱包地址") String address,
                                                   @RequestParam(value = "链类型（main/test/test_reg)默认值为当前系统所连接的链",
                                                           required = false) String netType) {
        CommonResult<String> rst = new CommonResult<>();
        try {
            if (StringUtils.isBlank(netType)) {
                netType = networkType;
            }
            rst.setData(AddressClient.addressToHash(coinType, address, netType));
        }catch (Exception e) {
            e.printStackTrace();
            rst.setError(-1,"转换失败");
        }
        return rst;
    }


    @ApiOperation("删除账号")
    @GetMapping("/deleteAccount")
    public CommonResult<String> deleteAccount (@RequestParam(value = "账号（手机/邮箱)") String account) {
        CommonResult<String> rst = new CommonResult<>();
        UserEntity user = userDao.findByPhoneOrEmail(account);
        if (Objects.isNull(user)) {
            rst.setError(-1, "账号不存在");
            return rst;
        }
        user.setStatus(1);
        user.setUpdateTime(new Date());
        commonDao.update(user);
        return rst;
    }

    @ApiOperation("主动解冻用户")
    @GetMapping("/unfrozenAccount")
    public CommonResult<String> unfrozenAccount (@RequestParam(value = "账号（手机/邮箱)") String accout) {
        CommonResult<String> rst = new CommonResult<>();
        UserEntity user = userDao.findByPhoneOrEmail(accout);
        if (Objects.isNull(user)) {
            rst.setError(-1, "账号不存在");
            return rst;
        } else if(user.getIsFrozen() == 0) {
            rst.setError(-1, "账号未冻结");
            return rst;
        }
        user.setIsFrozen(0);
        user.setUpdateTime(new Date());
        commonDao.update(user);
        return rst;
    }

    @ApiOperation("临时测试")
    @GetMapping("/test")
    public void test() {
        String[] array = type.split(",");
        for(int i=0;i<array.length;i++){
            redisTemplate.opsForValue().set(Constant.ACTIVITY_RESERVE_NUMBER+array[i],"1");
        }


    }
    @ApiOperation("临时测试")
    @GetMapping("/test2")
    public CommonResult test2(@RequestParam String id) {

//        Long s = redisTemplate.opsForValue().increment(Constant.ACTIVITY_RESERVE_NUMBER, -1);
//        if(s==0){
//            System.out.println("等于0了");
//        }
//        for(int x = 0; x < 10 ; x++){
//            int i = RandomUtil.genRandomNum(2);
//            System.out.println(i+"------------------");
//            String [] userIdArray = {i+""};
//            redisTemplate.opsForSet().add(Constant.INVI_USER_ID,userIdArray);
//            Set<ZSetOperations.TypedTuple<String>> test = redisTemplate.opsForZSet().rangeByScoreWithScores("test", 0, -1);
//            System.out.println(test);
//        }
//        Set<ZSetOperations.TypedTuple<String>> test = redisTemplate.opsForZSet().rangeByScoreWithScores("test", 0, -1);
//        Double a = 0d;
//        Double b = -1d;
////        Set<String> test = redisTemplate.opsForZSet().rangeByScore("fengpan", 0, 10000);
//        Set<ZSetOperations.TypedTuple<String>> test = redisTemplate.opsForZSet().rangeWithScores("fengpan", 0, -1);
//        System.out.println(test);
//        Integer i = 20;
//        redisTemplate.opsForZSet().incrementScore("laofeng",i.toString(),1);
//        return new CommonResult();
        return new CommonResult();
    }


    @Data
    private static class addressDTO extends Readable {
        @ApiModelProperty(value = "钱包地址",required = true)
        private String address;
        @ApiModelProperty(value = "地址所属币种(BTC:0,BCH:145,以太坊中所有代币：60)",required = true)
        private Integer coinType;
    }

    @Autowired
    private InvitationCodeService invitationCodeService;

    @ApiOperation("临时测试")
    @GetMapping("/generateInvitationCode/{count}")
    public CommonResult<List<String>> generateInvitationCode(
            @ApiParam("生成的邀请码数量") @PathVariable("count") int count) {
        return CommonResult.make(invitationCodeService.productCode(null, count));
    }


    @Data
    private  class walletInfo extends Readable {
        @ApiModelProperty(value = "钱包主公钥")
        private String walletPubkey;
        @ApiModelProperty(value = "钱包的创建时IP")
        private String createIp;
        @ApiModelProperty(value = "钱包的创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
        private Date createTime;
        @ApiModelProperty(value = "钱包地址")
        private List<String> addressList = new ArrayList<>();
    }

    @ApiOperation("通过账号查询钱包信息")
    @GetMapping("/getAccountWallet")
    public CommonResult<List<walletInfo>> getAccountWallet(
            @ApiParam("用户账号") @RequestParam("account") String account){
        List<walletInfo> walletInfoList = new ArrayList<>();
        UserEntity user = userDao.findByEmail(account);
        if (Objects.isNull(user)) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        List<Wallet> walletList = walletDao.queryByOwnerId(user.getId());
        walletList.forEach(wallet -> {
           List<WalletAddress> walletAddressList = walletAddressDao.queryByWalletId(wallet.getId());
            walletInfo walletInfo = new walletInfo();
            walletInfo.setCreateIp(wallet.getCreateIp());
            walletInfo.setWalletPubkey(wallet.getPublicKey());
            walletInfo.setCreateTime(wallet.getCreateTime());
            walletAddressList.forEach(walletAddress -> {
                walletInfo.getAddressList().add(walletAddress.getWalletAddress());
            });
            walletInfoList.add(walletInfo);
        });


        return CommonResult.make(walletInfoList);

    }
}
