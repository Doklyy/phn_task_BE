package vn.phn.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir).toAbsolutePath().normalize());
        } catch (Exception ignored) { }
    }

    /**
     * Upload file (báo cáo hoàn thành, báo cáo hàng ngày).
     * POST /api/upload → multipart/form-data, file field name: "file"
     * Trả về { "path": "uploads/xxx.ext" } để FE gửi kèm khi submit báo cáo.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) originalName = "file";
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) ext = originalName.substring(dot);
            String name = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
            Path target = dir.resolve(name);
            file.transferTo(target.toFile());
            String path = uploadDir + "/" + name;
            Map<String, String> body = new HashMap<>();
            body.put("path", path);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Tải file lên thất bại: " + (e.getMessage() != null ? e.getMessage() : "lỗi hệ thống"));
            return ResponseEntity.status(500).body(err);
        }
    }

    /**
     * Tải file đính kèm (Admin/Leader xem file nhân viên đã gửi).
     * GET /api/upload/files/{path} với path = "uploads/xxx.ext" (đúng như trả về từ POST /upload).
     */
    @GetMapping("/upload/files/{*path}")
    public ResponseEntity<Resource> getFile(@PathVariable("path") String pathSegment) {
        if (pathSegment == null || pathSegment.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        String path = pathSegment.startsWith("/") ? pathSegment.substring(1) : pathSegment;
        if (path.contains("..")) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path file = base.getParent().resolve(path).normalize();
            if (!file.startsWith(base)) {
                return ResponseEntity.notFound().build();
            }
            if (!Files.isRegularFile(file)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new InputStreamResource(Files.newInputStream(file));
            String name = file.getFileName().toString();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
