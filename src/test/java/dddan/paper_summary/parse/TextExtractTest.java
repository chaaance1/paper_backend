package dddan.paper_summary.parse;

import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TextAsset;
import dddan.paper_summary.parse.domain.model.SectionAsset;
import dddan.paper_summary.parse.infra.PdfboxTextExtractor;
import dddan.paper_summary.parse.infra.SectionSplitter;

import java.util.List;

public class TextExtractTest {
    public static void main(String[] args) throws Exception {
        PaperRef ref = PaperRef.builder()
                .paperId(1L)
                .localPath("C:/Users/yuswe/Downloads/2412.03801v1.pdf")
                .build();

        PdfboxTextExtractor extractor = new PdfboxTextExtractor();
        TextAsset text = extractor.extract(ref);

        // 1) fullText 출력 (기존 그대로)
        System.out.println("===== TEXT START =====");
        System.out.println(text.getFullText());
        System.out.println("===== TEXT END =====");

        // 2) 섹션 분리 테스트 추가
        List<SectionAsset> sections =
                SectionSplitter.split(ref.getPaperId(), text.getFullText());

        System.out.println();
        System.out.println("===== SECTION SPLIT RESULT =====");
        System.out.println("sections.size() = " + sections.size());
        System.out.println();

        for (SectionAsset s : sections) {
            String preview = s.getContent()
                    .replace("\n", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();

            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }

            System.out.println("order=" + s.getSectionOrder());
            System.out.println("title=" + s.getTitle());
            System.out.println("preview=" + preview);
            System.out.println("------------------------------------");
        }
    }
}
