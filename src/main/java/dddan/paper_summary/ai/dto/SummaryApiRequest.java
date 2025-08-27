package dddan.paper_summary.ai.dto;

import lombok.*;
import java.util.List;

/**
 * AI Summary API 요청 DTO (섹션 기반 요청)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryApiRequest {
    private Integer sectionId;
    private String tableOfContents;
    private String paperTitle;
    private String sectionTitle;
    private String text;
    private List<String> images;
    private List<String> tables;
    private List<String> equations;
}
