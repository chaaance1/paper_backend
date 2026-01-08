package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.FigureExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.FigureAsset;
import dddan.paper_summary.parse.domain.model.PaperRef;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDFBox를 이용해 PDF 논문의 각 페이지를 이미지로 렌더링하여
 * FigureAsset 목록으로 추출하는 구현체
 */

@Component
public class PdfboxFigureExtractor implements FigureExtractor {

    /**
     * PDF 파일을 페이지 단위로 이미지(JPG)로 변환하여 FigureAsset 리스트로 반환
     *
     * @param ref PDF 위치 및 paperId 정보를 담은 참조 객체
     * @return 페이지별 이미지 정보를 담은 FigureAsset 리스트
     * @throws DomainException PDF가 없거나 이미지 추출 중 오류가 발생한 경우
     */

    @Override
    public List<FigureAsset> extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        List<FigureAsset> results = new ArrayList<>();

        // 출력 디렉터리 안전 생성
        File outDir = new File("output");
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new DomainException("MKDIR_FAILED: " + outDir.getAbsolutePath());
        }

        try (PDDocument doc = PdfboxCommon.open(pdf)) {
            PDFRenderer renderer = new PDFRenderer(doc);

            for (int page = 0; page < doc.getNumberOfPages(); page++) {
                // 페이지 렌더 (200DPI 정도면 확인용으로 충분)
                BufferedImage img = renderer.renderImageWithDPI(page, 200);

                // 파일명: paperId_page.jpg
                String base = (ref.getPaperId() != null ? String.valueOf(ref.getPaperId()) : "paper");
                File out = new File(outDir, base + "_page_" + (page + 1) + ".jpg");
                ImageIO.write(img, "jpg", out);


                results.add(FigureAsset.builder()
                        .imagePath(out.getAbsolutePath())
                        .build());
            }
            return results;

        } catch (IOException e) {
            throw new DomainException("PDF_IMAGE_EXTRACT_FAILED: " + e.getMessage(), e);
        }
    }
}
