package com.rst.cgi.conf.security;

/**
 * @author hujia
 */
public class CurrentThreadData {
    @lombok.Data
    private static class Data {
        private String iBitID;
        private Integer language;
        private Integer clientVersion;
        private String clientPlatform;
    }

    private static ThreadLocal<Data> THREAD_LOCAL = ThreadLocal.withInitial(() -> new Data());

    public static String iBitID() {
        return THREAD_LOCAL.get().getIBitID();
    }

    public static Integer clientVersion() { return THREAD_LOCAL.get().getClientVersion(); }

    public static String clientPlatform() { return THREAD_LOCAL.get().getClientPlatform(); }

    public static Integer language() {
        return THREAD_LOCAL.get().getLanguage();
    }

    public static void setIBitID(String iBitID) {
        THREAD_LOCAL.get().setIBitID(iBitID);
    }

    public static void setLanguage(Integer language) {
        THREAD_LOCAL.get().setLanguage(language);
    }

    public static void setClientVersion(Integer clientVersion) {
        THREAD_LOCAL.get().setClientVersion(clientVersion);
    }

    public static void setClientPlatform(String clientPlatform) {
        THREAD_LOCAL.get().setClientPlatform(clientPlatform);
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
