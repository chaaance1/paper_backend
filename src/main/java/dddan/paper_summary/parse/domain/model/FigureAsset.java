package dddan.paper_summary.parse.domain.model;

import lombok.*;

/**
 * 논문(Paper) 내에서 추출된 "그림(Figure)" 하나를 표현하는 도메인 모델
 * - PDF 파싱 단계에서 그림 이미지를 추출한 뒤
 * - 해당 그림의 위치·소속 정보를 함께 담기 위해 사용된다
 * - 실제 이미지 데이터는 포함하지 않고, 저장 경로만 관리한다
 */
@Getter
@Builder
public class FigureAsset {

    private final Long paperId;       // 어떤 논문 소속인지
    private int pageNumber;
    private final int sectionOrder;   // 섹션 번호 (1,2,3…)
    private final String imagePath;   // 이미지 저장 경로
}

