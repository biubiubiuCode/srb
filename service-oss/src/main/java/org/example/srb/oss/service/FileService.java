package org.example.srb.oss.service;

import java.io.InputStream;

/**
 * @author wendao
 * @since 2024-04-09
 **/
public interface FileService {
    /**
     * 文件上传至阿里云
     */
    String upload(InputStream inputStream, String module, String fileName);
    /**
     * 根据路径删除文件
     * @param url
     */
    void removeFile(String url);
}
