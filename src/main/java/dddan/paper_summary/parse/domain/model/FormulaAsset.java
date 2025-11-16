package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormulaAsset {
    private int id;
    private String imagePath;   // 수식 이미지 경로(추후 OCR 예정)
    private String latex;       // 수식 라텍스(없으면 null)
}
