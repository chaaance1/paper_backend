package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;

/**
 * AI 스토리텔링 API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorytellingResponseDto {
    private String title;
    private List<Section> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Section {
        private int step;
        private String heading;
        private String content;
    }
}