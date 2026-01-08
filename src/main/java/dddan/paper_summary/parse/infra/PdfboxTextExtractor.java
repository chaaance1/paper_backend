package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * PDFBox 기반 전체 텍스트 추출기
 * - PDF 논문 전체에서 텍스트를 추출한다.
 * - 다단(column) 구조를 고려한 ColumnAwareTextStripper를 사용한다.
 * - 추출 결과는 도메인 모델(TextAsset)로 변환되어 반환된다.
 * Infra 레이어 구현체로서,
 * 도메인 인터페이스(TextExtractor)를 구현한다.
 */
@Component
public class PdfboxTextExtractor implements TextExtractor {

    /**
     * PDF 파일로부터 전체 텍스트를 추출한다.
     *
     * @param ref 논문 식별 정보 (paperId, local PDF 경로 포함)
     * @return PDF 전체 텍스트를 담은 TextAsset
     * @throws DomainException PDF 접근 또는 텍스트 추출 실패 시
     */
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
