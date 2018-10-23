package com.rst.cgi.common.EOS;

import com.rst.cgi.data.dto.response.EOStranactionRepDTO;

import java.text.SimpleDateFormat;
import java.util.*;

public class EOSSignUtil {

    private final RpcService rpcService;

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public EOSSignUtil(String baseUrl) {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        rpcService = Generator.createService(RpcService.class, baseUrl);
    }
    public ChainInfo getChainInfo() {
        return Generator.executeSync(rpcService.getChainInfo());
    }

    public Block getBlock(String blockNumberOrId) {
        return Generator.executeSync(rpcService.getBlock(Collections.singletonMap("block_num_or_id", blockNumberOrId)));
    }
    public EOStranactionRepDTO getTransReal(String contractAccount, String from, String to, String quantity, String memo){
        EOStranactionRepDTO dto = new EOStranactionRepDTO();
        ChainInfo info = getChainInfo();
        // get block info
        Block block = getBlock(info.getLastIrreversibleBlockNum().toString());
        // tx
        Tx tx = new Tx();
        tx.setExpiration(info.getHeadBlockTime().getTime() / 1000 + 60);
        tx.setRef_block_num(info.getLastIrreversibleBlockNum());
        tx.setRef_block_prefix(block.getRefBlockPrefix());
        tx.setNet_usage_words(0L);
        tx.setMax_cpu_usage_ms(0L);
        tx.setDelay_sec(0L);
        // actions
        List<TxAction> actions = new ArrayList();
        // data
        Map<String, Object> dataMap = new LinkedHashMap();
        dataMap.put("from", from);
        dataMap.put("to", to);
        dataMap.put("quantity", new DataParam(quantity, DataType.asset, Action.transfer).getValue());
        dataMap.put("memo", memo);
        // action
        TxAction action = new TxAction(from, contractAccount, "transfer", dataMap);
        actions.add(action);
        tx.setActions(actions);
        byte[] bytes = Sign.transReal(new TxSign(info.getChainId(), tx));
        String data = Sign.parseTransferData(from, to, quantity, memo);
        action.setData(data);
        tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
        dto.setCompression("none");
        dto.setTransaction(tx);
        dto.setBytes(bytes);
        return dto;
    }

    public EOStranactionRepDTO getAccountReal(String creator, String newAccount, String owner, String active, Long buyRam,String stakeNetQuantity, String stakeCpuQuantity, Long transfer){
        EOStranactionRepDTO dto = new EOStranactionRepDTO();
        // get chain info
        ChainInfo info = getChainInfo();
        // get block info
        Block block = getBlock(info.getLastIrreversibleBlockNum().toString());
        // tx
        Tx tx = new Tx();
        tx.setExpiration(info.getHeadBlockTime().getTime() / 1000 + 60);
        tx.setRef_block_num(info.getLastIrreversibleBlockNum());
        tx.setRef_block_prefix(block.getRefBlockPrefix());
        tx.setNet_usage_words(0l);
        tx.setMax_cpu_usage_ms(0l);
        tx.setDelay_sec(0l);
        // actions
        List<TxAction> actions = new ArrayList<>();
        tx.setActions(actions);
        // create
        Map<String, Object> createMap = new LinkedHashMap<>();
        createMap.put("creator", creator);
        createMap.put("name", newAccount);
        createMap.put("owner", owner);
        createMap.put("active", active);
        TxAction createAction = new TxAction(creator, "eosio", "newaccount", createMap);
        actions.add(createAction);
        // buyrap
        Map<String, Object> buyMap = new LinkedHashMap<>();
        buyMap.put("payer", creator);
        buyMap.put("receiver", newAccount);
        buyMap.put("bytes", buyRam);
        TxAction buyAction = new TxAction(creator, "eosio", "buyrambytes", buyMap);
        actions.add(buyAction);
        // buyrap
        Map<String, Object> delMap = new LinkedHashMap<>();
        delMap.put("from", creator);
        delMap.put("receiver", newAccount);
        delMap.put("stake_net_quantity", new DataParam(stakeNetQuantity, DataType.asset, Action.delegate).getValue());
        delMap.put("stake_cpu_quantity", new DataParam(stakeCpuQuantity, DataType.asset, Action.delegate).getValue());
        delMap.put("transfer", transfer);
        TxAction delAction = new TxAction(creator, "eosio", "delegatebw", delMap);
        actions.add(delAction);
        byte[] bytes = Sign.transReal(new TxSign(info.getChainId(), tx));
        String accountData = Sign.parseAccountData(creator, newAccount, owner, active);
        createAction.setData(accountData);
        // data parse
        String ramData = Sign.parseBuyRamData(creator, newAccount, buyRam);
        buyAction.setData(ramData);
        String delData = Sign.parseDelegateData(creator, newAccount, stakeNetQuantity, stakeCpuQuantity,
                transfer.intValue());
        delAction.setData(delData);
        // reset expiration
        tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
        dto.setCompression("none");
        dto.setTransaction(tx);
        dto.setBytes(bytes);
        return dto;

    }


}
