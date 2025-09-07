package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 논문 요약 전체 흐름을 관리하는 백엔드 내부 요청 DTO
 * - 어떤 논문을
 * - 어떤 사용자(userId)가
 * - 어떤 섹션들에 대해 요약을 요청하는지 명시
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRequestDto {
    private Long paperId;                  // 논문 ID
    private String paperTitle;             // 논문 제목
    private String userId;                 // 요청한 사용자 ID

    @JsonProperty("sections")
    private List<AiSummaryRequestDto> sections;
}
