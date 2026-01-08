package dddan.paper_summary.parse.domain.model;

import lombok.*;

/**
 * PDF 페이지 내에서 "표(Table)가 위치한 영역"을 표현하는 좌표 모델
 * - TableAsset에 포함되어 표의 위치 정보를 나타낸다
 * - PDF 좌표계를 기준으로 한 사각형 영역(Rectangle)
 * - 표 재추출, 시각적 디버깅, 인식 정확도 개선에 활용된다
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRegion {
    private float x;
    private float y;
    private float width;
    private float height;
}
