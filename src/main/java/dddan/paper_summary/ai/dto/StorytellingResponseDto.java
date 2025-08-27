package dddan.paper_summary.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 논문 스토리텔링 응답 DTO (AI → 백엔드 → 프론트)
 * - 논문 전체 스토리를 구성하는 단계별 요약 내용
 * - 예: 배경 → 문제 → 해결책 → 실험 → 결과 → 영향 등
 */
@Getter
@Setter
public class StorytellingResponseDto {

    private String title; // 예: "논문 스토리 요약 (Paper Story Summary)"

    private List<StorySectionDto> sections; // 각 단계별 요약 내용

    /**
     * 내부 클래스: 스토리 단계 하나에 해당하는 정보
     */
    @Getter
    @Setter
    public static class StorySectionDto {
        private Integer step;      // 단계 순서 (1, 2, 3, ...)
        private String heading;    // 단계 제목 (예: 배경, 문제 등)
        private String content;    // 해당 단계 설명
    }
}
