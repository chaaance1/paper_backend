package dddan.paper_summary.parse.infra.pdfbox;

import dddan.paper_summary.parse.domain.TableExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TableAsset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.PageIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfboxTableExtractor implements TableExtractor {

    @Override
    @SuppressWarnings("DuplicatedCode")
    public List<TableAsset> extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        List<TableAsset> results = new ArrayList<>();

        try (PDDocument doc = PdfboxCommon.open(pdf);
             ObjectExtractor oe = PdfboxCommon.extractor(doc)) {

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();

            int pageIdx = 1;
            //Iterable<Page> 이므로 향상 for문 사용
            PageIterator it = oe.extract();
            while (it.hasNext()) {
                Page page = it.next();

                List<Table> tables = sea.extract(page);
                if (tables == null || tables.isEmpty()) {
                    tables = bea.extract(page);
                }

                for (Table t : tables) {
                    String csv = TabulaMapper.toCSV(t);
                    results.add(TableAsset.builder()
                            .pageNumber(pageIdx)
                            .csv(csv)
                            .build());
                }
                pageIdx++;
            }
            return results;

        } catch (IOException e) {
            throw new DomainException("PDF_TABLE_READ_FAILED: " + e.getMessage(), e);
        }
    }
}
