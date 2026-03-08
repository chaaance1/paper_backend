package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.FigureExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.FigureAsset;
import dddan.paper_summary.parse.domain.model.PaperRef;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfboxFigureExtractor implements FigureExtractor {

    @Override
    public List<FigureAsset> extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        List<FigureAsset> results = new ArrayList<>();

        // 출력 디렉터리
        Path outDir = Paths.get("tmp", "figures", String.valueOf(ref.getPaperId()));
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            throw new DomainException("MKDIR_FAILED: " + outDir, e);
        }

        try (PDDocument doc = PdfboxCommon.open(pdf)) {
            int pageIdx = 1;
            int imgIdx = 1;

            for (PDPage page : doc.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) {
                    pageIdx++;
                    continue;
                }

                for (var name : resources.getXObjectNames()) {
                    PDXObject xObject = resources.getXObject(name);

                    if (xObject instanceof PDImageXObject image) {
                        BufferedImage bi = image.getImage();
                        if (bi == null) continue;

                        // 너무 작은 아이콘/로고 필터(원하면 조정)
                        if (bi.getWidth() < 80 || bi.getHeight() < 80) continue;

                        String fileName = "p" + pageIdx + "_img" + (imgIdx++) + ".png";
                        Path filePath = outDir.resolve(fileName);

                        ImageIO.write(bi, "png", filePath.toFile());

                        results.add(FigureAsset.builder()
                                .paperId(ref.getPaperId())
                                .pageNumber(pageIdx)
                                .sectionOrder(0)
                                .imagePath(filePath.toString())
                                .build());
                    }
                }

                pageIdx++;
            }

            return results;

        } catch (IOException e) {
            throw new DomainException("PDF_FIGURE_EXTRACT_FAILED: " + e.getMessage(), e);
        }
    }
}