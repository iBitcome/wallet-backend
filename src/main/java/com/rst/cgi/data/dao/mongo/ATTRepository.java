package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.AddressToTransaction;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author hujia
 */
public interface ATTRepository extends MongoRepository<AddressToTransaction, ObjectId> {
    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param pageable
     * @return
     */
    Page<AddressToTransaction> findAllByAddressIn(List<String> address, Pageable pageable);

    /**
     * 根据地址找到所有关联交易
     * @param statuses
     * @return
     */
    List<AddressToTransaction> findAllByStatusIn(List<Integer> statuses);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @return
     */
    List<AddressToTransaction> findAllByTokenInAndAddressIn(List<String> tokens,
                                                            List<String> address);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param begin
     * @param end
     * @param pageable
     * @param isRollOut
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndAddressInAndConfirmedTimeBetween(
            List<Boolean> isRollOut, List<String> address, Long begin, Long end, Pageable pageable);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param begin
     * @param end
     * @param pageable
     * @param isRollOut
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndAddressInAndPendingTimeBetween(
            List<Boolean> isRollOut, List<String> address, Long begin, Long end, Pageable pageable);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param begin
     * @param end
     * @param pageable
     * @param isRollOut
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndAddressInAndBlockTimeBetween(
            List<Boolean> isRollOut, List<String> address, Long begin, Long end, Pageable pageable);


    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @param begin
     * @param end
     * @param isRollOut
     * @param pageable
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndBlockTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end, Pageable pageable);

    /**
     * 根据地址找到所有关联交易
     * @param tokens
     * @param address
     * @param begin
     * @param end
     * @param isRollOut
     * @return
     */
    List<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndBlockTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @param begin
     * @param end
     * @param isRollOut
     * @return
     */
    List<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndPendingTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @param begin
     * @param end
     * @param isRollOut
     * @param pageable
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndPendingTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end, Pageable pageable);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @param begin
     * @param end
     * @param isRollOut
     * @return
     */
    List<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndConfirmedTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end);

    /**
     * 根据地址找到所有关联交易
     * @param address
     * @param tokens
     * @param begin
     * @param end
     * @param isRollOut
     * @param pageable
     * @return
     */
    Page<AddressToTransaction> findAllByRollOutInAndTokenInAndAddressInAndConfirmedTimeBetween(
            List<Boolean> isRollOut, List<String> tokens, List<String> address, Long begin, Long end, Pageable pageable);


    /**
     * 根据交易id找到所有关联地址
     * @param txId
     * @return
     */
    List<AddressToTransaction> findByTxId(String txId);

    /**
     * 根据交易id和地址找到关联记录
     * @param txId
     * @param address
     * @return
     */
    AddressToTransaction findByTxIdAndAddress(String txId, String address);
}
