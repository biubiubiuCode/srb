package org.example.srb.oss.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.common.exception.BusinessException;
import org.example.common.result.R;
import org.example.common.result.ResponseEnum;
import org.example.srb.oss.service.FileService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wendao
 * @since 2024-04-09
 **/
@Api(tags = "阿里云文件管理")
//@CrossOrigin //跨域
@RestController
@RequestMapping("/api/oss/file")
public class FileController {
    @Resource
    private FileService fileService;
    /**
     * 文件上传
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public R upload(
            @ApiParam(value = "文件", required = true)
            @RequestParam("file") MultipartFile file,

            @ApiParam(value = "模块", required = true)
            @RequestParam("module") String module)  {

        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String uploadUrl = fileService.upload(inputStream, module, originalFilename);

            //返回r对象
            return R.ok().message("文件上传成功").data("url", uploadUrl);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("删除OSS文件")
    @DeleteMapping("/remove")
    public R remove(
            @ApiParam(value = "要删除的文件路径", required = true)
            @RequestParam("url") String url) {
        fileService.removeFile(url);
        return R.ok().message("删除成功");
    }
}
