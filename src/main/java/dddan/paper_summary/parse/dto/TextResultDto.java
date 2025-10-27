package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TextResultDto {
    private String fullText;    // 추출 텍스트(간이)
    private int pageCount;      // 페이지 수(옵션)
}
