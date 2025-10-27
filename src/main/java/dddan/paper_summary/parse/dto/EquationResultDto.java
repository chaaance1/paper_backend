package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquationResultDto {
    private int index;
    private String imagePath;   // 수식 이미지 경로(추후 OCR 예정)
    private String latex;       // 수식 라텍스(없으면 null)
}
