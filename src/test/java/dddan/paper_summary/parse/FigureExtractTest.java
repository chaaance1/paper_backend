package dddan.paper_summary.parse;

import dddan.paper_summary.parse.domain.FigureExtractor;
import dddan.paper_summary.parse.domain.model.FigureAsset;
import dddan.paper_summary.parse.domain.model.PaperRef;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FigureExtractTest {

    @Autowired
    private FigureExtractor figureExtractor;

    @Test
    void extract_figures_smoke() throws Exception {
        // ✅ 네 PDF 경로로 바꿔도 됨
        String pdfPath = "C:/Users/yuswe/Downloads/2103.00112v3.pdf";

        File pdf = new File(pdfPath);
        System.out.println("===== FIGURE EXTRACT SMOKE =====");
        System.out.println("PDF exists=" + pdf.exists() + ", bytes=" + pdf.length());
        assertTrue(pdf.exists(), "PDF_NOT_FOUND: " + pdfPath);
        assertTrue(pdf.length() > 0, "PDF_EMPTY: " + pdfPath);

        PaperRef ref = PaperRef.builder()
                .paperId(1L)
                .localPath(pdfPath)
                .build();

        List<FigureAsset> figures = figureExtractor.extract(ref);

        System.out.println("figures.size() = " + figures.size());
        if (figures.isEmpty()) {
            fail("No figures extracted");
        }

        for (int i = 0; i < figures.size(); i++) {
            FigureAsset f = figures.get(i);

            String path = f.getImagePath();
            File out = (path == null) ? null : new File(path);

            System.out.println("------------------------------------");
            System.out.println("index=" + i);
            System.out.println("pageNumber=" + f.getPageNumber());
            System.out.println("path=" + path);

            if (out != null && out.exists()) {
                System.out.println("bytes=" + out.length());
                assertTrue(out.length() > 0, "EMPTY_OUTPUT_FILE: " + path);
            } else {
                // 저장 경로가 null/빈값이거나 파일이 안 생긴 경우
                fail("OUTPUT_NOT_FOUND: " + path);
            }
        }
    }
}
