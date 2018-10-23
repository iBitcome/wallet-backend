package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.utils.EncodeUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.InvitationCodeDao;
import com.rst.cgi.data.dto.response.CodeConsumersDTO;
import com.rst.cgi.data.entity.InvitationCode;
import com.rst.cgi.data.entity.InvitationCodeConsumer;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.InvitationCodeService;
import com.rst.cgi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hujia on 2017/8/29.
 */
@Service
public class InvitationCodeServiceImpl implements InvitationCodeService {
    private static final String CODE_SEQUENCE = "InvitationCodeService.sequence";
    private static long START_TIMESTAMP = 1503988050765L;
    private static long MAGIC_UIN = 10000000;
    private static int HEX = 36;

    private static final int CODE_VALID = 1;
    private static final int CODE_INVALID = 0;


    private final Logger logger = LoggerFactory.getLogger(InvitationCodeServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private InvitationCodeDao invitationCodeDao;
    @Autowired
    private CommonDao commonDao;

    @Override
    public String productCode(Integer ownerId) {
        InvitationCode invitationCodeHas = invitationCodeDao.queryCodeByOwnerId(ownerId);
        if (Objects.nonNull(invitationCodeHas)) {
            return invitationCodeHas.getCode();
        }

        long sequence = System.currentTimeMillis() - START_TIMESTAMP;
        if (stringRedisTemplate.hasKey(CODE_SEQUENCE)) {
            long newSequence = Long.parseLong(stringRedisTemplate.opsForValue().get(CODE_SEQUENCE));
            if (sequence > newSequence) {
                sequence = newSequence;
            }
        }

        sequence++;

        stringRedisTemplate.opsForValue().set(CODE_SEQUENCE, sequence + "");

        String code = EncodeUtil.hexNString(MAGIC_UIN - ownerId, HEX, 4)
                + EncodeUtil.hexNString(sequence, HEX, 4);

        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setCode(code);
        invitationCode.setOwnerId(ownerId);
        invitationCode.setStatus(CODE_VALID);
        invitationCodeDao.insertCode(invitationCode);

        return code;
    }

    @Override
    public String getCode(Integer ownerId) {
        InvitationCode code = invitationCodeDao.queryCodeByOwnerId(ownerId);
        if (code != null) {
            return code.getCode();
        }

        return "";
    }

    @Override
    public List<String> productCode(Integer ownerId, int count) {
        int userId = 1;
        if (ownerId != null) {
            userId = ownerId;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity = commonDao.queryFirstBy(userEntity);
        if (userEntity == null) {
            userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setPassword("system");
            userEntity.setEmail("system");
            userEntity.setCreateTime(new Date());
            userEntity.setUpdateTime(new Date());
            commonDao.insert(userEntity);
        }

        long sequence = System.currentTimeMillis() - START_TIMESTAMP;
        if (stringRedisTemplate.hasKey(CODE_SEQUENCE)) {
            long newSequence = Long.parseLong(stringRedisTemplate.opsForValue().get(CODE_SEQUENCE));
            if (sequence > newSequence) {
                sequence = newSequence;
            }
        }

        List<String> result = new ArrayList<>(count);
        List<InvitationCode> codes = new ArrayList<>(count);
        Random random = new Random();
        do {
            sequence++;

            String code = EncodeUtil.hexNString(MAGIC_UIN + random.nextInt(1000000), HEX, 4)
                    + EncodeUtil.hexNString(sequence, HEX, 4);

            InvitationCode invitationCode = new InvitationCode();
            invitationCode.setCode(code);
            invitationCode.setOwnerId(userId);
            invitationCode.setStatus(CODE_VALID);
            codes.add(invitationCode);
            result.add(code);
        } while (--count > 0);
        invitationCodeDao.batchInsert(codes);

        stringRedisTemplate.opsForValue().set(CODE_SEQUENCE, sequence + "");

        return result;
    }

    @Override
    public boolean consumeCode(String code, Integer consumerId) {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (!canConsumeCode(code)) {
            return false;
        }

        InvitationCode invitationCode = invitationCodeDao.queryCode(code);
        InvitationCodeConsumer dbCodeConsumer = invitationCodeDao.queryFirstByConsumer(consumerId);
        if (invitationCode.getOwnerId().equals(consumerId)) {
            CustomException.response(Error.ERR_MSG_INVITATION_SELF);
        }

        if (Objects.nonNull(dbCodeConsumer)) {
            CustomException.response(Error.ERR_MSG_INVITATIONED_TWICE);
        }

        InvitationCodeConsumer invitationCodeConsumer = new InvitationCodeConsumer();
        invitationCodeConsumer.setConsumerId(consumerId);
        invitationCodeConsumer.setInvitationCodeId(invitationCode.getId());
        boolean ret = invitationCodeDao.insertCodeConsumer(invitationCodeConsumer);
        return ret;
    }

    @Override
    public List<Integer> getCodeConsumers(Integer ownerId) {
        return invitationCodeDao.queryConsumersByOwnerId(ownerId);
    }

    @Override
    public boolean canConsumeCode(String code) {
        InvitationCode invitationCode = invitationCodeDao.queryCode(code);
        if(Objects.isNull(invitationCode)){
            return false;
        }
        return invitationCode.getStatus() == CODE_VALID;
    }

    @Override
    public Integer getCodeConsumersNum(Integer UserId) {
        List<Integer> list = this.getCodeConsumers(UserId);
        return list.size();
    }

    @Override
    public List<CodeConsumersDTO> getCodeConsumersList(Integer id) {
        List<CodeConsumersDTO> codeConsumersDTOList = new ArrayList<>();
        List<UserEntity> consumersList = invitationCodeDao.getCodeConsumersList(id);
        if (consumersList == null) {
            return null;
        }

        for (UserEntity user : consumersList) {
            CodeConsumersDTO codeConsumersDTO = new CodeConsumersDTO();
            String status = UserService.HAVE_WALLET;
            switch (user.getStatus()) {
                case 0: status = UserService.HAVE_WALLET;break;
                case 1: status = UserService.DELETED;break;
                case 2: status = UserService.NOT_HAVEN_WALLET;break;
                default: break;
            }
            codeConsumersDTO.setStatus(status);
            codeConsumersDTO.setId(user.getId());
            codeConsumersDTO.setPhoneNo(user.getPhone());
            codeConsumersDTO.setEmail(user.getEmail());
            codeConsumersDTOList.add(codeConsumersDTO);
        }
        return codeConsumersDTOList;
    }

    @Override
    public InvitationCode findInviterByUserId(Integer userId) {
        return invitationCodeDao.findInviterByUserId(userId);
    }

    @Override
    public List<String> getInvitedPhonesByUserId(Integer userId) {
        InvitationCode invitationCode = invitationCodeDao.queryCodeByOwnerId(userId);
        if (invitationCode != null) {
            return invitationCodeDao.findInvitedPhonesByCodeId(invitationCode.getId());
        }
       return null;
    }
}
