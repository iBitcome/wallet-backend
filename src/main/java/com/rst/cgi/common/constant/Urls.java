package com.rst.cgi.common.constant;

public interface Urls {
    String BSM_NEWS_URL = ServerMainUris.BSM_SERVER + "/news/get_news";

    String BSM_TRANSACTION_URL = ServerMainUris.BSM_SERVER + "/es_api/get_tranx/";

    String BSM_TRANSACTION_B_URL = ServerMainUris.BSM_SERVER + "/es_api/get_btc_or_bch_tranx";

    String ZCASH_TRANSATION_URL = ServerMainUris.BSM_SERVER + "/es_api/get_zcash_tranx";

    String BSM_ASSETS_URL = ServerMainUris.BSM_SERVER + "/es_api/get_token_assets";

    String GET_B_ASSETS_URL = ServerMainUris.BSM_SERVER + "/es_api/get_btc_or_bch_asset";

    String GET_UTXO_URL = ServerMainUris.BSM_SERVER + "/es_api/get_btc_or_bch_utxo_list";

    String GET_ZCASH_UTXO_URL = ServerMainUris.BSM_SERVER + "/es_api/get_zcash_utxo_list";

    String CHECK_TRANX_URL = ServerMainUris.BSM_SERVER + "/es_api/check_tx_existence_of_address";

    String GET_TXFEE_URL = ServerMainUris.BSM_SERVER + "/chain_utils/get_recommend_price/";

    String GET_ADDRESS_ASSETS = ServerMainUris.BSM_SERVER + "/es_api/get_btc_bch_asset_per_addr/";

    String GET_FLASH_NEWS = ServerMainUris.BSM_SERVER + "/news/get_flash_news";

    String GET_TX_INFO = ServerMainUris.BSM_SERVER + "/es_api/get_btc_bch_transaction_by_id";

    String GET_ZCASH_TX_INFO = ServerMainUris.BSM_SERVER + "/es_api/get_zcash_tranx_by_id";

    String GET_ETH_TX_INFO = ServerMainUris.BSM_SERVER + "/es_api/get_eth_transaction_by_id/";

    String GET_DEX_ETH_TX = ServerMainUris.BSM_SERVER + "/es_api/get_dex_eth_tx_by_addr";

    String HUO_BI_KLINE_URL = ServerMainUris.HUO_BI_PRO + "/market/history/kline";

    String GET_MONEY_RATE = ServerMainUris.JUHE + "/onebox/exchange/query";

    String GET_MONEY = ServerMainUris.JUHE + "/onebox/exchange/list";

    String EXCHANGE_PRICE_URL = ServerMainUris.EXCHANGE_BASE_URL+ "/api/custom/current/price";

    String GET_USDT_TO_CNY = ServerMainUris.EXCHANGE_BASE_URL + "/api/custom/const/usdt2CNY";

    String GET_EXCHANGE_SYMBOLS = ServerMainUris.EXCHANGE_BASE_URL + "/api/custom/const/symbols";

    String EOS_CHAIN_ACCOUNT = ServerMainUris.BSM_SERVER  + "/es_api/get_eos_accounts";

    String EOS_CHAIN_TRANSCTION = ServerMainUris.EOS_NODE_URL  + "/v1/chain/push_transaction";

    String EOS_TRANSACTION_RECORD = ServerMainUris.BSM_SERVER + "/es_api/get_eos_tx_by_account";

    String EOS_ACCOUNT_ASSET = ServerMainUris.BSM_SERVER + "/es_api/get_eos_currency_balance";

    String EOS_BASE_PAY = ServerMainUris.BSM_SERVER + "/es_api/get_new_account_recommend_eos_num";

    String EOS_REAL=ServerMainUris.EOS_NODE_URL;

    String GET_WHC_ADDRESS_ASSET = ServerMainUris.BSM_SERVER + "/es_api/get_whc_asset";

    String GET_WHC_TRANSACTION = ServerMainUris.BSM_SERVER + "/es_api/get_whc_tranx";

    String GET_usdt_ADDRESS_ASSET = ServerMainUris.BSM_SERVER + "/es_api/get_omni_asset";

    String GET_usdt_TRANSACTION = ServerMainUris.BSM_SERVER + "/es_api/get_omni_tranx";

    String GET_GATEWAY_EXCONFIG = ServerMainUris.GATEWAY_URL + "/exconfig";

    String GET_GATEWAY_PAYLOADE = ServerMainUris.GATEWAY_URL + "/mint_payload";

    String GET_GATEWAY_TOKEN_REG = ServerMainUris.GATEWAY_URL + "/tokenreg";

    String GET_GATEWAY_LAST_BLOCK = ServerMainUris.GATEWAY_URL + "/block/current";

    String MAILGUN_SEND = ServerMainUris.MAILGUN_URL + "/messages";




    //dex接口
    String DEX_MARKET_URL = "/v1/market";
    String DEX_PAIRS_URL = "/v1/pairlist/";
    String DEX_PAIR_INFO_URL = "/v1/pairinfo/";
}
