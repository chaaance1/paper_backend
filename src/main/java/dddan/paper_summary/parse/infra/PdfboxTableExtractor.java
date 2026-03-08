package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.TableExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TableAsset;
import dddan.paper_summary.parse.domain.model.TableRegion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.Rectangle;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class PdfboxTableExtractor implements TableExtractor {

    /**
     * 표 추출 정책
     * - SPREADSHEET_ONLY: Nurminen으로 영역 탐지 후 Spreadsheet(선 기반)만 시도
     * - SPREADSHEET_THEN_BASIC: Spreadsheet가 비면 해당 area에 한해 Basic fallback
     */
    public enum Mode { SPREADSHEET_ONLY, SPREADSHEET_THEN_BASIC }

    @Value("${parse.table.mode:SPREADSHEET_ONLY}")
    private String mode;

    @Override
    @SuppressWarnings("DuplicatedCode")
    public List<TableAsset> extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        List<TableAsset> results = new ArrayList<>();

        try (PDDocument doc = PdfboxCommon.open(pdf);
             ObjectExtractor oe = PdfboxCommon.extractor(doc)) {

            Mode m = parseMode(mode);

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
            NurminenDetectionAlgorithm detector = new NurminenDetectionAlgorithm();

            Path baseDir = Paths.get("tmp", "tables", String.valueOf(ref.getPaperId()));
            Files.createDirectories(baseDir);

            int pageIdx = 1;
            int tableIdx = 1;

            PageIterator it = oe.extract();
            while (it.hasNext()) {
                Page page = it.next();

                // 1) 표 영역 탐지 (페이지 전체가 아닌 '표가 있을만한 영역'들)
                List<Rectangle> areas = detector.detect(page);
                if (areas == null || areas.isEmpty()) {
                    pageIdx++;
                    continue;
                }

                // 2) 영역별 추출
                for (Rectangle area : areas) {
                    Page areaPage = page.getArea(area);

                    List<Table> tables = sea.extract(areaPage);

                    // 3) 정책에 따라 area 단위 Basic fallback
                    if ((tables == null || tables.isEmpty()) && m == Mode.SPREADSHEET_THEN_BASIC) {
                        tables = bea.extract(areaPage);
                    }

                    if (tables == null || tables.isEmpty()) continue;

                    // 4) 중복 제거
                    tables = dedupByRegion(tables);

                    // 5) 저장 + 자산 생성
                    for (Table t : tables) {
                        String csv = TabulaMapper.toCSV(t);

                        // 완전 빈/무의미한 결과만 제거 (임계값 난사 X)
                        if (!hasAnyMeaningfulCell(csv)) continue;

                        String fileName = "p" + pageIdx + "_t" + (tableIdx++) + ".csv";
                        Path filePath = baseDir.resolve(fileName);
                        Files.writeString(filePath, csv, StandardCharsets.UTF_8);

                        // region은 "탐지된 area" 기준으로 저장 (표 영역에 더 의미 있음)
                        TableRegion region = TableRegion.builder()
                                .x((float) area.getLeft())
                                .y((float) area.getTop())
                                .width((float) area.getWidth())
                                .height((float) area.getHeight())
                                .build();

                        results.add(TableAsset.builder()
                                .paperId(ref.getPaperId())
                                .pageNumber(pageIdx)
                                .sectionOrder(0)
                                .tablePath(filePath.toString())
                                .region(region)
                                .build());
                    }
                }

                pageIdx++;
            }

            return results;

        } catch (IOException e) {
            throw new DomainException(
                    "PDF_TABLE_READ_FAILED for paperId=" + ref.getPaperId() + ": " + e.getMessage(), e
            );
        }
    }

    private static Mode parseMode(String raw) {
        try {
            return Mode.valueOf(raw == null ? "SPREADSHEET_ONLY" : raw.trim());
        } catch (Exception ignore) {
            return Mode.SPREADSHEET_ONLY;
        }
    }

    /**
     * 같은 영역(region)을 여러 번 반환하는 Tabula 중복 제거.
     */
    private static List<Table> dedupByRegion(List<Table> tables) {
        record Key(int x, int y, int w, int h) {}
        Set<Key> seen = new HashSet<>();
        List<Table> out = new ArrayList<>();

        for (Table t : tables) {
            Key k = new Key(
                    Math.round((float) t.getX()),
                    Math.round((float) t.getY()),
                    Math.round((float) t.getWidth()),
                    Math.round((float) t.getHeight())
            );
            if (seen.add(k)) out.add(t);
        }
        return out;
    }

    /**
     * “완전 빈/무의미”만 방지하는 최소 안전장치
     */
    private static boolean hasAnyMeaningfulCell(String csv) {
        if (csv == null) return false;
        String s = csv.trim();
        if (s.isEmpty()) return false;

        String stripped = s.replace(",", "")
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim();

        return stripped.length() >= 10;
    }
}
