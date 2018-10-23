package com.rst.cgi.common.utils;

import java.io.*;
import java.util.Objects;

/**
 * Created by jiaoweiwei on 17-6-12.
 */
public class StreamUtils {
    /**
     * 读取源文件内容
     *
     * @param filename String 文件路径
     * @return byte[] 文件内容
     * @throws IOException
     */
    public static byte[] readFile(String filename) throws IOException {

        File file = new File(filename);
        if (filename == null || filename.equals("")) {
            throw new NullPointerException("无效的文件路径");
        }
        long len = file.length();
        byte[] bytes = new byte[(int) len];

        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream

                        (file));
        int r = bufferedInputStream.read(bytes);
        if (r != len)
            throw new IOException("读取文件不正确");
        bufferedInputStream.close();
        return bytes;

    }

//
//    public static byte[] readFile(in) throws IOException{
//
//    }



    /** */
    /**
     * 将数据写入文件
     *
     * @param data byte[]
     * @throws IOException
     */
    public static void writeFile(byte[] data, String filename) throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        bufferedOutputStream.write(data);
        bufferedOutputStream.close();

    }


    /**
     * 将输入流转换成输出流
     * @param inStream
     * @return
     * @throws Exception
     */
     public static OutputStream inStreamToOutStream(InputStream inStream) throws Exception{
         if(Objects.isNull(inStream)){
             throw new  IOException("文件不存在");
         }
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         byte[] buffer = new byte[1024];
         int len = 0;
         while( (len=inStream.read(buffer)) != -1 ){
             outStream.write(buffer, 0, len);
         }
         inStream.close();
         return outStream;
     }

    /**
     * 关闭流工具方法
     * @author hxl
     * 2018/5/18 下午4:44
     */
     public static void close(Closeable... cloneables) {
         for (Closeable c : cloneables) {
             if (c != null) {
                 try {
                     c.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
}
