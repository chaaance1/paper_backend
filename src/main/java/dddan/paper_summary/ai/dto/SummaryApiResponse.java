package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;

/**
 * AI Summary API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryApiResponse {
    private Integer sectionId;
    private String textResult;
    private List<String> imageResults;
    private List<String> tableResults;
    private List<String> equationResults;
}
