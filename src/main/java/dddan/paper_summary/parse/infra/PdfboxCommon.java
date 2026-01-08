package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;

import java.io.File;
import java.io.IOException;

/**
 * PDFBox + Tabula 사용 시 공통으로 필요한 유틸리티 클래스
 * - PDF 파일 존재 여부 검증
 * - PDDocument 로드
 * - Tabula ObjectExtractor 생성
 */

public final class PdfboxCommon {
    private PdfboxCommon() {}

    /**
     * PaperRef에 저장된 로컬 PDF 경로를 검증하고
     * 실제 존재하는 PDF 파일(File)을 반환한다.
     *
     * @param ref 논문 참조 정보 (로컬 경로 포함)
     * @return 존재가 보장된 PDF File 객체
     * @throws DomainException 로컬 경로가 없거나 파일이 존재하지 않을 경우
     */

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

    /**
     * Tabula를 이용한 표 추출을 위해 ObjectExtractor를 생성한다.
     *
     * @param doc 이미 로드된 PDDocument
     * @return Tabula ObjectExtractor
     */
    public static ObjectExtractor extractor(PDDocument doc) {
        return new ObjectExtractor(doc);
    }
}
