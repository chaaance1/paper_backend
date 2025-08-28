package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * AI 요약 API 요청용 DTO (섹션 1개당 1개 요청)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AiSummaryRequestDto {
    @JsonProperty("section_id") private int sectionId;
    @JsonProperty("table_of_contents") private String tableOfContents;
    @JsonProperty("paper_title") private String paperTitle;
    @JsonProperty("section_title") private String sectionTitle;
    @JsonProperty("text") private String text;
    @JsonProperty("images") private List<String> images;
    @JsonProperty("tables") private List<String> tables;
    @JsonProperty("equations") private List<String> equations;
}