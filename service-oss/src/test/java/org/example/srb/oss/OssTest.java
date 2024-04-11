package org.example.srb.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import org.junit.jupiter.api.Test;

/**
 * @author wendao
 * @since 2024-04-09
 **/
public class OssTest {
    // Endpoint以杭州为例，其它Region请按实际情况填写。
    String endpoint = "your endpoint";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
    String accessKeyId = "your accessKeyId";
    String accessKeySecret = "your accessKeySecret";
    String bucketName = "srb-file";
    /**
     * 创建Bucket
     * @param 
     * @return void
     * @author Administrator
     * @date 2024/4/9 0009 0:55       
    */
    @Test
    public void testCreateBucket() {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 创建存储空间。
        ossClient.createBucket(bucketName);

        // 关闭OSSClient。
        ossClient.shutdown();
    }
    /**
     * 判断bucket是否存在
     * @param 
     * @return void
     * @author Administrator
     * @date 2024/4/9 0009 0:55       
    */

    @Test
    public void testExist() {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        boolean exists = ossClient.doesBucketExist(bucketName);
        System.out.println(exists);

        // 关闭OSSClient。
        ossClient.shutdown();
    }
    /**
     * 设置bucket访问权限
     * @param
     * @return void
     * @author Administrator
     * @date 2024/4/9 0009 0:55
    */
    @Test
    public void testAccessControl() {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 设置存储空间的访问权限为：公共读。
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);

        // 关闭OSSClient。
        ossClient.shutdown();
    }
}
