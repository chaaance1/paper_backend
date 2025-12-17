package dddan.paper_summary.ai.mapper;

import dddan.paper_summary.ai.dto.AiFullTextRequest;
import dddan.paper_summary.arxiv.dto.ArxivPaperDto;
import dddan.paper_summary.parse.domain.model.ParseResult;
import org.springframework.stereotype.Component;

@Component
public class AiFullTextMapper {

    public AiFullTextRequest toFullText(ArxivPaperDto paper, ParseResult parse) {
        return AiFullTextRequest.builder()
                .paperId(paper.getArxivId())                 // OK
                .paperTitle(paper.getTitle())                // OK
                .fullText(parse.getText().getFullText())     // OK (TextResultDto에 getFullText 있을 때)
                .build();
    }
}
