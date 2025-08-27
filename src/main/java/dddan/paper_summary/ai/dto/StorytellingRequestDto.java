package dddan.paper_summary.ai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 논문 스토리텔링 요청 DTO (백엔드 → AI)
 * - 전체 논문 텍스트를 기반으로 스토리 형태의 요약을 요청함
 */
@Getter
@Setter
public class StorytellingRequestDto {
    private String fullPaperText;

    public StorytellingRequestDto() {}

    public StorytellingRequestDto(String fullPaperText) {
        this.fullPaperText = fullPaperText;
    }
}
