package dddan.paper_summary.parse.infra.pdfbox;

import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class PdfboxTextExtractor implements TextExtractor {

    @Override
    public TextAsset extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        try (PDDocument doc = PdfboxCommon.open(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return TextAsset.of(text); // ← TextAsset.of(String) 이미 있음
        } catch (IOException e) {
            throw new DomainException("PDF_TEXT_EXTRACT_FAILED: " + e.getMessage(), e);
        }
    }
}
