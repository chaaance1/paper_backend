// FigureExtractor.java
package dddan.paper_summary.parse.domain;

import java.util.List;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.FigureAsset;
import dddan.paper_summary.parse.domain.error.DomainException;

/** 그림/그래프/이미지 추출 전용 포트. */
public interface FigureExtractor {
    List<FigureAsset> extract(PaperRef ref) throws DomainException;
}
