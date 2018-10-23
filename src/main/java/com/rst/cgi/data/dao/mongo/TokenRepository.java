package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.Token;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by mtb on 2018/4/12.
 */
public interface TokenRepository extends MongoRepository<Token, ObjectId> {
    /**
     * 据token的名称查找token
     * @param name
     * @return
     */
    Token findByName(String name);

    /**
     * 据token的合约地址查找token
     * @param contract
     * @return
     */
    Token findByAddress(String contract);

    /**
     * 据token的名称查找token
     * @param nameList
     * @return
     */
    List<Token> findByNameIn(List<String> nameList);



    /**
     * 查询小于等于输入的版本的代币
     * @param version
     * @return
     */
    List<Token> findByVersionLessThanEqual(Integer version);


    /**
     *  查询小于等于输入的版本的代币，并且关键字模糊查询和排序
     * @param version
     * @return
     */
    Page<Token> findByVersionLessThanEqualAndNameLikeOrFullNameLikeOrAddressOrChecksumAddressAllIgnoreCase(Integer version,
                                                                                                         String name,
                                                                                                         String fullName,
                                                                                                         String address,
                                                                                                         String checksumAddress,
                                                                                                         Pageable pageable);

    /**
     * 查询小于等于输入的版本的代币(分页)
     * @param version
     * @return
     */
    Page<Token> findByVersionLessThanEqual(Integer version, Pageable pageable);

    /**
     * 据token的code查找token
     * @param tokenCode
     * @return
     */
    Token findByTokenCode(Long tokenCode);

}
