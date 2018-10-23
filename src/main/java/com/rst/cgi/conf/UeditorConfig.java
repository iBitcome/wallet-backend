package com.rst.cgi.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 前端的ueditor插件配置
 * @author huangxiaolin
 * @date 2018-04-13 上午11:06
 */
@Component
public class UeditorConfig {

    @Value("${ueditor.uploadFileMaxSize:20480}")
    private int uploadFileMaxSize;

    private final static String UPLOAD_UEDITOR_PATH = "/upload/ueditor";
    //允许上传图片的类型
    private final static String[] UPLOAD_IMAGE_TYPE = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};
    //前端上传文件表单input标签的字段名称
    private final static String UPLOAD_FIELD_NAME = "file";

    private String imageActionName = "uploadimage";
    private String imageFieldName = UPLOAD_FIELD_NAME;
    private int imageMaxSize = uploadFileMaxSize; /* 上传大小限制，单位B */
    private String[] imageAllowFiles = UPLOAD_IMAGE_TYPE; /* 上传图片格式显示 */
    private boolean imageCompressEnable = true; /* 是否压缩图片,默认是true */
    private int imageCompressBorder =1600; /* 图片压缩最长边限制 */
    private String imageInsertAlign = "none"; /* 插入的图片浮动方式 */
    //这里不设置图片访问前缀
    private String imageUrlPrefix = ""; /* 图片访问路径前缀 */
    private String imagePathFormat = UPLOAD_UEDITOR_PATH+"/{time}{rand:6}";
    
    
    /* 涂鸦图片上传配置项 */
    private String scrawlActionName = "uploadscrawl"; /* 执行上传涂鸦的action名称 */
    private String scrawlFieldName = UPLOAD_FIELD_NAME; /* 提交的图片表单名称 */
    private String scrawlPathFormat = UPLOAD_UEDITOR_PATH+"/{yyyy}{mm}{dd}/{time}{rand:6}"; /* 上传保存路径;可以自定义保存路径和文件名格式 */
    private int scrawlMaxSize = uploadFileMaxSize; /* 上传大小限制，单位B */
    private String scrawlUrlPrefix = ""; /* 图片访问路径前缀 */
    private String scrawlInsertAlign = "none";

    /* 截图工具上传 */
    private String snapscreenActionName = "uploadimage"; /* 执行上传截图的action名称 */
    private String snapscreenPathFormat = UPLOAD_UEDITOR_PATH+"/{yyyy}{mm}{dd}/{time}{rand:6}"; /* 上传保存路径;可以自定义保存路径和文件名格式 */
    private String snapscreenUrlPrefix = ""; /* 图片访问路径前缀 */
    private String snapscreenInsertAlign = "none"; /* 插入的图片浮动方式 */

    /* 抓取远程图片配置 */
    private String[] catcherLocalDomain = {"127.0.0.1", "localhost", "img.baidu.com"};
    private String catcherActionName = "catchimage"; /* 执行抓取远程图片的action名称 */
    private String catcherFieldName = "source"; /* 提交的图片列表表单名称 */
    private String catcherPathFormat = UPLOAD_UEDITOR_PATH+"/{yyyy}{mm}{dd}/{time}{rand:6}"; /* 上传保存路径;可以自定义保存路径和文件名格式 */
    private String catcherUrlPrefix = ""; /* 图片访问路径前缀 */
    private int catcherMaxSize = uploadFileMaxSize; /* 上传大小限制，单位B */
    private String[] catcherAllowFiles = UPLOAD_IMAGE_TYPE; /* 抓取图片格式显示 */

    /* 上传视频配置 */
    private String videoActionName = "uploadvideo"; /* 执行上传视频的action名称 */
    private String videoFieldName = UPLOAD_FIELD_NAME; /* 提交的视频表单名称 */
    private String videoPathFormat = UPLOAD_UEDITOR_PATH + "/{yyyy}{mm}{dd}/{time}{rand:6}"; /* 上传保存路径;可以自定义保存路径和文件名格式 */
    private String videoUrlPrefix = ""; /* 视频访问路径前缀 */
    private int videoMaxSize = uploadFileMaxSize; /* 上传大小限制，单位B，默认100MB */
    private String[] videoAllowFiles = {".flv"}; /* 上传视频格式显示 */

    /* 上传文件配置 */
    private String fileActionName = "uploadfile"; /* controller里;执行上传视频的action名称 */
    private String fileFieldName = UPLOAD_FIELD_NAME; /* 提交的文件表单名称 */
    private String filePathFormat = UPLOAD_UEDITOR_PATH+"/{yyyy}{mm}{dd}/{time}{rand:6}"; /* 上传保存路径;可以自定义保存路径和文件名格式 */
    private String fileUrlPrefix = ""; /* 文件访问路径前缀 */
    private int fileMaxSize = uploadFileMaxSize; /* 上传大小限制，单位B，默认50MB */
    private String[] fileAllowFiles = {
            ".png", ".jpg", ".jpeg", ".gif", ".bmp",
            ".doc",".docx",".xls",".xlsx",".ppt",".pptx",".pdf",".txt"
    }; /* 上传文件格式显示 */

    /* 列出指定目录下的图片 */
    private String imageManagerActionName = "listimage"; /* 执行图片管理的action名称 */
    private String imageManagerListPath = UPLOAD_UEDITOR_PATH+"/image/"; /* 指定要列出图片的目录 */
    private int imageManagerListSize = 2; /* 每次列出文件数量 */
    private String imageManagerUrlPrefix = ""; /* 图片访问路径前缀 */
    private String imageManagerInsertAlign = "none"; /* 插入的图片浮动方式 */
    private String[] imageManagerAllowFiles = UPLOAD_IMAGE_TYPE; /* 列出的文件类型 */

    /* 列出指定目录下的文件 */
    private String fileManagerActionName = "listfile"; /* 执行文件管理的action名称 */
    private String fileManagerListPath = UPLOAD_UEDITOR_PATH+"/file/"; /* 指定要列出文件的目录 */
    private String fileManagerUrlPrefix = ""; /* 文件访问路径前缀 */
    private int fileManagerListSize = 2; /* 每次列出文件数量 */
    private String[] fileManagerAllowFiles = {
            ".png", ".jpg", ".jpeg", ".gif", ".bmp",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".pdf", ".txt", ".md", ".xml"
    }; /* 列出的文件类型 */


    public String getImageActionName() {
        return imageActionName;
    }

    public void setImageActionName(String imageActionName) {
        this.imageActionName = imageActionName;
    }

    public String getImageFieldName() {
        return imageFieldName;
    }

    public void setImageFieldName(String imageFieldName) {
        this.imageFieldName = imageFieldName;
    }

    public int getImageMaxSize() {
        return imageMaxSize;
    }

    public void setImageMaxSize(int imageMaxSize) {
        this.imageMaxSize = imageMaxSize;
    }

    public String[] getImageAllowFiles() {
        return imageAllowFiles;
    }

    public void setImageAllowFiles(String[] imageAllowFiles) {
        this.imageAllowFiles = imageAllowFiles;
    }

    public boolean isImageCompressEnable() {
        return imageCompressEnable;
    }

    public void setImageCompressEnable(boolean imageCompressEnable) {
        this.imageCompressEnable = imageCompressEnable;
    }

    public int getImageCompressBorder() {
        return imageCompressBorder;
    }

    public void setImageCompressBorder(int imageCompressBorder) {
        this.imageCompressBorder = imageCompressBorder;
    }

    public String getImageInsertAlign() {
        return imageInsertAlign;
    }

    public void setImageInsertAlign(String imageInsertAlign) {
        this.imageInsertAlign = imageInsertAlign;
    }

    public String getImageUrlPrefix() {
        return imageUrlPrefix;
    }

    public void setImageUrlPrefix(String imageUrlPrefix) {
        this.imageUrlPrefix = imageUrlPrefix;
    }

    public String getImagePathFormat() {
        return imagePathFormat;
    }

    public void setImagePathFormat(String imagePathFormat) {
        this.imagePathFormat = imagePathFormat;
    }

    public String getScrawlActionName() {
        return scrawlActionName;
    }

    public void setScrawlActionName(String scrawlActionName) {
        this.scrawlActionName = scrawlActionName;
    }

    public String getScrawlFieldName() {
        return scrawlFieldName;
    }

    public void setScrawlFieldName(String scrawlFieldName) {
        this.scrawlFieldName = scrawlFieldName;
    }

    public String getScrawlPathFormat() {
        return scrawlPathFormat;
    }

    public void setScrawlPathFormat(String scrawlPathFormat) {
        this.scrawlPathFormat = scrawlPathFormat;
    }

    public int getScrawlMaxSize() {
        return scrawlMaxSize;
    }

    public void setScrawlMaxSize(int scrawlMaxSize) {
        this.scrawlMaxSize = scrawlMaxSize;
    }

    public String getScrawlUrlPrefix() {
        return scrawlUrlPrefix;
    }

    public void setScrawlUrlPrefix(String scrawlUrlPrefix) {
        this.scrawlUrlPrefix = scrawlUrlPrefix;
    }

    public String getScrawlInsertAlign() {
        return scrawlInsertAlign;
    }

    public void setScrawlInsertAlign(String scrawlInsertAlign) {
        this.scrawlInsertAlign = scrawlInsertAlign;
    }

    public String getSnapscreenActionName() {
        return snapscreenActionName;
    }

    public void setSnapscreenActionName(String snapscreenActionName) {
        this.snapscreenActionName = snapscreenActionName;
    }

    public String getSnapscreenPathFormat() {
        return snapscreenPathFormat;
    }

    public void setSnapscreenPathFormat(String snapscreenPathFormat) {
        this.snapscreenPathFormat = snapscreenPathFormat;
    }

    public String getSnapscreenUrlPrefix() {
        return snapscreenUrlPrefix;
    }

    public void setSnapscreenUrlPrefix(String snapscreenUrlPrefix) {
        this.snapscreenUrlPrefix = snapscreenUrlPrefix;
    }

    public String getSnapscreenInsertAlign() {
        return snapscreenInsertAlign;
    }

    public void setSnapscreenInsertAlign(String snapscreenInsertAlign) {
        this.snapscreenInsertAlign = snapscreenInsertAlign;
    }

    public String[] getCatcherLocalDomain() {
        return catcherLocalDomain;
    }

    public void setCatcherLocalDomain(String[] catcherLocalDomain) {
        this.catcherLocalDomain = catcherLocalDomain;
    }

    public String getCatcherActionName() {
        return catcherActionName;
    }

    public void setCatcherActionName(String catcherActionName) {
        this.catcherActionName = catcherActionName;
    }

    public String getCatcherFieldName() {
        return catcherFieldName;
    }

    public void setCatcherFieldName(String catcherFieldName) {
        this.catcherFieldName = catcherFieldName;
    }

    public String getCatcherPathFormat() {
        return catcherPathFormat;
    }

    public void setCatcherPathFormat(String catcherPathFormat) {
        this.catcherPathFormat = catcherPathFormat;
    }

    public String getCatcherUrlPrefix() {
        return catcherUrlPrefix;
    }

    public void setCatcherUrlPrefix(String catcherUrlPrefix) {
        this.catcherUrlPrefix = catcherUrlPrefix;
    }

    public int getCatcherMaxSize() {
        return catcherMaxSize;
    }

    public void setCatcherMaxSize(int catcherMaxSize) {
        this.catcherMaxSize = catcherMaxSize;
    }

    public String[] getCatcherAllowFiles() {
        return catcherAllowFiles;
    }

    public void setCatcherAllowFiles(String[] catcherAllowFiles) {
        this.catcherAllowFiles = catcherAllowFiles;
    }

    public String getVideoActionName() {
        return videoActionName;
    }

    public void setVideoActionName(String videoActionName) {
        this.videoActionName = videoActionName;
    }

    public String getVideoFieldName() {
        return videoFieldName;
    }

    public void setVideoFieldName(String videoFieldName) {
        this.videoFieldName = videoFieldName;
    }

    public String getVideoPathFormat() {
        return videoPathFormat;
    }

    public void setVideoPathFormat(String videoPathFormat) {
        this.videoPathFormat = videoPathFormat;
    }

    public String getVideoUrlPrefix() {
        return videoUrlPrefix;
    }

    public void setVideoUrlPrefix(String videoUrlPrefix) {
        this.videoUrlPrefix = videoUrlPrefix;
    }

    public int getVideoMaxSize() {
        return videoMaxSize;
    }

    public void setVideoMaxSize(int videoMaxSize) {
        this.videoMaxSize = videoMaxSize;
    }

    public String[] getVideoAllowFiles() {
        return videoAllowFiles;
    }

    public void setVideoAllowFiles(String[] videoAllowFiles) {
        this.videoAllowFiles = videoAllowFiles;
    }

    public String getFileActionName() {
        return fileActionName;
    }

    public void setFileActionName(String fileActionName) {
        this.fileActionName = fileActionName;
    }

    public String getFileFieldName() {
        return fileFieldName;
    }

    public void setFileFieldName(String fileFieldName) {
        this.fileFieldName = fileFieldName;
    }

    public String getFilePathFormat() {
        return filePathFormat;
    }

    public void setFilePathFormat(String filePathFormat) {
        this.filePathFormat = filePathFormat;
    }

    public String getFileUrlPrefix() {
        return fileUrlPrefix;
    }

    public void setFileUrlPrefix(String fileUrlPrefix) {
        this.fileUrlPrefix = fileUrlPrefix;
    }

    public int getFileMaxSize() {
        return fileMaxSize;
    }

    public void setFileMaxSize(int fileMaxSize) {
        this.fileMaxSize = fileMaxSize;
    }

    public String[] getFileAllowFiles() {
        return fileAllowFiles;
    }

    public void setFileAllowFiles(String[] fileAllowFiles) {
        this.fileAllowFiles = fileAllowFiles;
    }

    public String getImageManagerActionName() {
        return imageManagerActionName;
    }

    public void setImageManagerActionName(String imageManagerActionName) {
        this.imageManagerActionName = imageManagerActionName;
    }

    public String getImageManagerListPath() {
        return imageManagerListPath;
    }

    public void setImageManagerListPath(String imageManagerListPath) {
        this.imageManagerListPath = imageManagerListPath;
    }

    public int getImageManagerListSize() {
        return imageManagerListSize;
    }

    public void setImageManagerListSize(int imageManagerListSize) {
        this.imageManagerListSize = imageManagerListSize;
    }

    public String getImageManagerUrlPrefix() {
        return imageManagerUrlPrefix;
    }

    public void setImageManagerUrlPrefix(String imageManagerUrlPrefix) {
        this.imageManagerUrlPrefix = imageManagerUrlPrefix;
    }

    public String getImageManagerInsertAlign() {
        return imageManagerInsertAlign;
    }

    public void setImageManagerInsertAlign(String imageManagerInsertAlign) {
        this.imageManagerInsertAlign = imageManagerInsertAlign;
    }

    public String[] getImageManagerAllowFiles() {
        return imageManagerAllowFiles;
    }

    public void setImageManagerAllowFiles(String[] imageManagerAllowFiles) {
        this.imageManagerAllowFiles = imageManagerAllowFiles;
    }

    public String getFileManagerActionName() {
        return fileManagerActionName;
    }

    public void setFileManagerActionName(String fileManagerActionName) {
        this.fileManagerActionName = fileManagerActionName;
    }

    public String getFileManagerListPath() {
        return fileManagerListPath;
    }

    public void setFileManagerListPath(String fileManagerListPath) {
        this.fileManagerListPath = fileManagerListPath;
    }

    public String getFileManagerUrlPrefix() {
        return fileManagerUrlPrefix;
    }

    public void setFileManagerUrlPrefix(String fileManagerUrlPrefix) {
        this.fileManagerUrlPrefix = fileManagerUrlPrefix;
    }

    public int getFileManagerListSize() {
        return fileManagerListSize;
    }

    public void setFileManagerListSize(int fileManagerListSize) {
        this.fileManagerListSize = fileManagerListSize;
    }

    public String[] getFileManagerAllowFiles() {
        return fileManagerAllowFiles;
    }

    public void setFileManagerAllowFiles(String[] fileManagerAllowFiles) {
        this.fileManagerAllowFiles = fileManagerAllowFiles;
    }
}
