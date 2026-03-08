package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDFBox 기반 텍스트 추출기
 * - PDF를 "한 번만" 열어서
 *   1) 페이지별 텍스트(pageTexts)
 *   2) 전체 텍스트(fullText = pageTexts join)
 *   를 함께 생성한다.
 * NOTE:
 * - PDFTextStripper의 startPage/endPage는 1-based.
 * - ColumnAwareTextStripper(네 커스텀)도 PDFTextStripper 상속이므로 동일하게 사용 가능.
 */
@Component
public class PdfboxTextExtractor implements TextExtractor {

    @Override
    public TextAsset extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);

        try (PDDocument doc = PdfboxCommon.open(pdf)) {
            int pageCount = doc.getNumberOfPages();

            // 1) 페이지별 텍스트 추출
            List<String> pageTexts = new ArrayList<>(pageCount);

            // (PDFTextStripper로 바꿔도 됨)
            ColumnAwareTextStripper stripper = new ColumnAwareTextStripper();

            for (int pageNo = 1; pageNo <= pageCount; pageNo++) {
                stripper.setStartPage(pageNo); // 1-based
                stripper.setEndPage(pageNo);

                String pageText = stripper.getText(doc);
                if (pageText == null) pageText = "";
                pageTexts.add(pageText.trim());
            }

            // 2) 전체 텍스트(fullText) 생성: pageTexts를 합친 뷰(view)
            StringBuilder full = new StringBuilder();
            for (int i = 0; i < pageTexts.size(); i++) {
                if (i > 0) full.append("\n\n"); // 페이지 경계 구분(원하면 \f 같은 구분자도 가능)
                full.append(pageTexts.get(i));
            }
            String fullText = full.toString().trim();

            // 3) TextAsset 구성
            return TextAsset.builder()
                    .paperId(ref.getPaperId())
                    .fullText(fullText)
                    .pageTexts(pageTexts)
                    .pageCount(pageCount)
                    .build();

        } catch (IOException e) {
            throw new DomainException("PDF_TEXT_EXTRACT_FAILED: " + e.getMessage(), e);
        }
    }
}
