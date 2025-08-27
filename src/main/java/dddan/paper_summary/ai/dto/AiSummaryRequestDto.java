package dddan.paper_summary.ai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 논문 요약 요청 DTO (프론트 → 백엔드)
 * - 논문 ID와 사용자 ID를 전달받아 섹션별로 요약 요청을 처리함
 * - userId는 로그인하지 않은 경우 null 가능
 */
@Getter
@Setter
public class AiSummaryRequestDto {
    private Long paperId;
    private String userId;

    public AiSummaryRequestDto() {}

    public AiSummaryRequestDto(Long paperId, String userId) {
        this.paperId = paperId;
        this.userId = userId;
    }
}
