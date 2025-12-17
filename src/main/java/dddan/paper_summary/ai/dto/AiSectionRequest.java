package dddan.paper_summary.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiSectionRequest {

    @JsonProperty("section_id")
    private int sectionId;

    @JsonProperty("section_title")
    private String sectionTitle;

    @JsonProperty("text")
    private String text;

    @JsonProperty("figures")
    private List<String> figures;

    @JsonProperty("tables")
    private List<String> tables;
}
