package dddan.paper_summary.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiFullTextRequest {

    @JsonProperty("paper_id")
    private String paperId;

    @JsonProperty("paper_title")
    private String paperTitle;

    @JsonProperty("full_text")
    private String fullText;
}
