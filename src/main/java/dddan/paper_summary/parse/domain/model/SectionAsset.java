package dddan.paper_summary.parse.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 논문 PDF에서 추출된 "섹션(Section)" 하나를 표현하는 도메인 모델
 * - 논문의 구조적 단위(Introduction, Method, Experiment 등)를 표현
 * - 섹션 제목과 해당 섹션의 본문 텍스트를 함께 보관한다
 * - TableAsset, FigureAsset, FormulaAsset 등과
 *   sectionOrder를 기준으로 논리적으로 연결된다
 */
@Getter
@Builder
public class SectionAsset {
    private final Long paperId;
    private final int sectionOrder;  // 1,2,3...
    private final String title;      // "1 Introduction"
    private final String content;    // 섹션 본문
}
