package dddan.paper_summary.parse.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SectionAsset {
    private final Long paperId;
    private final int sectionOrder;  // 1,2,3...
    private final String title;      // "1 Introduction"
    private final String content;    // 섹션 본문
}
