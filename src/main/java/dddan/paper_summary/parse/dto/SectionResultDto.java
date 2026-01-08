package dddan.paper_summary.parse.dto;

import lombok.*;
/**
 * PDF 파싱 결과 중 "Section(섹션)" 하나를 표현하는 결과 DTO
 * - 논문 본문을 논리적 단위(Introduction, Methods, Results 등)로 나눈 결과
 * - 각 섹션의 제목과 본문 텍스트를 외부(API 응답, AI 요청 등)로 전달하기 위한 객체
 * - 도메인 모델(SectionAsset 등)을 직접 노출하지 않기 위한 결과 전용 DTO
 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SectionResultDto {
    private int sectionOrder;  // 1,2,3...
    private String title;      // "1 Introduction"
    private String content;    // 섹션 본문 텍스트
}
