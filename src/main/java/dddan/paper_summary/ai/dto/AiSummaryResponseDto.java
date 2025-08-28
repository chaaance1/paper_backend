package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI 요약 API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryResponseDto {
    @JsonProperty("section_id") private int sectionId;
    @JsonProperty("text_result") private String textResult;
    @JsonProperty("image_results") private List<String> imageResults;
    @JsonProperty("table_results") private List<String> tableResults;
    @JsonProperty("equation_results") private List<String> equationResults;
}