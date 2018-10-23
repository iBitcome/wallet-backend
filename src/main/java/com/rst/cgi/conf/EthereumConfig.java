package com.rst.cgi.conf;


import com.rst.cgi.common.utils.Web3jClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import java.io.IOException;


/**
 * Created by mtb on 2018/4/3.
 * java使用web3j方式连接以太坊区块链(暂时未使用)
 *
 */
//@Service
public class EthereumConfig {
//    @Autowired
//    private Web3jClient web3jClient;
//
//    public static Integer ETH_BLOCK_HEIGHT = null;//当前最新区块高度
//
//    private final Logger logger = LoggerFactory.getLogger(EthereumConfig.class);


   /* @PostConstruct
    public void init(){
         Web3j web3 = web3jClient.getWeb3j();
         startSubscription(web3);
         this.getEthBlockHeight();
    }


    private Subscription subscription;

    void startSubscription(Web3j web3j) {
        //块过滤器（链上产生一个块时触发）
        subscription = web3j.blockObservable(false).subscribe(block -> {
            if (ETH_BLOCK_HEIGHT == null || ETH_BLOCK_HEIGHT < block.getBlock().getNumber().intValue()) {
                ETH_BLOCK_HEIGHT = block.getBlock().getNumber().intValue();
                logger.info("【ETH】最新区块高度:{}", ETH_BLOCK_HEIGHT);
            }
        }, e -> {
            logger.error("【ETH】订阅已被断开，正在重连--" + e.getMessage());
            subscription.unsubscribe();
            startSubscription(web3j);
        });
    }




    public void checkEthSubscribe(){
        logger.info("【ETH】订阅已被断开，正在重连");
        Web3j web3 = web3jClient.getWeb3j();
        subscription.unsubscribe();
        getEthBlockHeight();
        startSubscription(web3);
    }*/


//   @Scheduled(fixedRate = 15 * 1000)
//    public  Integer getEthBlockHeight(){
//        Web3j web3 = web3jClient.getWeb3j();
//        Integer result = 0;
//        Request<?, Web3ClientVersion> request = web3.web3ClientVersion();
//        request.setMethod("eth_blockNumber");
//        try {
//            String heightStr = request.send().getWeb3ClientVersion();
//            Integer height = Integer.valueOf(heightStr.substring(2, heightStr.length()), 16);
//            if (ETH_BLOCK_HEIGHT == null || ETH_BLOCK_HEIGHT < height) {
//                ETH_BLOCK_HEIGHT = height;
//                result = height;
//                logger.info("【ETH】最新区块高度:{}", ETH_BLOCK_HEIGHT);
//            }
//        } catch (IOException e) {
//            logger.info("获取以太坊最新块高度失败");
//            e.printStackTrace();
//        }
//        return result;
//    }

}
