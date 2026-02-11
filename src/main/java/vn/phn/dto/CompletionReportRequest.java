package vn.phn.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionReportRequest {

    /** Ghi chú báo cáo hoàn thành (bắt buộc, tối thiểu 10 ký tự) */
    private String completionNote;

    /** Link đính kèm (tùy chọn) */
    private String completionLink;

    /** Đường dẫn file đính kèm (tùy chọn, FE có thể gửi sau khi upload) */
    private String completionFilePath;
}
