// TextExtractor.java
package dddan.paper_summary.parse.domain;

import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;
import dddan.paper_summary.parse.domain.error.DomainException;

/** 텍스트 추출 전용 포트. */
public interface TextExtractor {
    TextAsset extract(PaperRef ref) throws DomainException;
}