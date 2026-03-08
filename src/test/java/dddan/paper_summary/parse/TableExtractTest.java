package dddan.paper_summary.parse;

import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TableAsset;
import dddan.paper_summary.parse.infra.PdfboxTableExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TableExtractTest {
    public static void main(String[] args) throws Exception {

        PaperRef ref = PaperRef.builder()
                .paperId(1L)
                .localPath("C:/Users/yuswe/Downloads/2103.00112v3.pdf")
                .build();

        PdfboxTableExtractor extractor = new PdfboxTableExtractor();
        List<TableAsset> tables = extractor.extract(ref);

        System.out.println("===== TABLE EXTRACT RESULT =====");
        System.out.println("tables.size() = " + tables.size());
        System.out.println();

        int i = 0;
        for (TableAsset t : tables) {
            System.out.println("index=" + (i++));

            // 아래 getter들은 네 TableAsset에 맞게 수정
            System.out.println("pageNumber=" + t.getPageNumber());

            // 보통 csvPath/tablePath/localPath 같은 필드가 있음
            String path = null;
            try {
                path = t.getTablePath(); // <-- 없으면 getTablePath() 등으로 변경
            } catch (Exception ignored) {}

            if (path != null) {
                System.out.println("path=" + path);

                Path p = Paths.get(path);
                System.out.println("exists=" + Files.exists(p));
                if (Files.exists(p)) {
                    System.out.println("bytes=" + Files.size(p));
                }
            } else {
                System.out.println("path=(TableAsset에 path 필드가 없거나 getter명이 다름)");
            }

            if (t.getRegion() != null) {
                System.out.println("region=" +
                        "x=" + t.getRegion().getX() + ", " +
                        "y=" + t.getRegion().getY() + ", " +
                        "w=" + t.getRegion().getWidth() + ", " +
                        "h=" + t.getRegion().getHeight()
                );
            } else {
                System.out.println("region=null");
            }

            System.out.println("------------------------------------");
        }
    }
}
