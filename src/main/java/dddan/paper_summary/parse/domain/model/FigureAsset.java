package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Getter
@Builder
public class FigureAsset {

    private final Long paperId;       // 어떤 논문 소속인지
    private int pageNumber;
    private final int sectionOrder;   // 섹션 번호 (1,2,3…)
    private final String imagePath;   // 이미지 저장 경로
}

