package com.forge.gami.cos.controller;

import com.forge.gami.common.ResponseResult;
import com.forge.gami.cos.service.CosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cos")
public class CosController {

    @Autowired
    private CosService cosService;

    /**
     * 创建临时文件并获取上传签名
     */
    @PostMapping("/create-temp-file")
    public ResponseEntity<?> createTempFileAndGetSign(@RequestBody Map<String, Object> data) {
        try {
            String fileName = (String) data.get("fileName");
            String fileType = (String) data.get("fileType");
            Long fileSize = data.get("fileSize") instanceof Number ?
                    ((Number) data.get("fileSize")).longValue() : 0;

            Map<String, String> signature = cosService.generateUploadSignature(fileName, fileType, fileSize);

            return ResponseEntity.ok(ResponseResult.success("获取上传签名成功", signature));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "获取上传签名失败: " + e.getMessage()));
        }
    }

    /**
     * 提交文件表单并将临时文件转为正式文件
     */
    @PostMapping("/submit-form")
    public ResponseEntity<?> submitFileForm(@RequestBody Map<String, Object> data) {
        try {
            // 这里可以根据实际业务需求处理文件表单数据
            // 示例中简化处理，直接返回成功

            return ResponseEntity.ok(ResponseResult.success("提交文件表单成功", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "提交文件表单失败: " + e.getMessage()));
        }
    }

    /**
     * 从腾讯COS下载文件
     */
    @GetMapping("/download/{fileId}")
    public void downloadFileFromCOS(@PathVariable String fileId, HttpServletResponse response) {
        try {
            cosService.downloadFile(fileId, response);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载文件失败: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/files")
    public ResponseEntity<?> getCOSFileList(@RequestParam(required = false) String prefix,
                                          @RequestParam(defaultValue = "100") int maxKeys) {
        try {
            List<Map<String, Object>> fileList = cosService.listFiles(prefix, maxKeys);

            Map<String, Object> result = new HashMap<>();
            result.put("list", fileList);
            result.put("total", fileList.size());

            return ResponseEntity.ok(ResponseResult.success("获取文件列表成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "获取文件列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取单个文件信息
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<?> getCOSFileInfo(@PathVariable String fileId) {
        try {
            Map<String, Object> fileInfo = cosService.getFileInfo(fileId);

            if (fileInfo != null) {
                return ResponseEntity.ok(ResponseResult.success("获取文件信息成功", fileInfo));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseResult.error(404, "文件不存在"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "获取文件信息失败: " + e.getMessage()));
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<?> deleteCOSFile(@PathVariable String fileId) {
        try {
            boolean success = cosService.deleteFile(fileId);

            if (success) {
                return ResponseEntity.ok(ResponseResult.success("删除文件成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseResult.error(400, "删除文件失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "删除文件失败: " + e.getMessage()));
        }
    }

    /**
     * 更新文件信息
     */
    @PutMapping("/file/{fileId}")
    public ResponseEntity<?> updateCOSFile(@PathVariable String fileId, @RequestBody Map<String, Object> data) {
        try {
            // 这里可以根据实际业务需求更新文件信息
            // 示例中简化处理，直接返回成功

            return ResponseEntity.ok(ResponseResult.success("更新文件信息成功", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "更新文件信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取文件预览URL
     */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<?> getCOSFilePreviewUrl(@PathVariable String fileId) {
        try {
            String previewUrl = cosService.getFilePreviewUrl(fileId);

            return ResponseEntity.ok(ResponseResult.success("获取文件预览URL成功", previewUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "获取文件预览URL失败: " + e.getMessage()));
        }
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/create-multipart-upload")
    public ResponseEntity<?> createMultipartUpload(@RequestBody Map<String, Object> data) {
        try {
            String fileName = (String) data.get("fileName");
            // fileSize 参数在这个接口中可以不直接使用，但保留以便将来扩展

            Map<String, String> uploadInfo = cosService.initMultipartUpload(fileName);

            return ResponseEntity.ok(ResponseResult.success("初始化分片上传成功", uploadInfo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "初始化分片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 为分片生成签名URL
     */
    @PostMapping("/sign-part")
    public ResponseEntity<?> signPart(@RequestBody Map<String, Object> data) {
        try {
            String key = (String) data.get("key");
            String uploadId = (String) data.get("uploadId");
            Integer partNumber = data.get("partNumber") instanceof Number ?
                    ((Number) data.get("partNumber")).intValue() : 1;

            String signedUrl = cosService.generatePartUploadUrl(key, uploadId, partNumber);

            Map<String, String> result = new HashMap<>();
            result.put("url", signedUrl);

            return ResponseEntity.ok(ResponseResult.success("生成分片上传URL成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "生成分片上传URL失败: " + e.getMessage()));
        }
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/complete-multipart-upload")
    public ResponseEntity<?> completeMultipartUpload(@RequestBody Map<String, Object> data) {
        try {
            String key = (String) data.get("key");
            String uploadId = (String) data.get("uploadId");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) data.get("parts");

            Map<String, Object> result = cosService.completeMultipartUpload(key, uploadId, parts);

            return ResponseEntity.ok(ResponseResult.success("完成分片上传成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "完成分片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 取消分片上传
     */
    @PostMapping("/abort-multipart-upload")
    public ResponseEntity<?> abortMultipartUpload(@RequestBody Map<String, Object> data) {
        try {
            String key = (String) data.get("key");
            String uploadId = (String) data.get("uploadId");

            cosService.abortMultipartUpload(key, uploadId);

            return ResponseEntity.ok(ResponseResult.success("取消分片上传成功", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseResult.error(500, "取消分片上传失败: " + e.getMessage()));
        }
    }
}
