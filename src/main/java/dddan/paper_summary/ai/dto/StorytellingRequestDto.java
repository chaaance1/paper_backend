package dddan.paper_summary.ai.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI 스토리텔링 API 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorytellingRequestDto {
    @JsonProperty("full_paper_text") private String fullPaperText;
}
