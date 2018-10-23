package com.rst.cgi.data.dao.mysql;

import com.rst.cgi.data.entity.TokenPriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author huangxiaolin
 * @date 2018-05-17 下午5:46
 */
@Mapper
public interface TokenPriceHistoryDao {

    /**
     * 查询大于指定时间之前的数据，升序排列，去除重复的代币。多个交易所可能存在同一种代币
     * @author huangxiaolin
     * @date 2018-05-17 19:52
     */
    @Select("SELECT * FROM token_price_history WHERE token_from=#{token} AND time_utc >= #{timeUtc}" +
            " AND trade_market = (SELECT trade_market FROM token_price_history WHERE token_from=#{token}" +
            " ORDER BY time_utc DESC LIMIT 1) ORDER BY time_utc ASC")
    List<TokenPriceHistory> findByTokenAndTimeUtc(@Param("token") String token, @Param("timeUtc") String timeUtc);

}
