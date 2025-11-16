package dddan.paper_summary.parse.infra.pdfbox;

import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;

import java.io.File;
import java.io.IOException;

public final class PdfboxCommon {
    private PdfboxCommon() {}

    public static File requireExistingPdf(PaperRef ref) throws DomainException {
        String path = ref.getLocalPath();
        if (path == null || path.isBlank()) {
            throw new DomainException("LOCAL_PATH_EMPTY for paper=" + ref.getPaperId());
        }
        File f = new File(path);
        if (!f.exists()) {
            throw new DomainException("PDF_NOT_FOUND: " + path);
        }
        return f;
    }

    public static PDDocument open(File pdf) throws IOException {
        return PDDocument.load(pdf);
    }

    public static ObjectExtractor extractor(PDDocument doc) {
        return new ObjectExtractor(doc);
    }
}
