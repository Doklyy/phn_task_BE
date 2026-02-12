package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.ScoringDto;
import vn.phn.service.ScoringService;

import java.util.List;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class ScoringController {

    private final ScoringService scoringService;

    /**
     * Tính điểm chuyên cần và chất lượng (WQT) cho một user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ScoringDto> getUserScore(@PathVariable Long userId) {
        ScoringDto score = scoringService.calculateScore(userId);
        return score != null ? ResponseEntity.ok(score) : ResponseEntity.notFound().build();
    }

    /**
     * Lấy bảng xếp hạng điểm cho tất cả user.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<ScoringDto>> getRanking() {
        return ResponseEntity.ok(scoringService.getRanking());
    }
}
