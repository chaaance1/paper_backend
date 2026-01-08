package dddan.paper_summary.parse.infra;

import lombok.Value;

/**
 * PDF 페이지 내에서 표(Table)가 차지하는 사각형 영역 정보를 담는 값 객체
 * - 좌표 기반 표 추출(Tabula, PDFBox 등)에 사용
 * - 불변 객체로 설계되어 파싱 결과의 안정성 보장
 */

@Value
public class TableRect {
    int pageNumber;
    float x;
    float y;
    float width;
    float height;
}
