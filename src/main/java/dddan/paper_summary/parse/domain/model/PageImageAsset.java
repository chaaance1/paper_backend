// PageImageAsset.java
package dddan.paper_summary.parse.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 논문 PDF의 "페이지 전체를 이미지로 렌더링한 결과"를 표현하는 도메인 모델
 * - PDF → 이미지(PNG/JPEG) 변환 단계의 결과물
 * - 한 페이지당 하나의 이미지가 생성된다
 * - 본 프로젝트에서는 주로 "수식(Formula) OCR 추출"의 입력 데이터로 사용된다
 * - 실제 이미지 데이터는 포함하지 않고, 저장 위치 정보만 관리한다
 */
@Getter
@Builder
public class PageImageAsset {
    private final Long paperId;
    private final int pageNumber;      // 1-based
    private final String localPath;    // or storageUrl
}
