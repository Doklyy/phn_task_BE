package vn.phn.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Upload và tải file đính kèm (báo cáo, hoàn thành nhiệm vụ).
 * POST /api/upload → trả về { "path": "..." }
 * GET /api/upload/file?path=... → trả về nội dung file.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class UploadController {

    private static final String UPLOAD_DIR = "uploads";

    private static Path getUploadBasePath() {
        Path base = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
        try {
            if (!Files.exists(base)) {
                Files.createDirectories(base);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không tạo được thư mục upload", e);
        }
        return base;
    }

    /**
     * POST /api/upload
     * Part name: "file"
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chưa chọn file."));
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        }
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot);
        }
        String safeName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path base = getUploadBasePath();
        Path target = base.resolve(safeName);
        try {
            Files.write(target, file.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lưu file thất bại: " + e.getMessage()));
        }
        String relativePath = UPLOAD_DIR + "/" + safeName;
        return ResponseEntity.ok(Map.of("path", relativePath, "filePath", relativePath));
    }

    /**
     * GET /api/upload/file?path=...
     * Trả về file để xem/tải. path là relative (vd: uploads/abc123.pdf).
     */
    @GetMapping("/upload/file")
    public ResponseEntity<byte[]> getFile(@RequestParam("path") String pathParam) {
        if (pathParam == null || pathParam.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String safe = pathParam.replace("..", "").replace("\\", "/").trim();
        if (safe.startsWith("/")) safe = safe.substring(1);
        Path base = getUploadBasePath();
        Path resolved = base.resolve(Paths.get(safe).getFileName().toString()).normalize();
        if (!resolved.startsWith(base)) {
            return ResponseEntity.status(403).build();
        }
        if (!Files.isRegularFile(resolved)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = Files.readAllBytes(resolved);
            String name = resolved.getFileName().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", name);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
