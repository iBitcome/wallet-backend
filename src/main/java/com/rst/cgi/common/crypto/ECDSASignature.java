package com.rst.cgi.common.crypto;

import com.google.common.base.Preconditions;
import com.rst.cgi.common.constant.Constant;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;


/**
 * @author hujia
 */
public class ECDSASignature {
    public BigInteger r;
    public BigInteger s;
    public int recId;

    public ECDSASignature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
        this.recId = -1;
    }

    /**
     * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
     * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
     * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
     * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
     * considered legal and the other will be banned.
     */
    public void ensureCanonical() {
        if (s.compareTo(Constant.HALF_CURVE_ORDER) > 0) {
            s = Constant.CURVE.getN().subtract(s);
        }
    }

    /**
     * DER is an international standard for serializing data structures which is widely used in cryptography.
     * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
     * of the signature, as recognized by OpenSSL and other libraries.
     */
    public byte[] encodeToDER() {
        try {
            return derByteStream().toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public int findRecoveryId(byte[] hash, byte[] privateKey) {
        BigInteger privateIntValue = new BigInteger(1, privateKey);
        ECPoint multiply = Constant.CURVE.getG().multiply(privateIntValue);
        byte[] pubKey = multiply.getEncoded(true);

        for (int i = 0; i < 4; i++) {
            byte[] recovered = recoverPubKey(hash, this, i);
            if (Arrays.equals(pubKey, recovered)) {
                this.recId = i;
                break;
            }
        }

        return this.recId;
    }

    public byte[] compact(boolean isCompressed) {
        int curveLen = (Constant.CURVE.getCurve().getFieldSize() + 7) / 8;
        byte[] result = new byte[2 * curveLen + 1];

        int index = 0;
        result[0] = (byte)(27 + recId);

        if (isCompressed) {
            result[0] = (byte) (4 + result[0]);
        }

        index += 1;

        byte[] rBytes = Converter.integerToBytes(r, curveLen);
        byte[] sBytes = Converter.integerToBytes(s, curveLen);

        System.arraycopy(rBytes, 0, result, index, curveLen);
        index += curveLen;
        System.arraycopy(sBytes, 0, result, index, curveLen);

        return result;
    }

    public static ECDSASignature decodeFromDER(byte[] bytes) {
        try {
            ASN1InputStream decoder = new ASN1InputStream(bytes);
            DLSequence seq = (DLSequence) decoder.readObject();
            ASN1Integer r, s;
            try {
                r = (ASN1Integer) seq.getObjectAt(0);
                s = (ASN1Integer) seq.getObjectAt(1);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(e);
            }
            decoder.close();
            // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
            // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
            return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ByteArrayOutputStream derByteStream() throws IOException {
        // Usually 70-72 bytes.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
        DERSequenceGenerator seq = new DERSequenceGenerator(bos);
        seq.addObject(new ASN1Integer(r));
        seq.addObject(new ASN1Integer(s));
        seq.close();
        return bos;
    }

    public static byte[] recoverPubKey(byte[] messageSigned, ECDSASignature signature, int recId) {
        Preconditions.checkArgument(recId >= 0, "recId must be positive");
        Preconditions.checkArgument(signature.r.compareTo(BigInteger.ZERO) >= 0,
                "r must be positive");
        Preconditions.checkArgument(signature.s.compareTo(BigInteger.ZERO) >= 0,
                "s must be positive");
        Preconditions.checkNotNull(messageSigned);
        // 1.0 For j from 0 to h (h == recId here and the loop is outside this
        // function)
        // 1.1 Let x = r + jn

        BigInteger n = Constant.CURVE.getN();
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = signature.r.add(i.multiply(n));
        // 1.2. Convert the integer x to an octet string X of length mlen using
        // the conversion routine
        // specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen =
        // ⌈m/8⌉.
        // 1.3. Convert the octet string (16 set binary digits)||X to an elliptic
        // curve point R using the
        // conversion routine specified in Section 2.3.4. If this conversion
        // routine outputs "invalid", then
        // do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed
        // public key.

        BigInteger prime = Constant.Q;
        // the letter it uses for the prime.
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes
            // place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the
        // y-coord as there are two possibilities.
        // So it's encoded in the recId.

        ECPoint R = decompressKey(x, (recId & 1) == 1);
        // 1.4. If nR != point at infinity, then do another iteration of Step 1
        // (callers responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        // 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature
        // verification.
        BigInteger e = new BigInteger(1, messageSigned);
        // 1.6. For k from 1 to 2 do the following. (loop is outside this function
        // via iterating recId)
        // 1.6.1. Compute a candidate public key as:
        // Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this
        // into the following:
        // Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z +
        // e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking
        // the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 =
        // 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = signature.r.modInverse(n);
        BigInteger srInv = rInv.multiply(signature.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = sumOfTwoMultiplies( Constant.CURVE.getG(), eInvrInv, R, srInv); //  Secp256k1Param.G, eInvrInv, R, srInv);

        return q.getEncoded(true);
    }

    public static ECPoint decompressKey(BigInteger x, boolean firstBit) {
        int size = 1 + getByteLength(Constant.CURVE.getCurve().getFieldSize());
        byte[] dest = Converter.integerToBytes(x, size);
        dest[0] = (byte) (firstBit ? 0x03 : 0x02);
        return Constant.CURVE.getCurve().decodePoint(dest);
    }

    public static ECPoint sumOfTwoMultiplies(ECPoint P, BigInteger k, ECPoint Q, BigInteger l) {
        int m = Math.max(k.bitLength(), l.bitLength());
        ECPoint Z = P.add(Q);
        ECPoint R = P.getCurve().getInfinity();

        for (int i = m - 1; i >= 0; --i) {
            R = R.twice();

            if (k.testBit(i)) {
                if (l.testBit(i)) {
                    R = R.add(Z);
                } else {
                    R = R.add(P);
                }
            } else {
                if (l.testBit(i)) {
                    R = R.add(Q);
                }
            }
        }

        return R;
    }

    public static int getByteLength(int fieldSize) {
        return (fieldSize + 7) / 8;
    }
}
