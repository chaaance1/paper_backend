package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FigureResultDto {
    private int index;
    private int sectionOrder;
    private String imagePath;   // 저장 경로 또는 URL
}
