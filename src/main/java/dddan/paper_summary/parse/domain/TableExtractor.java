// TableExtractor.java
package dddan.paper_summary.parse.domain;

import java.util.List;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TableAsset;
import dddan.paper_summary.parse.domain.error.DomainException;

/** 표(table) 추출 전용 포트. */
public interface TableExtractor {
    List<TableAsset> extract(PaperRef ref) throws DomainException;
}
