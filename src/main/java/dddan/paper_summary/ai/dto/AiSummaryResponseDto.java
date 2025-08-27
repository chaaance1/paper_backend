package dddan.paper_summary.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 전체 논문 요약 응답 DTO (백엔드 → 프론트)
 * - 논문 기본 정보(제목, 저자, 날짜)와 섹션별 요약 리스트를 포함
 */
@Getter
@Setter
public class AiSummaryResponseDto {

    private String paperTitle;      // 논문 제목
    private String authors;         // 논문 저자
    private String publishedDate;   // 논문 발행일

    private List<SectionSummaryDto> sectionSummaries; // 섹션별 요약 리스트
}
