package dddan.paper_summary.parse.domain.model;

import lombok.*;

/**
 * 논문 PDF에서 추출된 "전체 텍스트(Full Text)"를 표현하는 도메인 모델
 * - PDF 문서 전체를 대상으로 텍스트를 추출한 결과물
 * - 섹션 분리 이전의 원본 텍스트 데이터
 * - SectionAsset 생성, 전체 요약, AI 전체 문맥 분석의 입력으로 사용된다
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextAsset {

    private Long paperId;                 // 어떤 논문인지
    private String fullText;              // PDF 전체 텍스트
}
