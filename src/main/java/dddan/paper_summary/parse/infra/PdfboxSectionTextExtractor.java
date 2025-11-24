package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.SectionTextExtractor;
import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.SectionAsset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PdfboxSectionTextExtractor implements SectionTextExtractor {

    private final TextExtractor textExtractor;

    @Override
    public List<SectionAsset> extract(PaperRef ref) throws DomainException {
        // 1) 먼저 전체 텍스트 추출
        var textAsset = textExtractor.extract(ref);

        // 2) fullText 기준으로 섹션 나누기
        return SectionSplitter.split(
                ref.getPaperId(),
                textAsset.getFullText()
        );
    }
}
