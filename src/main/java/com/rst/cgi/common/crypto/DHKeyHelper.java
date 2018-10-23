package com.rst.cgi.common.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.ZERO;


/**
 * @author hujia
 */
class DHKeyHelper {
    private static final BigInteger ONE = BigInteger.valueOf(1);

    private static final BigInteger TWO = BigInteger.valueOf(2);

    public static BigInteger P = new BigInteger("fca682ce8e12caba26efccf7110e526db078b05edecbcd1eb4a208" +
            "f3ae1617ae01f35b91a47e6df63413c5e12ed0899bcd132acd50d99151bdc43ee737592e17", 16);
    public static BigInteger G = new BigInteger("678471b27a9cf44ee91a49c5147db1a9aaf244f05a434d6486931d" +
            "2d14271b9e35030b71fd73da179069b32e2935630e1c2062354d0da20a6c416e50be794ca4", 16);
    public static SecureRandom random = new SecureRandom();

    public static BigInteger calculatePrivate(int limit) {
        if (limit == 0) {
            calculatePrivate();
        }

        int minWeight = limit >>> 2;
        do {
            BigInteger x = new BigInteger(limit, random).setBit(limit - 1);
            if (getNafWeight(x) >= minWeight)
            {
                return x;
            }
        } while (true);
    }

    public static BigInteger calculatePrivate() {
        BigInteger min = TWO;
        BigInteger max = P.subtract(TWO);
        int minWeight = max.bitLength() >>> 2;

        do {
            BigInteger x = createRandomInRange(min, max, random);
            if (getNafWeight(x) >= minWeight)
            {
                return x;
            }
        } while (true);
    }

    public static BigInteger calculatePublic(BigInteger privateValue) {
        return G.modPow(privateValue, P);
    }

    public static BigInteger calculateK(BigInteger privateValue, BigInteger publicValue) {
        return publicValue.modPow(privateValue, P);
    }

    public static void main(String[] args) throws Exception {
        BigInteger priv1 = DHKeyHelper.calculatePrivate();
        BigInteger pub1 = DHKeyHelper.calculatePublic(priv1);
        System.out.println("priv1:" + priv1);
        System.out.println("pub1:" + pub1);

        BigInteger priv2 = DHKeyHelper.calculatePrivate();
        BigInteger pub2 = DHKeyHelper.calculatePublic(priv2);
        System.out.println("priv2:" + priv2);
        System.out.println("pub2:" + pub2);

        BigInteger k1 = DHKeyHelper.calculateK(priv1, pub2);
        BigInteger k2 = DHKeyHelper.calculateK(priv2, pub1);
        System.out.println("k1:" + k1);
        System.out.println("k2:" + k2);

        String IV_STRING = "rst@123456--java";
        byte[] ivBytes = IV_STRING.getBytes();
        System.out.println(Converter.byteArrayToHexString(ivBytes));
    }

    /**======以下暂时忽略，我们采用常量=======*/
    private BigInteger[] generateSafePrimes(int size, int certainty, SecureRandom random) {
        BigInteger p, q;
        int qLength = size - 1;
        int minWeight = size >>> 2;

        do {
            q = new BigInteger(qLength, 2, random);
            p = q.shiftLeft(1).add(ONE);

            if (!p.isProbablePrime(certainty)) {
                continue;
            }

            if (certainty > 2 && !q.isProbablePrime(certainty - 2)) {
                continue;
            }

            if (getNafWeight(p) < minWeight) {
                continue;
            }

            break;
        } while (true);

        return new BigInteger[] { p, q };
    }

    private BigInteger selectGenerator(BigInteger p, BigInteger q, SecureRandom random) {
        BigInteger pMinusTwo = p.subtract(TWO);
        BigInteger g;

        do
        {
            BigInteger h = createRandomInRange(TWO, pMinusTwo, random);

            g = h.modPow(TWO, p);
        } while (g.equals(ONE));

        return g;
    }

    public void generateParameters() {
        BigInteger[] safePrimes = generateSafePrimes(512, 2, random);

        P = safePrimes[0];
        G = selectGenerator(P, safePrimes[1], random);
    }

    public static int getNafWeight(BigInteger k) {
        if (k.signum() == 0)
        {
            return 0;
        }

        BigInteger _3k = k.shiftLeft(1).add(k);
        BigInteger diff = _3k.xor(k);

        return diff.bitCount();
    }

    private static final int MAX_ITERATIONS = 1000;
    public static BigInteger createRandomInRange(
            BigInteger      min,
            BigInteger      max,
            SecureRandom    random) {
        int cmp = min.compareTo(max);
        if (cmp >= 0) {
            if (cmp > 0) {
                throw new IllegalArgumentException("'min' may not be greater than 'max'");
            }

            return min;
        }

        if (min.bitLength() > max.bitLength() / 2) {
            return createRandomInRange(ZERO, max.subtract(min), random).add(min);
        }

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            BigInteger x = new BigInteger(max.bitLength(), random);
            if (x.compareTo(min) >= 0 && x.compareTo(max) <= 0)
            {
                return x;
            }
        }

        // fall back to a faster (restricted) method
        return new BigInteger(max.subtract(min).bitLength() - 1, random).add(min);
    }
}

