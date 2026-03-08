package dddan.paper_summary.parse;
import dddan.paper_summary.parse.domain.PageImageExtractor;
import dddan.paper_summary.parse.domain.model.PageImageAsset;
import dddan.paper_summary.parse.domain.model.PaperRef;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PageImageExtractorTest {

    @Autowired
    private PageImageExtractor pageImageExtractor;

    @Test
    void testRenderPages() throws Exception {
        // 여기만 너 PDF 경로/ID에 맞게 수정하면 됨
        PaperRef ref = PaperRef.builder()
                .paperId(1L)
                .localPath("C:/Users/yuswe/Downloads/2412.03801v1.pdf") // 테스트할 PDF 경로
                .build();

        List<PageImageAsset> pages = pageImageExtractor.render(ref);

        System.out.println("===== PAGE IMAGES =====");
        for (PageImageAsset page : pages) {
            System.out.printf("page %d -> %s%n",
                    page.getPageNumber(),
                    page.getImagePath());
        }
    }
}
