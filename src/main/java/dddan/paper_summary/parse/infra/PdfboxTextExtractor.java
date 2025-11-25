package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class PdfboxTextExtractor implements TextExtractor {

    @Override
    public TextAsset extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);

        try (PDDocument doc = PdfboxCommon.open(pdf)) {

            // 컬럼 감지만 수행
            ColumnAwareTextStripper stripper = new ColumnAwareTextStripper();
            String fullText = stripper.getText(doc);

            return TextAsset.builder()
                    .paperId(ref.getPaperId())
                    .fullText(fullText)
                    .build();

        } catch (IOException e) {
            throw new DomainException("PDF_TEXT_EXTRACT_FAILED: " + e.getMessage(), e);
        }
    }
}
