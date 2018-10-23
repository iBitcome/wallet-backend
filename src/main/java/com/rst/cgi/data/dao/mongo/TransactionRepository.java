package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.Transaction;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by mtb on 2018/4/8.
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, ObjectId>{
    Page<Transaction> findByFromOrTo(String fromWalletAddress, String toWalletAddress,  Pageable pageable);

    Page<Transaction> findByFrom(String fromWalletAddress,  Pageable pageable);

    Page<Transaction> findByTo(String fromWalletAddress,  Pageable pageable);

    Page<Transaction> findAllByFromInAndContractOrToInAndContract(List<String> fromWalletAddress,
                                                                  String contract1,
                                            List<String> toWalletAddress,
                                            String contract2,
                                            Pageable pageable);



    Page<Transaction> findAllByFromInAndMethodOrToInAndMethod(List<String> fromWalletAddress,
                                                        String method1,
                                                        List<String> toWalletAddress,
                                                        String method2,
                                                        Pageable pageable);

    Page<Transaction> findAllByToInAndMethod(List<String> toWalletAddress,
                                             String method,
                                             Pageable pageable);

    Page<Transaction> findAllByFromInAndMethod(List<String> fromWalletAddress,
                                               String method,
                                               Pageable pageable);


    Page<Transaction> findAllByFromInOrToInAndContractAndInput_MethodIn(List<String> fromWalletAddress,
                                                                        List<String> toWalletAddress,
                                                                        String contract,
                                                                        List<String> method,
                                                                        Pageable pageable);

    Page<Transaction> findAllByFromInAndContractAndInput_MethodIn(List<String> fromWalletAddress,
                                                 String contract,
                                                 List<String> method,
                                                 Pageable pageable);



    Page<Transaction> findAllByToInAndContractAndInput_MethodIn(List<String> toWalletAddress,
                                               String contract,
                                               List<String> method,
                                               Pageable pageable);




}
