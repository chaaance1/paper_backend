package dddan.paper_summary.parse;

import dddan.paper_summary.ai.client.AiClient;
import dddan.paper_summary.ai.dto.AiFullTextRequest;
import dddan.paper_summary.ai.dto.AiRequestDto;
import dddan.paper_summary.ai.mapper.AiFullTextMapper;
import dddan.paper_summary.ai.mapper.AiRequestMapper;
import dddan.paper_summary.arxiv.dto.ArxivPaperDto;

import dddan.paper_summary.parse.domain.*;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.*;
import dddan.paper_summary.parse.dto.SectionResultDto;

import dddan.paper_summary.parse.infra.SectionOrderFiller;
import dddan.paper_summary.parse.infra.SectionStartPageDetector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 논문 PDF 파싱을 담당하는 Application Service
 * policy:
 * - ParseService는 저장소 구현(GCS 등)을 몰라야 한다.
 * - 업로드/URL 생성은 AssetStorage(store)가 담당한다.
 * - ParseResult에는 외부 접근 가능한 URL이 들어간다(버킷 public 전제).
 */
@Service
@RequiredArgsConstructor
public class ParseService implements ParseUseCase {

    private static final Logger log = LoggerFactory.getLogger(ParseService.class);

    private final AssetStorage assetStorage;          // 포트만 사용
    private final TextExtractor textExtractor;
    private final TableExtractor tableExtractor;
    private final FigureExtractor figureExtractor;
    private final SectionTextExtractor sectionTextExtractor;

    private final AiFullTextMapper aiFullTextMapper;
    private final AiClient aiClient;

