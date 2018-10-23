package com.rst.cgi.common.EOS;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Sign{
    public static final Secp256k secp = new Secp256k();
    public static byte[] transReal(TxSign sign){
        // tx
        com.rst.cgi.common.utils.ByteBuffer bf = new com.rst.cgi.common.utils.ByteBuffer();
        ObjectUtils.writeBytes(sign, bf);
        byte[] real = bf.getBuffer();
        // append
        real = ByteUtils.concat(real, ByteBuffer.allocate(33).array());
        return real;
    }
    public static String parseTransferData(String from, String to, String quantity, String memo) {
        DataParam[] datas = new DataParam[] { new DataParam(from, DataType.name, Action.transfer),
                new DataParam(to, DataType.name, Action.transfer),
                new DataParam(quantity, DataType.asset, Action.transfer),
                new DataParam(memo, DataType.string, Action.transfer), };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            allbyte = ByteUtils.concat(allbyte, value.seria());
        }
        return Hex.bytesToHexString(allbyte);
    }
    public static String parseBuyRamData(String payer, String receiver, Long bytes) {

        DataParam[] datas = new DataParam[] { new DataParam(payer, DataType.name, Action.ram),
                new DataParam(receiver, DataType.name, Action.ram),
                new DataParam(String.valueOf(bytes), DataType.unit32, Action.ram)

        };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            allbyte = ByteUtils.concat(allbyte, value.seria());
        }
        return Hex.bytesToHexString(allbyte);
    }
    public static String parseAccountData(String creator, String name, String onwer, String active) {

        DataParam[] datas = new DataParam[] {
                // creator
                new DataParam(creator, DataType.name, Action.account),
                // name
                new DataParam(name, DataType.name, Action.account),
                // owner
                new DataParam(onwer, DataType.key, Action.account),
                // active
                new DataParam(active, DataType.key, Action.account),

        };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            allbyte = ByteUtils.concat(allbyte, value.seria());
        }
        return Hex.bytesToHexString(allbyte);
    }
    public static String parseDelegateData(String from, String receiver, String stakeNetQuantity,
                                           String stakeCpuQuantity, int transfer) {

        DataParam[] datas = new DataParam[] { new DataParam(from, DataType.name, Action.delegate),
                new DataParam(receiver, DataType.name, Action.delegate),
                new DataParam(stakeNetQuantity, DataType.asset, Action.delegate),
                new DataParam(stakeCpuQuantity, DataType.asset, Action.delegate),
                new DataParam(String.valueOf(transfer), DataType.varint32, Action.delegate)

        };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            allbyte = ByteUtils.concat(allbyte, value.seria());
        }
        return Hex.bytesToHexString(allbyte);
    }

    private static BigInteger privateKey(String pk) {
        byte[] private_wif = Base58.decode(pk);
        byte version = (byte) 0x80;
        if (private_wif[0] != version) {
            throw new EException("version_error", "Expected version " + 0x80 + ", instead got " + version);
        }
        byte[] private_key = ByteUtils.copy(private_wif, 0, private_wif.length - 4);
        byte[] new_checksum = Sha.SHA256(private_key);
        new_checksum = Sha.SHA256(new_checksum);
        new_checksum = ByteUtils.copy(new_checksum, 0, 4);
        byte[] last_private_key = ByteUtils.copy(private_key, 1, private_key.length - 1);
        BigInteger d = new BigInteger(Hex.bytesToHexString(last_private_key), 16);
        return d;
    }
    public static String signHash(String pk, byte[] b) {
        String dataSha256 = Hex.bytesToHexString(Sha.SHA256(b));
        BigInteger e = new BigInteger(dataSha256, 16);
        int nonce = 0;
        int i = 0;
        BigInteger d = privateKey(pk);
        Point Q = secp.G().multiply(d);
        nonce = 0;
        Ecdsa ecd = new Ecdsa(secp);
        Ecdsa.SignBigInt sign;
        while (true) {
            sign = ecd.sign(dataSha256, d, nonce++);
            byte der[] = sign.getDer();
            byte lenR = der[3];
            byte lenS = der[5 + lenR];
            if (lenR == 32 && lenS == 32) {
                i = ecd.calcPubKeyRecoveryParam(e, sign, Q);
                i += 4; // compressed
                i += 27; // compact // 24 or 27 :( forcing odd-y 2nd key candidate)
                break;
            }
        }
        byte[] pub_buf = new byte[65];
        pub_buf[0] = (byte) i;
        ByteUtils.copy(sign.getR().toByteArray(), 0, pub_buf, 1, sign.getR().toByteArray().length);
        ByteUtils.copy(sign.getS().toByteArray(), 0, pub_buf, sign.getR().toByteArray().length + 1,
                sign.getS().toByteArray().length);

        byte[] checksum = Ripemd160.from(ByteUtils.concat(pub_buf, "K1".getBytes())).bytes();

        byte[] signatureString = ByteUtils.concat(pub_buf, ByteUtils.copy(checksum, 0, 4));

        return "SIG_K1_" + Base58.encode(signatureString);
    }

}

