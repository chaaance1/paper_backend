package dddan.paper_summary.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 섹션 요약 결과 DTO (AI → 백엔드 → 프론트)
 * - 하나의 섹션에 대한 요약 결과를 담음
 * - 섹션 제목과 함께 텍스트, 이미지, 표, 수식 요약 결과 포함
 */
@Getter
@Setter
public class SectionSummaryDto {
    private Integer sectionId;
    private String sectionTitle; // 섹션 제목 (예: Introduction, Method 등)

    private String textResult; // 텍스트 요약 결과
    private List<String> imageResults; // 이미지 요약 결과 (텍스트 설명)
    private List<String> tableResults; // 표 요약 결과 (텍스트 설명)
    private List<String> equationResults; // 수식 요약 결과 (텍스트 설명)

    @Override
    public String toString() {
        return "SectionSummaryDto{" +
                "sectionId=" + sectionId +
                ", sectionTitle='" + sectionTitle + '\'' +
                ", textResult='" + textResult + '\'' +
                ", imageResults=" + imageResults +
                ", tableResults=" + tableResults +
                ", equationResults=" + equationResults +
                '}';
    }
}
