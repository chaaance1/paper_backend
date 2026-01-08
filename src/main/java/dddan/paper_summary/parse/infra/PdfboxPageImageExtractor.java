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

/**
 * PDFBox를 이용해 PDF의 각 페이지를 이미지(PNG)로 렌더링하는 인프라 구현체
 * - PDF 파일을 페이지 단위로 이미지 변환
 * - 변환된 이미지를 로컬 디렉터리에 저장
 * - 페이지별 메타데이터를 PageImageAsset으로 반환
 */
@Component
@RequiredArgsConstructor
public class PdfboxPageImageExtractor implements PageImageExtractor {

    @Value("${parse.page-image.dpi:200}")
    private float dpi;

    @Value("${parse.page-image.output-dir:pdf-pages}")
    private String outputDir;

    /**
     * PDF 파일의 모든 페이지를 이미지로 렌더링한다.
     *
     * @param ref 논문 식별 정보 (paperId, PDF 로컬 경로 포함)
     * @return 페이지별 이미지 정보(PageImageAsset) 리스트
     * @throws DomainException PDF 로드 또는 렌더링 실패 시
     */

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
