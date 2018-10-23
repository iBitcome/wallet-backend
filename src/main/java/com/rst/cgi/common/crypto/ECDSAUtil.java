package com.rst.cgi.common.crypto;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;

import java.math.BigInteger;

/**
 * @author hujia
 */
public class ECDSAUtil {
    private final static Logger logger = LoggerFactory.getLogger(ECDSAUtil.class);

    public static ECDSASignature signEth(byte[] hash, byte[] privateKey) {
        int nonce = 0;
        while (nonce < 25){
            ECDSASignature signature = generateSignature(hash, privateKey, nonce++);
            signature.findRecoveryId(hash, privateKey);

            if (signature.recId > 0) {
                return signature;
            }
        }

        return null;
    }

    public static ECDSASignature signEos(byte[] hash, byte[] privateKey) {
        int nonce = 0;
        while (nonce < 25){
            ECDSASignature signature = generateSignature(hash, privateKey, nonce++);
            signature.findRecoveryId(hash, privateKey);

            if (signature.recId < 0) {
                continue;
            }

            byte[] compactSig = signature.compact(true);
            if (isCanonical(compactSig)) {
                return signature;
            }
        }

        return null;
    }

    public static boolean isCanonical(byte[] compactSig) {
        return (compactSig[1] & 0x80) == 0
                && !(compactSig[1] == 0 && ((compactSig[2] & 0x80) == 0))
                && (compactSig[33] & 0x80) == 0
                && !(compactSig[33] == 0 && ((compactSig[34] & 0x80) == 0));
    }

    public static ECDSASignature generateSignature(byte[] hash, byte[] privateKey, int nonce) {
        BigInteger privateIntValue = new BigInteger(1, privateKey);

        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculatorEx(new SHA256Digest(), nonce));
        ECPrivateKeyParameters privateKeyParam = new ECPrivateKeyParameters(privateIntValue, Constant.CURVE);

        signer.init(true, privateKeyParam);

        BigInteger[] components = signer.generateSignature(hash);
        final ECDSASignature signature = new ECDSASignature(components[0], components[1]);
        signature.ensureCanonical();

        return signature;
    }

    public static boolean verify(byte[] data, byte[] pub, BigInteger r, BigInteger s) {
        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(Constant.CURVE.getCurve().decodePoint(pub),
                Constant.CURVE));
        return signer.verifySignature(data, r, s);
    }

    public static boolean verify(byte[] data, String signature) {
        BigInteger r = new BigInteger(signature.substring(2, 66), 16);
        BigInteger s = new BigInteger(signature.substring(66, 130), 16);
        int recId = new BigInteger(signature.substring(130, 132), 16).intValue();
        ECDSASignature ecdsaSignature = new ECDSASignature(r, s);
        byte[] pub = ECDSASignature.recoverPubKey(data, ecdsaSignature, recId);

        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(Constant.CURVE.getCurve().decodePoint(pub),
                Constant.CURVE));
        return signer.verifySignature(data, r, s);
    }

    public static String sigToString(ECDSASignature sig) {
        String R = StringUtil.toHexString(Converter.integerToBytes(sig.r, 32));
        String S = StringUtil.toHexString(Converter.integerToBytes(sig.s, 32));
        if (sig.recId >= 0) {
            String V = sig.recId == 0 ? "00" : "01";
            return "0x" + R + S + V;
        }

        return "0x" + R + S;
    }
}
