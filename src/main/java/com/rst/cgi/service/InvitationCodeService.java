package com.rst.cgi.service;


import com.rst.cgi.data.dto.response.CodeConsumersDTO;
import com.rst.cgi.data.entity.InvitationCode;

import java.util.List;

/**
 * Created by hujia on 2017/8/29.
 */
public interface InvitationCodeService {
    /**
     * 生成邀请码
     * @param ownerId 邀请码的所有者
     * @return 邀请码
     */
    String productCode(Integer ownerId);

    /**
     * 获取邀请码
     * @param ownerId 邀请码的所有者
     * @return 邀请码
     */
    String getCode(Integer ownerId);


    /**
     * 生成邀请码
     * @param ownerId 邀请码的所有者
     * @param count 邀请码的数量
     * @return 邀请码
     */
    List<String> productCode(Integer ownerId, int count);

    /**
     * 使用邀请码
     * @param code 被使用的邀请码
     * @param consumerId 邀请码的使用者
     * @return true-成功使用
     */
    boolean consumeCode(String code, Integer consumerId);

    /**
     * 获取用户邀请的用户列表
     * @param ownerId 邀请者
     * @return 被邀请人列表
     */
    List<Integer> getCodeConsumers(Integer ownerId);

    /**
     * 邀请码是否可以被使用
     * @param code 邀请码
     * @return true-可以使用
     */
    boolean canConsumeCode(String code);

    /**
     * 获取用户邀请的用户总数
     * @param UserId 邀请者
     * @return 被邀请人列表
     */
    Integer getCodeConsumersNum(Integer UserId);

    /**
     * 获取当前中介已邀请的用户列表
     * @param id 中介用户Id
     * @return
     */
    List<CodeConsumersDTO> getCodeConsumersList(Integer id);

    /**
     * 获取当前用户的邀请人的邀请码信息
     * @param userId
     * @return
     */
    InvitationCode findInviterByUserId(Integer userId);

    /**
     * 获取已邀请的用户的手机号码
     * @param userId
     * @return
     */
    List<String> getInvitedPhonesByUserId(Integer userId);
}
