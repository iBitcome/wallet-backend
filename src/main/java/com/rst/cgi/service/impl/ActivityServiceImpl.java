package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.ActivityRepository;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dao.mysql.ActivityDao;
import com.rst.cgi.data.dao.mysql.WalletAddressDao;
import com.rst.cgi.data.dao.mysql.WalletDao;
import com.rst.cgi.data.dto.request.ActivityAddressReqDTO;
import com.rst.cgi.data.dto.response.GetDappRepDTO;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.AddressClient;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private WalletAddressDao walletAddressDao;
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private ActivityDao activityDao;

    @Value("${net.work.type:test_reg}")
    private String networkType;

    @Override
    public void submitActivityInfo(ActivityAddressReqDTO body) {
        Token token = tokenRepository.findByTokenCode(body.getTokenCode());
        if (Objects.isNull(token)) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        String address = body.getAddress();
        address = AddressClient.addressToHash(token.getCoinType(),address, networkType);

        List<Wallet> walletList = walletDao.queryOwnerCreateByAddress(address);
        if (walletList == null || walletList.size() == 0) {
            CustomException.response(Error.ERR_MSG_ADDRESS_NOT_IN);
        }

        ActivityInfo activityInfoDb = activityRepository.findByAddressAndActivityName(address, body.getActivityName());
        if (Objects.nonNull(activityInfoDb)) {
            CustomException.response(Error.ERR_MSG_ADDRESS_EXIST);
        }

        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.setAddress(address);
        activityInfo.setTokenCode(token.getTokenCode());
        activityInfo.setTokenName(token.getName());
        activityInfo.setCreateTime(System.currentTimeMillis());
        activityInfo.setActivityName(body.getActivityName());
        activityRepository.save(activityInfo);
    }

    @Override
    public List<GetDappRepDTO> GetDappInfo() {
        List<GetDappRepDTO> result = new ArrayList<>();
        Integer languageType = CurrentThreadData.language();
        List<DappInfo> dappInfoList = activityDao.GetDapps(languageType);

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dappInfoList.forEach(dappInfo -> {
            GetDappRepDTO dappRepDTO = new GetDappRepDTO();
            BeanCopier.getInstance().copyBean(dappInfo, dappRepDTO);
            dappRepDTO.setCreateTime(spf.format(dappInfo.getCreateTime()));
            result.add(dappRepDTO);
        });
        return result;
    }
}
