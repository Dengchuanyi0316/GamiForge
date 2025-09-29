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
        System.out.println("来了老弟================");
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
}
