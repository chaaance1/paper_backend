package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.PageImageExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PageImageAsset;
import dddan.paper_summary.parse.domain.model.PaperRef;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PdfboxPageImageExtractor implements PageImageExtractor {

    @Value("${parse.page-image.dpi:200}")
    private float dpi;

    @Value("${parse.page-image.output-dir:pdf-pages}")
    private String outputDir;

    @Override
    public List<PageImageAsset> render(PaperRef ref) throws DomainException {
        List<PageImageAsset> result = new ArrayList<>();

        try (PDDocument doc = PDDocument.load(Path.of(ref.getLocalPath()).toFile())) {

            PDFRenderer renderer = new PDFRenderer(doc);

            int pageCount = doc.getNumberOfPages();

            Path baseDir = Path.of(outputDir, String.valueOf(ref.getPaperId()));
            Files.createDirectories(baseDir);

            for (int i = 0; i < pageCount; i++) {
                BufferedImage bim = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);

                int pageNumber = i + 1;
                Path outPath = baseDir.resolve("page-" + pageNumber + ".png");

                ImageIO.write(bim, "png", outPath.toFile());

                result.add(
                        PageImageAsset.builder()
                                .paperId(ref.getPaperId())
                                .pageNumber(pageNumber)
                                .localPath(outPath.toString())
                                .build()
                );
            }

            return result;

        } catch (IOException e) {
            throw new DomainException("PDF 페이지 렌더링 실패: " + ref.getLocalPath(), e);
        }
    }
}
