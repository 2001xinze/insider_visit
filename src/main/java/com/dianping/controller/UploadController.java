package com.dianping.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.dianping.pojo.Result;
import com.dianping.constant.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    /**
     *
     * @param image
     * @return
     */
    @PostMapping("blog")
    public Result uploadImage (@RequestParam("file") MultipartFile image) {
        try {
            // get the original file name
            String originalFilename = image.getOriginalFilename();
            // generate new file name
            String fileName = createNewFileName(originalFilename);
            // save the file
            image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            // return the result
            log.info("successfully upload the file, {}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("unsuccessfully upload the file", e);
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    @GetMapping("/blog/delete")
    public Result deleteBlogImg (@RequestParam("name") String fileName) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName);
        if (file.isDirectory()) {
            return Result.fail("wrong file name");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    /**
     *
     * @param originalFilename
     * @return
     */
    private String createNewFileName(String originalFilename) {
        // get the suffix
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // generate directory
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // check if the directory exists
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // generate the file name
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
