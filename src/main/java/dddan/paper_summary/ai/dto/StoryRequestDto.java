package dddan.paper_summary.ai.dto;

import lombok.*;

/**
 * 스토리텔링 전체 흐름을 처리하는 백엔드 내부 요청 DTO
 * - 어떤 논문을
 * - 어떤 사용자(userId)가
 * - 어떤 전체 텍스트로 스토리텔링을 요청하는지 명시
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryRequestDto {
    private Long paperId;         // 논문 ID
    private String fullText;      // 논문 전체 텍스트
    private String userId;        // 요청한 사용자 ID
    private java.util.UUID requestId;
}
