package dddan.paper_summary.parse.dto;

import lombok.*;

/**
 * PDF 파싱 결과 중 "Text(전체 텍스트)"를 표현하는 결과 DTO
 * - PDF 문서 전체에서 추출된 원문 텍스트를 담는 객체
 * - 섹션 분리 이전 단계의 결과이거나,
 *   섹션 파싱이 실패했을 경우의 대체 데이터로 활용될 수 있다
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TextResultDto {
    private String fullText;    // 추출 텍스트(간이)
    private int pageCount;      // 페이지 수(옵션)
}
