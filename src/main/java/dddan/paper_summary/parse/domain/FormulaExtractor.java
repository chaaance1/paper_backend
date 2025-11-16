// FormulaExtractor.java
package dddan.paper_summary.parse.domain;

import java.util.List;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.FormulaAsset;
import dddan.paper_summary.parse.domain.error.DomainException;

/** 수식 추출 전용 포트(이미지/LaTeX 등). */
public interface FormulaExtractor {
    List<FormulaAsset> extract(PaperRef ref) throws DomainException;
}
