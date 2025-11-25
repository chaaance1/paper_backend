// PageImageExtractor.java
package dddan.paper_summary.parse.domain;

import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.PageImageAsset;

import java.util.List;

/** PDF 전체를 페이지 단위 이미지로 렌더링하는 포트 */
public interface PageImageExtractor {
    List<PageImageAsset> render(PaperRef ref) throws DomainException;
}
