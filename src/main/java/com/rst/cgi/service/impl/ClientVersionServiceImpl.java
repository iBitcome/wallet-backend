package com.rst.cgi.service.impl;

import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.data.dao.mongo.VersionConfigureRepository;
import com.rst.cgi.data.dto.response.VersionConfigResDTO;
import com.rst.cgi.data.entity.VersionConfigure;
import com.rst.cgi.service.ClientVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mtb
 * @date 2018/4/14
 */
@Service
public class ClientVersionServiceImpl implements ClientVersionService{
    public static final String VERSION_PLATFORM = "iBitcome";

    public static final String CLIENT_IOS = "ios";
    public static final String CLIENT_ANDROID = "android";
    public static final String CLIENT_H5 = "h5";

    @Autowired
    private VersionConfigureRepository versionConfigureRepository;

    @Override
    public void updateConfigure(VersionConfigure body) {
        body.setPlatform(VERSION_PLATFORM);
        versionConfigureRepository.save(body);
    }

    @Override
    public VersionConfigResDTO getVersionConfig() {
        VersionConfigResDTO versionConfigResDTO = new VersionConfigResDTO();
        VersionConfigure versionConfigure = versionConfigureRepository.findByPlatform(VERSION_PLATFORM);
        if (versionConfigure != null) {
            versionConfigResDTO.setAndroidFake(versionConfigure.isAndroidFake());
            versionConfigResDTO.setIosFake(versionConfigure.isIosFake());
            versionConfigResDTO.setNeedForceUpdate(!isValidClient());
            versionConfigResDTO.setLatestVersion(isLatestClient());
            versionConfigResDTO.setIosLatestVersion(versionConfigure.getIosMaxVersion());
            versionConfigResDTO.setAndroidLatestVersion(versionConfigure.getAndroidMaxVersion());
            versionConfigResDTO.setAndroidDownloadUrl(versionConfigure.getAndroidDownloadUrl());
            versionConfigResDTO.setIosDownloadUrl(versionConfigure.getIosDownloadUrl());
        } else {
            versionConfigResDTO.setAndroidFake(false);
            versionConfigResDTO.setIosFake(false);
            versionConfigResDTO.setNeedForceUpdate(false);
            versionConfigResDTO.setLatestVersion(true);
            versionConfigResDTO.setIosLatestVersion("0");
            versionConfigResDTO.setAndroidLatestVersion("0");
            versionConfigResDTO.setIosDownloadUrl("");
            versionConfigResDTO.setAndroidDownloadUrl("");
        }

        return versionConfigResDTO;
    }

    @Override
    public boolean isValidClient() {
        VersionConfigure versionConfigure = versionConfigureRepository.findByPlatform(VERSION_PLATFORM);
        if (versionConfigure == null) {
            return true;
        }

        String clientPlatform = CurrentThreadData.clientPlatform();
        Integer clientVersion = CurrentThreadData.clientVersion();

        if (clientVersion == null) {
            return false;
        }

        if (CLIENT_H5.equalsIgnoreCase(clientPlatform)) {
            return true;
        }

        if (CLIENT_IOS.equalsIgnoreCase(clientPlatform)) {
            return clientVersion >= Integer.parseInt(versionConfigure.getIosValidMinVersion());
        } else if (CLIENT_ANDROID.equalsIgnoreCase(clientPlatform)) {
            return clientVersion >= Integer.parseInt(versionConfigure.getAndroidValidMinVersion());
        }

        return false;
    }


    @Override
    public boolean isLatestClient() {
        VersionConfigure versionConfigure = versionConfigureRepository.findByPlatform(VERSION_PLATFORM);

        if (versionConfigure == null) {
            return true;
        }

        Integer clientVersion = CurrentThreadData.clientVersion();

        if (clientVersion == null) {
            return false;
        }

        if (CLIENT_IOS.equalsIgnoreCase(CurrentThreadData.clientPlatform())) {
            return clientVersion >= Integer.parseInt(versionConfigure.getIosMaxVersion());
        } else if (CLIENT_ANDROID.equalsIgnoreCase(CurrentThreadData.clientPlatform())) {
            return clientVersion >= Integer.parseInt(versionConfigure.getAndroidMaxVersion());
        }

        return false;
    }
}
