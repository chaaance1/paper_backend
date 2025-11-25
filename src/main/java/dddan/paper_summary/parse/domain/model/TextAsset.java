package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextAsset {

    private Long paperId;                 // 어떤 논문인지
    private String fullText;              // PDF 전체 텍스트
}
