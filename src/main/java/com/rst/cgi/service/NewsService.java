package com.rst.cgi.service;

import com.rst.cgi.data.dto.request.FlashNewsReqDTO;
import com.rst.cgi.data.dto.request.GetNewsReqDTO;
import com.rst.cgi.data.dto.response.FlashNewsRepDTO;
import com.rst.cgi.data.dto.response.GetNewsRepDTO;
import com.rst.cgi.data.dto.response.WalletAssistantResDTO;

import java.util.List;

/**
 * Created by mtb on 2018/4/3.
 */
public interface NewsService {
    GetNewsRepDTO getNews(GetNewsReqDTO body);

    FlashNewsRepDTO getFlashNews(FlashNewsReqDTO body);

    List<WalletAssistantResDTO> WalletAssistantMsg();
}
