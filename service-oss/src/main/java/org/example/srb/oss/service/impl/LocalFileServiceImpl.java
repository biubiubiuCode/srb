package org.example.srb.oss.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResponseEnum;
import org.example.srb.oss.service.FileService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 用本地存储代替阿里云Oss, 临时实现功能，仅供学习测试, 返回文件路径，然后前端直接超链接
 * @author wendao
 * @since 2024-04-09
 **/
@Service
@Slf4j
public class LocalFileServiceImpl implements FileService {

    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        FileOutputStream fos =null;
        try {
            //构建日期路径：avatar/2019/02/26/文件名
            String folder = new DateTime().toString("yyyy/MM/dd");

            //文件名：uuid.扩展名
            fileName = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));
            //本地存储文件:根路径+文件名
            String path ="./srb_photo/"+ module + "/" + folder + "/";

            String newFileName =path+ fileName;

            File file = new File(newFileName);

            File dir = file.getParentFile();
            if (!dir.exists()) {
                // 创建文件夹
                dir.mkdirs();
            }
            // 创建文件
            file.createNewFile();

            fos = new FileOutputStream(file);
            //将文件存储在服务器的磁盘目录
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            log.info("将文件 "+fileName+"保存到了 "+newFileName);
            //文件
            return newFileName;
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeFile(String url) {
        // 创建一个代表要删除文件的File对象
        File file = new File(url);

        // 调用File对象的delete方法来删除文件
        if (file.delete()) {
            log.info("成功删除文件"+url);
        }
    }
}
