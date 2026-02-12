package vn.phn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Xử lý request tới root (/) hoặc (/. ) để tránh NoResourceFoundException
 * khi Render health check hoặc user mở URL gốc backend.
 */
@RestController
public class RootController {

    @GetMapping(value = {"/", "/."})
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "api", "/api",
                "health", "/api/health"
        ));
    }
}
