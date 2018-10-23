package com.rst.cgi.common.crypto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author hujia
 */
public class ContractFunction {
    private final static ObjectMapper DEFAULT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Param {
        public Boolean indexed;
        public String name;
        public SolidityType type;

        @JsonGetter("type")
        public String getType() {
            return type.getName();
        }
    }

    public enum FunctionType {
        constructor,
        function,
        event,
        fallback
    }

    public boolean anonymous;
    public boolean constant;
    public boolean payable;
    public String name = "";
    public Param[] inputs = new Param[0];
    public Param[] outputs = new Param[0];
    public FunctionType type;

    public byte[] encode(Object... args) {
        return ByteUtil.merge(encodeSignature(), encodeArguments(args));
    }

    public String encodeHex(Object... args) {
        return "0x" + Converter.byteArrayToHexString(encode(args));
    }

    public byte[] encodeArguments(Object... args) {
        if (args.length > inputs.length) {
            throw new RuntimeException("Too many arguments: " + args.length + " > " + inputs.length);
        }

        int staticSize = 0;
        int dynamicCnt = 0;
        // calculating static size and number of dynamic params
        for (int i = 0; i < args.length; i++) {
            Param param = inputs[i];
            if (param.type.isDynamicType()) {
                dynamicCnt++;
            }
            staticSize += param.type.getFixedSize();
        }

        byte[][] bb = new byte[args.length + dynamicCnt][];

        int curDynamicPtr = staticSize;
        int curDynamicCnt = 0;
        for (int i = 0; i < args.length; i++) {
            if (inputs[i].type.isDynamicType()) {
                byte[] dynBB = inputs[i].type.encode(args[i]);
                bb[i] = SolidityType.IntType.encodeInt(curDynamicPtr);
                bb[args.length + curDynamicCnt] = dynBB;
                curDynamicCnt++;
                curDynamicPtr += dynBB.length;
            } else {
                bb[i] = inputs[i].type.encode(args[i]);
            }
        }
        return ByteUtil.merge(bb);
    }

    private Object[] decode(byte[] encoded, Param[] params) {
        Object[] ret = new Object[params.length];

        int off = 0;
        for (int i = 0; i < params.length; i++) {
            if (params[i].type.isDynamicType()) {
                ret[i] = params[i].type.decode(encoded, SolidityType.IntType.decodeInt(encoded, off).intValue());
            } else {
                ret[i] = params[i].type.decode(encoded, off);
            }
            off += params[i].type.getFixedSize();
        }
        return ret;
    }

    public Object[] decode(byte[] encoded) {
        return decode(ByteUtils.subArray(encoded, 4, encoded.length), inputs);
    }

    public Object[] decodeResult(byte[] encodedRet) {
        return decode(encodedRet, outputs);
    }

    public String formatSignature() {
        StringBuilder paramsTypes = new StringBuilder();
        for (Param param : inputs) {
            paramsTypes.append(param.type.getCanonicalName()).append(",");
        }
        return String.format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
    }

    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }


        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    public byte[] encodeSignatureLong() {
        String signature = formatSignature();
        byte[] sha3Fingerprint = Converter.sha3(signature.getBytes());
        return sha3Fingerprint;
    }

    public byte[] encodeSignature() {
        return Arrays.copyOfRange(encodeSignatureLong(), 0, 4);
    }

    @Override
    public String toString() {
        return formatSignature();
    }

    public static ContractFunction fromJsonInterface(String json) {
        try {
            return DEFAULT_MAPPER.readValue(json, ContractFunction.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ContractFunction fromSignature(String funcName, String... paramTypes) {
        return fromSignature(funcName, paramTypes, new String[0]);
    }

    public static ContractFunction fromSignature(String funcName, String[] paramTypes, String[] resultTypes) {
        ContractFunction ret = new ContractFunction();
        ret.name = funcName;
        ret.constant = false;
        ret.type = FunctionType.function;
        ret.inputs = new Param[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            ret.inputs[i] = new Param();
            ret.inputs[i].name = "param" + i;
            ret.inputs[i].type = SolidityType.getType(paramTypes[i]);
        }
        ret.outputs = new Param[resultTypes.length];
        for (int i = 0; i < resultTypes.length; i++) {
            ret.outputs[i] = new Param();
            ret.outputs[i].name = "res" + i;
            ret.outputs[i].type = SolidityType.getType(resultTypes[i]);
        }
        return ret;
    }
}
