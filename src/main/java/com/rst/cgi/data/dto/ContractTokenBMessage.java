package com.rst.cgi.data.dto;

import com.google.gson.annotations.SerializedName;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.data.dto.response.GetAllTransactionRepDTO;
import com.rst.cgi.data.entity.BlockTransaction;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.VIn;
import com.rst.cgi.data.entity.VOut;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;


@Data
public class ContractTokenBMessage {
     /*typeInt 交易类型编号
          # 0: token send
          # 3: token sto
          # 4: send all
          # 50: create fixed issuance
          # 51: create issuancecrowdsale
          # 53: close crowdsale
          # 54: create managed token
          # 55: grant token
          # 56: revoke token
          # 68: get whc
          # 70: change issuer*/

    @SerializedName("txid")
    private String txId;
    @SerializedName("fee")
    private String fee;
    @SerializedName("sendingaddress")
    private String fromAddress;
    @SerializedName("referenceaddress")
    private String toAddress;
    @SerializedName("type_int")
    private Integer typeInt;
    @SerializedName("type")
    private String type;
    @SerializedName("valid")
    private Boolean success;
    @SerializedName("invalidreason")
    private String failReason;
    @SerializedName("blockhash")
    private String blockHash;
    @SerializedName("blocktime")
    private Long blockTime;
    @SerializedName("block")
    private Long blockHeight;
    @SerializedName("amount")
    private String value;
    @SerializedName("positioninblock")
    private Integer index;
    @SerializedName("burn")
    private String burn;
    @SerializedName("confirmations")
    private Long confirmations;


    public BlockTransaction toTransaction(Token token, Long confirmNum) {
        final Logger logger = LoggerFactory.getLogger(ContractTokenBMessage.class);

        BlockTransaction blockTransaction = new BlockTransaction();
        blockTransaction.setTokenName(token.getName());
        if (success != null && !success) {
            blockTransaction.setStatus(BlockTransaction.FAILED);
        } else if (success != null) {
            /*if (blockHeight == null) {
                blockTransaction.setStatus(BlockTransaction.PENDING);
            } else if (blockHeight >= 0 && blockHeight < confirmNum) {
                blockTransaction.setStatus(BlockTransaction.PENDING);
                blockTransaction.setBlockTime(blockTime);
            } else if (blockHeight >= confirmNum) {
                blockTransaction.setStatus(BlockTransaction.CONFIRMED);
                if (blockHeight == confirmNum) {
                    blockTransaction.setConfirmedTime(System.currentTimeMillis());
                }
            } else {
                blockTransaction.setStatus(BlockTransaction.UNKNOWN);
            }*/
            if (confirmations == 0) {
                blockTransaction.setStatus(BlockTransaction.PENDING);
            } else if (confirmations > 0) {
                blockTransaction.setStatus(BlockTransaction.PACKED);
                blockTransaction.setBlockTime(blockTime);
            } else {
                blockTransaction.setStatus(BlockTransaction.UNKNOWN);
            }
        } else {
            blockTransaction.setStatus(BlockTransaction.UNKNOWN);
        }
        if (blockHeight != null) {
            blockTransaction.setHeight(blockHeight.intValue());
        }


        VIn vIn = new VIn();
        vIn.setAddress(fromAddress);

        VOut vOut = new VOut();
        vOut.setAddress(toAddress);

        BigDecimal mulDecimal = new BigDecimal(Math.pow(10, token.getDecimal()));
        if (StringUtils.isNotBlank(value)) {
            try {
                BigDecimal bigValue = new BigDecimal(value);
                BigDecimal smallValue = bigValue.multiply(mulDecimal);
                vIn.setValue(smallValue.longValue());
                if (fee != null) {
                    BigDecimal feeDecimal = new BigDecimal(fee);
                    vIn.setValue(smallValue.add(feeDecimal).longValue());
                }
                vOut.setValue(smallValue.longValue());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                logger.info("value :{}", value);
            }

        }
        blockTransaction.setVIns(Arrays.asList(vIn));
        blockTransaction.setVOuts(Arrays.asList(vOut));


        if (index != null) {
            blockTransaction.setIndex(index);
        }

        if (success != null) {
            blockTransaction.setWhcIsSuccess(success);
        }

        if (failReason != null) {
            blockTransaction.setWhcFailReason(failReason);
        }
        blockTransaction.setTxId(txId);
        blockTransaction.setTokenName(token.getName());
        blockTransaction.setType(type);
        blockTransaction.setTypeInt(typeInt);

        return blockTransaction;
    }


    public GetAllTransactionRepDTO toAllTransactionRepDTO (Token token,  Long confirmNum, Double tokenPrice,
                                                           String webUrl){
        GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
        repDTO.setSuccess(success);
        repDTO.setHeight(blockHeight == null ? null : blockHeight.intValue());
        repDTO.setHash(txId);
        repDTO.setTokenName(token.getName());
        if (fromAddress != null) {
            GetAllTransactionRepDTO.TranxAddrInfo tranxAddrInfo =
                    new GetAllTransactionRepDTO.TranxAddrInfo();
            tranxAddrInfo.setAddress(fromAddress);
            tranxAddrInfo.setMoney(Long.valueOf(value).doubleValue());
            repDTO.setInputAddress(Arrays.asList(tranxAddrInfo));
        }

        if (toAddress != null) {
            GetAllTransactionRepDTO.TranxAddrInfo tranxAddrInfo =
                    new GetAllTransactionRepDTO.TranxAddrInfo();
            tranxAddrInfo.setAddress(toAddress);
            tranxAddrInfo.setMoney(Long.valueOf(value).doubleValue());
            repDTO.setOutputAddress(Arrays.asList(tranxAddrInfo));
        }

        repDTO.setDecimal(token.getDecimal());
        repDTO.setTxFee(Double.valueOf(fee));
        repDTO.setTime(Objects.isNull(blockTime) ? null : blockTime * 1000);
        repDTO.setPrice(tokenPrice);
        repDTO.setTargetConfirmNum(confirmNum);
        repDTO.setConfirmNum(confirmations);
        if (CoinType.BCH.getCode().equals(token.getCoinType())) {
            repDTO.setWebTxUrl(webUrl + txId);
        } else if (CoinType.BTC.getCode().equals(token.getCoinType())) {
            repDTO.setWebTxUrl(webUrl + txId);
        }


        /*if (blockHeight == null) {
            repDTO.setStatus(GetAllTransactionRepDTO.PENDING);
        } else if (blockHeight >= 0 && blockHeight < confirmNum) {
            repDTO.setStatus(GetAllTransactionRepDTO.PACKED);
        } else if (blockHeight >= confirmNum) {
            repDTO.setStatus(GetAllTransactionRepDTO.CONFIRMED);
        } else {
            repDTO.setStatus(GetAllTransactionRepDTO.UNKNOWN);
        }*/

        if (confirmations == 0) {
            repDTO.setStatus(GetAllTransactionRepDTO.PENDING);
        } else if (confirmations > 0 && confirmations < confirmNum) {
            repDTO.setStatus(GetAllTransactionRepDTO.PACKED);
        } else if (confirmations >= confirmNum) {
            repDTO.setStatus(GetAllTransactionRepDTO.CONFIRMED);
        } else {
            repDTO.setStatus(GetAllTransactionRepDTO.UNKNOWN);
        }
        return repDTO;
    }

}

