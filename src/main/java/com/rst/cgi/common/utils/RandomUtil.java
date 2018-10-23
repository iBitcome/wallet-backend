package com.rst.cgi.common.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by matianbao on 2018/1/2.
 */
public class RandomUtil {
    /**
     * 生成指定长度的数字+字母组合的随机字符串
     * (支持多线程)
     * @param length
     * @param caseLetter
     * @return
     */
    public static String randomLetterAndNumber(int length, boolean caseLetter){
        String val = "";
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i<length; i++) {
            String type = random.nextInt(2) % 2 ==0 ? "letter" : "number";
            if (caseLetter) {
                if ("letter".equalsIgnoreCase(type)) {
                    int begin = random.nextInt(2) % 2 ==0 ? 65 : 97;
                    val = val + (char)(begin + random.nextInt(26));
                } else if ("number".equalsIgnoreCase(type)) {
                    val = val + random.nextInt(10);
                }
            }else {
                if ("letter".equalsIgnoreCase(type)) {
                    int begin = 65;
                    val = val + (char)(begin + random.nextInt(26));
                } else if ("number".equalsIgnoreCase(type)) {
                    val = val + random.nextInt(10);
                }
            }

        }
        return val;
    }

    /**
     * 生成指定位数的随机数
     * @author huangxiaolin
     * @date 2018-05-14 17:28
     * @param digit 生成随机数的位数，值为1到9位
     */
    public static int genRandomNum(int digit) {
        if (digit < 1 || digit > 9) {
            throw new IllegalArgumentException("digit 的范围应该是1到9位");
        }
        StringBuilder numStr = new StringBuilder("1");
        for (int i = 1; i < digit; i++) {
            numStr.append("0");
        }
        int minNum = Integer.parseInt(numStr.toString());
        int maxNum = minNum * 10;
        return ThreadLocalRandom.current().nextInt(maxNum - minNum) + minNum;
    }
}
