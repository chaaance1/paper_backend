package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SectionResultDto {
    private int sectionOrder;  // 1,2,3...
    private String title;      // "1 Introduction"
    private String content;    // 섹션 본문 텍스트
}
