package dddan.paper_summary.parse.infra.pdfbox;

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

@Component
public class PdfboxFigureExtractor implements FigureExtractor {

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

                // 파일명: paperId_page.png
                String base = (ref.getPaperId() != null ? String.valueOf(ref.getPaperId()) : "paper");
                File out = new File(outDir, base + "_page_" + (page + 1) + ".png");
                ImageIO.write(img, "png", out);

                // ⚠️ FigureAsset의 필드명이 imagePath가 아니라 path라면 .imagePath(...)를 .path(...)로 바꿔주세요.
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
