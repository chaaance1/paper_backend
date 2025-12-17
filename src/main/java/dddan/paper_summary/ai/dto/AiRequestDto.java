package dddan.paper_summary.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class AiRequestDto {

    @JsonProperty("paper_id")
    private String paperId;

    @JsonProperty("paper_title")
    private String paperTitle;

    @JsonProperty("table_of_contents")
    private String tableOfContents;

    @JsonProperty("formula_pages")
    private List<String> formulaPages;

    @JsonProperty("sections")
    private List<AiSectionRequest> sections;
}