    @Override
    public ParseResult parse(PaperRef ref) {
        try {
            // 1) 추출 (tablePath/imagePath는 우선 로컬 경로)
            TextAsset text = textExtractor.extract(ref);
            List<SectionAsset> sections = sectionTextExtractor.extract(ref);
            List<TableAsset> tables = tableExtractor.extract(ref);
            List<FigureAsset> figures = figureExtractor.extract(ref);

            log.info("[PARSE] paperId={} extracted: pages={}, sections={}, tables={}, figures={}",
                    ref.getPaperId(),
                    (text != null ? text.getPageCount() : -1),
                    (sections != null ? sections.size() : 0),
                    (tables != null ? tables.size() : 0),
                    (figures != null ? figures.size() : 0)
            );

            // 2) 섹션 시작 페이지 탐지
            Map<Integer, Integer> startPageMap = Map.of();
            if (text != null && text.getPageTexts() != null && !text.getPageTexts().isEmpty()
                    && sections != null && !sections.isEmpty()) {
                startPageMap = SectionStartPageDetector.detect(text.getPageTexts(), sections);
            }

            if (!startPageMap.isEmpty()) {
                String mapStr = startPageMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> "sec" + e.getKey() + "->p" + e.getValue())
                        .collect(Collectors.joining(", "));
                log.info("[MAP] paperId={} sectionStartPages: {}", ref.getPaperId(), mapStr);
            } else {
                log.warn("[MAP] paperId={} sectionStartPages: (empty)", ref.getPaperId());
            }

            // 3) tables/figures에 sectionOrder 채우기
            SectionOrderFiller.Result mapped = SectionOrderFiller.fill(startPageMap, tables, figures);
            List<TableAsset> mappedTables = mapped.getTables();
            List<FigureAsset> mappedFigures = mapped.getFigures();

            // ---- DEBUG: 매핑 결과 샘플 ----
            if (mappedTables != null && !mappedTables.isEmpty()) {
                mappedTables.stream().limit(5).forEach(t ->
                        log.info("[MAP][TABLE] paperId={} page={} -> sectionOrder={} path={}",
                                ref.getPaperId(), t.getPageNumber(), t.getSectionOrder(), safeTail(t.getTablePath()))
                );
            }
            if (mappedFigures != null && !mappedFigures.isEmpty()) {
                mappedFigures.stream().limit(5).forEach(f ->
                        log.info("[MAP][FIG] paperId={} page={} -> sectionOrder={} path={}",
                                ref.getPaperId(), f.getPageNumber(), f.getSectionOrder(), safeTail(f.getImagePath()))
                );
            }

            // ---- DEBUG: 미매핑(0) 카운트 ----
            int unmappedTables = (mappedTables == null) ? 0 :
                    (int) mappedTables.stream().filter(t -> t.getSectionOrder() == 0).count();
            int unmappedFigures = (mappedFigures == null) ? 0 :
                    (int) mappedFigures.stream().filter(f -> f.getSectionOrder() == 0).count();

            if (unmappedTables > 0 || unmappedFigures > 0) {
                log.warn("[MAP] paperId={} unmapped: tables(sectionOrder=0)={}, figures(sectionOrder=0)={}",
                        ref.getPaperId(), unmappedTables, unmappedFigures);
            } else {
                log.info("[MAP] paperId={} unmapped: none", ref.getPaperId());
            }

            // 4) 업로드 후 URL로 치환 ( 저장/URL 생성은 assetStorage.store()가 담당)
            String paperIdForStorage = buildPaperIdForStorage(ref); // "tmp" or actual
            List<TableAsset> uploadedTables = uploadTables(ref, paperIdForStorage, mappedTables);
            List<FigureAsset> uploadedFigures = uploadFigures(ref, paperIdForStorage, mappedFigures);

            // 5) 최종 결과 (JSON에는 URL이 들어감)
            return ParseResult.success(
                    ref.getPaperId(),
                    text,
                    sections,
                    uploadedTables,
                    uploadedFigures
            );

        } catch (DomainException e) {
            log.error("[PARSE] paperId={} failed: {}", ref.getPaperId(), e.getMessage(), e);
            return ParseResult.error(ref.getPaperId(), e.getMessage());
        }
    }

    public void parseAndSendToAI(ArxivPaperDto paper,
                                 List<String> formulaPageUrls,
                                 PaperRef ref) {

        ParseResult result = parse(ref);
        if (!result.isSuccess()) return;

        String paperId = paper.getArxivId();
        String title = paper.getTitle();

        String toc = result.getSections().stream()
                .map(SectionResultDto::getTitle)
                .collect(Collectors.joining("\n"));

        AiRequestDto sectionRequest = AiRequestMapper.toAiRequest(
                paperId,
                title,
                toc,
                formulaPageUrls,
                result.getSections(),
                result.getTables(),
                result.getFigures()
        );

        AiFullTextRequest fullTextRequest = aiFullTextMapper.toFullText(paper, result);

        aiClient.sendSectionRequest(sectionRequest);
        aiClient.sendFullTextRequest(fullTextRequest);
    }

    // =========================
    // Upload helpers (AssetStorage.store() only)
    // =========================

    private List<TableAsset> uploadTables(PaperRef ref, String paperIdForStorage, List<TableAsset> tables) {
        if (tables == null || tables.isEmpty()) return List.of();

        AtomicInteger seq = new AtomicInteger(1);

        return tables.stream().map(t -> {
            String localPath = t.getTablePath();
            if (localPath == null || localPath.isBlank()) return t;

            byte[] bytes = readBytes(localPath);
            int index = seq.getAndIncrement();

            // 어댑터가 paperId/index를 추출할 수 있게 힌트 포맷 고정
            String pathHint = String.format(
                    "papers/%s/tables/table-%03d.csv",
                    paperIdForStorage, index
            );

            // store()가 업로드 + public URL 반환
            String url = assetStorage.store(bytes, pathHint);

            return TableAsset.builder()
                    .paperId(ref.getPaperId())
                    .pageNumber(t.getPageNumber())
                    .sectionOrder(t.getSectionOrder())
                    .tablePath(url)          // URL
                    .region(t.getRegion())
                    .build();
        }).toList();
    }

    private List<FigureAsset> uploadFigures(PaperRef ref, String paperIdForStorage, List<FigureAsset> figures) {
        if (figures == null || figures.isEmpty()) return List.of();

        AtomicInteger seq = new AtomicInteger(1);

        return figures.stream().map(f -> {
            String localPath = f.getImagePath();
            if (localPath == null || localPath.isBlank()) return f;

            byte[] bytes = readBytes(localPath);
            int index = seq.getAndIncrement();

            // ⚠️ ObjectStorageService가 figure를 JPG로 올린다는 정책이면 hint도 jpg로 통일
            String pathHint = String.format(
                    "papers/%s/figures/figure-%03d.jpg",
                    paperIdForStorage, index
            );

            String url = assetStorage.store(bytes, pathHint);

            return FigureAsset.builder()
                    .paperId(ref.getPaperId())
                    .pageNumber(f.getPageNumber())
                    .sectionOrder(f.getSectionOrder())
                    .imagePath(url)          // URL
                    .build();
        }).toList();
    }

    private String buildPaperIdForStorage(PaperRef ref) {
        return (ref.getPaperId() != null) ? String.valueOf(ref.getPaperId()) : "tmp";
    }

    private byte[] readBytes(String localPath) {
        try {
            return Files.readAllBytes(Path.of(localPath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read local file: " + localPath, e);
        }
    }

    private static String safeTail(String path) {
        if (path == null) return "(null)";
        int max = 60;
        if (path.length() <= max) return path;
        return "..." + path.substring(path.length() - max);
    }
}
