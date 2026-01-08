package dddan.paper_summary.parse.dto;

import lombok.*;

/**
 * PDF 파싱 결과 중 "Table(표)" 하나를 표현하는 결과 DTO
 * - PDF에서 추출된 개별 표의 메타데이터를 외부(API 응답, AI 요청 등)로 전달하기 위한 객체
 * - 도메인 모델(TableAsset 등)을 직접 노출하지 않기 위한 결과 전용 DTO
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TableResultDto {
    private int index;          // 0-based
    private int sectionOrder;
    private String tablePath;     // 저장 경로 또는 URL(없으면 빈 문자열)
}
