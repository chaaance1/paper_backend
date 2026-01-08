package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.TableExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.TableAsset;
import dddan.paper_summary.parse.domain.model.TableRegion;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.PageIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * PdfBox + Tabula 기반 PDF 표(Table) 추출 구현체
 * - PDF 파일에서 표를 탐지
 * - 각 표를 CSV 파일로 저장
 * - 표의 위치 정보(좌표)를 포함한 TableAsset 생성
 */
@Component
public class PdfboxTableExtractor implements TableExtractor {

    /**
     * PDF에서 표를 추출하여 TableAsset 목록으로 반환
     *
     * @param ref 논문 참조 정보 (paperId, 로컬 PDF 경로 포함)
     * @return 추출된 표 메타데이터 목록
     * @throws DomainException PDF 처리 중 오류 발생 시
     */
    @Override
    @SuppressWarnings("DuplicatedCode")
    public List<TableAsset> extract(PaperRef ref) throws DomainException {
        File pdf = PdfboxCommon.requireExistingPdf(ref);
        List<TableAsset> results = new ArrayList<>();

        try (PDDocument doc = PdfboxCommon.open(pdf);
             ObjectExtractor oe = PdfboxCommon.extractor(doc)) {

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();

            // PDF당 한 번만 생성
            Path baseDir = Paths.get("tmp", "tables", String.valueOf(ref.getPaperId()));
            Files.createDirectories(baseDir);

            int pageIdx = 1;
            int tableIdx = 1;

            PageIterator it = oe.extract();
            while (it.hasNext()) {
                Page page = it.next();

                List<Table> tables = sea.extract(page);
                if (tables == null || tables.isEmpty()) {
                    tables = bea.extract(page);
                }

                for (Table t : tables) {
                    String csv = TabulaMapper.toCSV(t);

                    String fileName = "p" + pageIdx + "_t" + (tableIdx++) + ".csv";
                    Path filePath = baseDir.resolve(fileName);

                    Files.writeString(filePath, csv, StandardCharsets.UTF_8);

                    // Tabula Table은 Rectangle 상속 → getX/getY/getWidth/getHeight 사용
                    TableRegion region = new TableRegion(
                            (float) t.getX(),
                            (float) t.getY(),
                            (float) t.getWidth(),
                            (float) t.getHeight()
                    );

                    results.add(
                            TableAsset.builder()
                                    .paperId(ref.getPaperId())
                                    .pageNumber(pageIdx)
                                    .sectionOrder(0)              // 섹션 매핑 추후 처리
                                    .tablePath(filePath.toString())
                                    .region(region)               // 좌표 세팅
                                    .build()
                    );
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
}