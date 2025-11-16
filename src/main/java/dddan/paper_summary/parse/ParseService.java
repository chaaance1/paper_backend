package dddan.paper_summary.parse;

import lombok.RequiredArgsConstructor;
import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.TableExtractor;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.ParseResult;

import dddan.paper_summary.parse.domain.model.TextAsset;
import dddan.paper_summary.parse.domain.model.TableAsset;

import dddan.paper_summary.parse.domain.ParseUseCase;
import dddan.paper_summary.parse.domain.error.DomainException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParseService implements ParseUseCase {

    private final TextExtractor textExtractor;
    private final TableExtractor tableExtractor;

    @Override
    public ParseResult parse(PaperRef ref) {
        try {
            // PDDocument 직접 다루지 않음. 포트에 PaperRef만 넘김
            TextAsset text = textExtractor.extract(ref);
            List<TableAsset> tables = tableExtractor.extract(ref);

            // ParseResult가 도메인 타입을 받도록(권장)
            return ParseResult.success(ref.getPaperId(), text, tables);

            // 만약 ParseResult.success가 아직 (String, List<TableData>)만 받는다면
            // 아래 임시 변환으로 사용 가능 (필요 시 주석 해제해서 사용)
            // String extractedText = text != null ? text.getText() : "";
            // List<TableData> tableDtos = tables.stream().map(TableData::from).toList();
            // return ParseResult.success(ref.getPaperId(), extractedText, tableDtos);

        } catch (DomainException e) {
            return ParseResult.error(ref.getPaperId(), e.getMessage());
        }
    }
}