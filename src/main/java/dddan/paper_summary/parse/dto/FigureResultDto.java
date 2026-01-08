package dddan.paper_summary.parse.dto;

import lombok.*;

/**
 * PDF 파싱 결과 중 "Figure(그림)" 하나를 표현하는 결과 DTO
 * - PDF에서 추출된 그림의 메타데이터를 외부(API 응답, AI 요청, 프론트엔드 등)로 전달하기 위한 객체
 * - 도메인 모델(FigureAsset 등)을 직접 노출하지 않기 위한 계층 분리용 DTO
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FigureResultDto {
    private int index;
    private int sectionOrder;
    private String imagePath;   // 저장 경로 또는 URL
}
