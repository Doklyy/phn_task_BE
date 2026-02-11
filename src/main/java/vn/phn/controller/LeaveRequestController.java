package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.CreateLeaveRequest;
import vn.phn.dto.LeaveRequestDto;
import vn.phn.entity.LeaveRequestType;
import vn.phn.service.LeaveRequestService;

import java.util.List;
import java.util.Map;

/**
 * Đơn xin nghỉ / xin đến muộn / xin về sớm. Admin duyệt.
 */
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * POST /api/leave-requests - Tạo đơn xin nghỉ/muộn/về sớm
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestBody CreateLeaveRequest req) {
        if (req == null || req.getType() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Loại đơn và ngày bắt đầu, kết thúc bắt buộc."));
        }
        LeaveRequestDto dto = leaveRequestService.create(
                userId,
                req.getType(),
                req.getFromDate(),
                req.getToDate() != null ? req.getToDate() : req.getFromDate(),
                req.getFromTime(),
                req.getToTime(),
                req.getReason());
        if (dto == null) {
            String msg = "Không tạo được đơn. Kiểm tra dữ liệu (lý do, ngày). Nghỉ hiếu hỷ tối đa 3 ngày.";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/leave-requests/my?userId=...
     */
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> getMyRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(leaveRequestService.getByUser(userId));
    }

    /**
     * GET /api/leave-requests/pending - Admin: đơn chờ duyệt
     */
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPending(@RequestParam Long adminId) {
        return ResponseEntity.ok(leaveRequestService.getPendingForAdmin());
    }

    /**
     * PATCH /api/leave-requests/{id}/approve?adminId=...
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestParam Long adminId) {
        LeaveRequestDto dto = leaveRequestService.approve(id, adminId);
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không duyệt được."));
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * PATCH /api/leave-requests/{id}/reject?adminId=... - Body: { "reason": "..." }
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam Long adminId, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        LeaveRequestDto dto = leaveRequestService.reject(id, adminId, reason);
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không từ chối được."));
        }
        return ResponseEntity.ok(dto);
    }
}
