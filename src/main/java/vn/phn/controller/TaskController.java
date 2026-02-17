package vn.phn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.TaskDto;
import vn.phn.dto.CreateTaskRequest;
import vn.phn.dto.UpdateTaskRequest;
import vn.phn.dto.CompletionReportRequest;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.service.ExcelExportService;
import vn.phn.service.TaskService;
import vn.phn.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final ExcelExportService excelExportService;

    /**
     * Lấy danh sách nhiệm vụ theo User ID và quyền (FE gửi userId, backend tự lấy role từ DB).
     * Query: ?userId=1 hoặc header X-User-Id
     */
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false, defaultValue = "all") String filter) {

        User user = userService.getEntity(userId);
        if (user == null)
            return ResponseEntity.badRequest().build();
        Role role = user.getRole();

        List<TaskDto> tasks;
        if (filter.equalsIgnoreCase("month") && year != null && month != null) {
            tasks = taskService.getTasksFilteredByMonth(userId, role, year, month);
        } else if (filter.equalsIgnoreCase("quarter") && year != null && quarter != null) {
            tasks = taskService.getTasksFilteredByQuarter(userId, role, year, quarter);
        } else if (filter.equalsIgnoreCase("year") && year != null) {
            tasks = taskService.getTasksFilteredByYear(userId, role, year);
        } else {
            tasks = taskService.getTasksForUser(userId, role);
        }
        return ResponseEntity.ok(tasks);
    }

    /** Tạo task mới (Admin/Leader). Assigner = userId gửi lên. */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest req, @RequestParam Long assignerId) {
        TaskDto created = taskService.createTask(req, assignerId);
        return ResponseEntity.ok(created);
    }

    /** Tiếp nhận task (chuyển NEW -> ACCEPTED) */
    @PatchMapping("/{taskId}/accept")
    public ResponseEntity<TaskDto> acceptTask(@PathVariable Long taskId, @RequestParam Long userId) {
        TaskDto updated = taskService.acceptTask(taskId, userId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Assignee gửi báo cáo hoàn thành → chuyển sang Đợi duyệt (PENDING_APPROVAL).
     * POST /api/tasks/{taskId}/complete?userId=...
     * Body (JSON, tùy chọn): { "completionNote", "completionLink", "completionFilePath" }
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<?> submitCompletion(
            @PathVariable Long taskId,
            @RequestBody(required = false) CompletionReportRequest req,
            @RequestParam Long userId) {
        String note = req != null ? req.getCompletionNote() : null;
        String link = req != null ? req.getCompletionLink() : null;
        String filePath = req != null ? req.getCompletionFilePath() : null;
        TaskDto updated = taskService.submitCompletion(taskId, userId, note, link, filePath);
        if (updated != null)
            return ResponseEntity.ok(updated);
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Không gửi được: kiểm tra task tồn tại, bạn là người được giao (assignee), và task đang trạng thái Đang thực hiện."
        ));
    }

    /**
     * Leader (người phân công) duyệt báo cáo hoàn thành → COMPLETED. Admin chỉ theo dõi, không gọi API này.
     * PATCH /api/tasks/{taskId}/approve?userId=...&quality=0.9
     */
    @PatchMapping("/{taskId}/approve")
    public ResponseEntity<TaskDto> approveCompletion(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestParam(required = false) Double quality) {
        TaskDto updated = taskService.approveCompletion(taskId, userId, quality);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Leader (người phân công) từ chối → trả về tồn đọng (ACCEPTED).
     * PATCH /api/tasks/{taskId}/reject?userId=...&reason=...
     */
    @PatchMapping("/{taskId}/reject")
    public ResponseEntity<TaskDto> rejectCompletion(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        TaskDto updated = taskService.rejectCompletion(taskId, userId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /** Cập nhật task (trạng thái, chất lượng) - Leader/Admin. Query params. */
    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double quality,
            @RequestBody(required = false) UpdateTaskRequest body) {
        User user = userService.getEntity(userId);
        if (user == null || (user.getRole() != Role.ADMIN && user.getRole() != Role.LEADER))
            return ResponseEntity.status(403).build();
        if (body != null && (body.getTitle() != null || body.getContent() != null || body.getObjective() != null
                || body.getDeadline() != null || body.getWeight() != null || body.getStatus() != null || body.getQuality() != null)) {
            TaskDto updated = taskService.updateTaskDetails(taskId, body, userId, user.getRole());
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
        }
        vn.phn.entity.TaskStatus s = status != null ? vn.phn.entity.TaskStatus.valueOf(status) : null;
        TaskDto updated = taskService.updateTask(taskId, s, quality, userId, user.getRole());
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Xuất Excel theo bộ lọc. Query: userId, year, month/quarter, filter=month|quarter|year|all
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false, defaultValue = "all") String filter) throws IOException {

        User user = userService.getEntity(userId);
        if (user == null) return ResponseEntity.badRequest().build();
        Role role = user.getRole();

        List<TaskDto> tasks;
        if (filter.equalsIgnoreCase("month") && year != null && month != null) {
            tasks = taskService.getTasksFilteredByMonth(userId, role, year, month);
        } else if (filter.equalsIgnoreCase("quarter") && year != null && quarter != null) {
            tasks = taskService.getTasksFilteredByQuarter(userId, role, year, quarter);
        } else if (filter.equalsIgnoreCase("year") && year != null) {
            tasks = taskService.getTasksFilteredByYear(userId, role, year);
        } else {
            tasks = taskService.getTasksForUser(userId, role);
        }

        byte[] bytes = excelExportService.exportTasksToExcel(tasks);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "danh-sach-nhiem-vu.xlsx");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
