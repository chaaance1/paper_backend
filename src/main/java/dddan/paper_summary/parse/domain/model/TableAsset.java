package dddan.paper_summary.parse.domain.model;

import lombok.*;

/**
 * 논문 PDF에서 추출된 "표(Table)" 하나를 표현하는 도메인 모델
 * - PDF 내의 표 구조를 인식하여 CSV 등 구조화된 데이터로 추출한 결과를 표현
 * - 표가 위치한 페이지, 섹션 정보와 함께
 *   실제 표 데이터가 저장된 경로 및 좌표 영역 정보를 관리한다
 * - SectionAsset과 sectionOrder를 기준으로 논리적으로 연결된다
 */
 @Getter
@Builder
public class TableAsset {

    private final Long paperId;
    private int pageNumber;
    private final int sectionOrder;
    private final String tablePath;
    private TableRegion region;  // 표가 있는 좌표 영역
}
