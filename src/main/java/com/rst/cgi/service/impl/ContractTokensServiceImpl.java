package com.rst.cgi.service.impl;

import com.google.gson.Gson;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.ContractTokenBMessage;
import com.rst.cgi.data.dto.response.GetAllTransactionRepDTO;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.service.ContractTokensService;
import com.rst.cgi.service.FlatMoneyService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContractTokensServiceImpl implements ContractTokensService {
    private final Logger logger = LoggerFactory.getLogger(ContractTokensServiceImpl.class);

    @Autowired
    private FlatMoneyService flatMoneyService;
    @Value("${bch.web.url: https://www.blocktrail.com/tBCC/}")
    private String BCH_WEB_URL;
    @Value("${btc.web.url: https://www.blocktrail.com/tBTC/}")
    private String BTC_WEB_URL;

    @Override
    public List<GetAllTransactionRepDTO> dealContractTokensTranB(List<String> addressList,
                                                                 Token token,
                                                                 Long confirmNum,
                                                                 String transType,
                                                                 Integer pageNo,
                                                                 Integer pageSize) {
        List<GetAllTransactionRepDTO> repDTOS = new ArrayList<>();

        if (token == null) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        List<ContractTokenBMessage> ContractTokenBMessageList = this.getContractTokensTranB(addressList,
                token,
                transType,
                pageNo,
                pageSize);
        if (ContractTokenBMessageList == null || ContractTokenBMessageList.isEmpty()) {
            return repDTOS;
        } else {
            Double price = 0d;
            if (CoinType.usdt.getName().equals(token.getName())) {
                price = 1d;
            } else {
                price = flatMoneyService.tokenPriceAdaptation(token.getName());;
            }

            final String webUrl;
            if (CoinType.BTC.getCode().equals(token.getCoinType())) {
                webUrl = BTC_WEB_URL;
            } else if (CoinType.BCH.getCode().equals(token.getCoinType())) {
                webUrl = BCH_WEB_URL;
            } else {
                webUrl = "";
            }


            final Double tokenPrice = price;
            ContractTokenBMessageList.forEach(message -> {
                GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
                repDTO = message.toAllTransactionRepDTO(token, confirmNum, tokenPrice, webUrl);
                //状态判断逻辑
                if (addressList.contains(message.getFromAddress()) &&
                        !addressList.contains(message.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.EXPENSE);
                    repDTOS.add(repDTO);

                } else if (!addressList.contains(message.getFromAddress()) &&
                        addressList.contains(message.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.INCOME);
                    repDTOS.add(repDTO);

                } else if (message.getFromAddress().
                        equalsIgnoreCase(message.getToAddress()) ||
                        addressList.contains(message.getFromAddress()) &&
                                addressList.contains(message.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.EXPENSE);
                    repDTOS.add(repDTO);

                    GetAllTransactionRepDTO repDTOIncome = new GetAllTransactionRepDTO();
                    BeanCopier.getInstance().copyBean(repDTO, repDTOIncome);
                    repDTOIncome.setType(GetAllTransactionRepDTO.INCOME);
                    repDTOS.add(repDTOIncome);

                }
            });
        }
        return repDTOS;
    }



    @Data
    class NewsRepRep{
        private Integer code;
        private String msg;
        private Integer total;
        private List<ContractTokenBMessage> data;
    }

    /**
     * 获取B链上的合约地址的所有交易记录
     * @param addressList
     * @param transType
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<ContractTokenBMessage> getContractTokensTranB(List<String> addressList,
                                                        Token token,
                                                        String transType,
                                                        Integer pageNo,
                                                        Integer pageSize) {
        String contractTokenUrl = null;
        if (CoinType.usdt.getName().equals(token.getName())) {
            contractTokenUrl = Urls.GET_usdt_TRANSACTION;
        } else if (CoinType.WHC.getName().equals(token.getName())) {
            contractTokenUrl = Urls.GET_WHC_TRANSACTION;
        }

        if (contractTokenUrl == null) {
            return null;
        }
        String trancstr  = OkHttpUtil.http(contractTokenUrl)
                .param("addr_list", addressList)
                .param("property_id", token.getOwnerTokenId())
                .param("trans_type", transType)
                .param("page_number", pageNo)
                .param("page_size", pageSize)
                .post();

         NewsRepRep newsRepRep = null;
        try {
            newsRepRep = new Gson().fromJson(trancstr, NewsRepRep.class);
        } catch (Exception e) {
            logger.error("NEWS SERVER返回数据有误");
            CustomException.response(Error.SERVER_EXCEPTION);
        }

        return newsRepRep.getData();
    }

}
