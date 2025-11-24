package dddan.paper_summary.parse.domain;

import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.SectionAsset;

import java.util.List;

public interface SectionTextExtractor {
    List<SectionAsset> extract(PaperRef ref) throws DomainException;
}
