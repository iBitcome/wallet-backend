package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.Web3jClient;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.service.ETHGasService;
import com.rst.cgi.service.ETHNonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

@Service
public class ETHGasServiceImpl implements ETHGasService {
    private final Logger logger = LoggerFactory.getLogger(ETHGasServiceImpl.class);
    @Autowired
    private Web3jClient web3jClient;
    @Autowired
    private ETHNonceService ethNonceService;

    @Override
    public long getETHGasLimit(String fromAddress, String toAddress) {
        Long gasLimit = 0l;
        BigInteger nonce = BigInteger.valueOf(ethNonceService.nextNonce(fromAddress));

       Transaction transaction = Transaction.createEtherTransaction(
                        fromAddress,
                        nonce,
                        BigInteger.valueOf(50000000000l),
                        org.web3j.protocol.core.methods.request.Transaction.DEFAULT_GAS,
                        toAddress,
                        BigInteger.valueOf(1000000000000000000l)
                );
        gasLimit = getLimit(transaction);
        return gasLimit;
    }

    @Override
    public long getTokenGasLimit(String fromAddress, String toAddress, String tokenAddress) {

        Long gasLimit = 0l;

        ArrayList<Type> dataParam = new ArrayList<>();
        Token ETHToken = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(CoinType.ETH.getName());
        dataParam.add(new Address(toAddress));
        dataParam.add(new Uint256(BigInteger.ONE.pow(ETHToken.getDecimal())));

        ArrayList<TypeReference<?>> outParams = new ArrayList<>();
        outParams.add(new TypeReference<Bool>() {
        });

        Function function = new Function(
                "transfer",
                dataParam,
                outParams);
        String encodedFunction = FunctionEncoder.encode(function);

        BigInteger nonce = BigInteger.valueOf(ethNonceService.nextNonce(fromAddress));
        Transaction transaction = Transaction.createFunctionCallTransaction(
                fromAddress,
                nonce,
                BigInteger.valueOf(50000000000l),
                org.web3j.protocol.core.methods.request.Transaction.DEFAULT_GAS,
                tokenAddress,
                encodedFunction
        );
        gasLimit = getLimit(transaction);
        return gasLimit + 10000;
    }



    private Long getLimit(org.web3j.protocol.core.methods.request.Transaction transaction){
        Long gasLimit = 0l;
        Web3j web3j = web3jClient.getWeb3j();
        try {
            EthEstimateGas estimateGas =  web3j.ethEstimateGas(transaction).send();
            if (estimateGas.hasError()) {
                logger.error(estimateGas.getError().getMessage());
                CustomException.response(Error.GET_GAS_LIMIT_FAIL);
            } else {
                gasLimit =  estimateGas.getAmountUsed().longValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CustomException.response(Error.GET_GAS_LIMIT_FAIL);
        }
        return gasLimit;
    }
}
