package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.model.FigureAsset;
import dddan.paper_summary.parse.domain.model.TableAsset;

import java.util.*;
import java.util.stream.Collectors;

/**
 * startPageMap(sectionOrder -> startPageNumber)을 이용해서
 * Table/Figure의 pageNumber를 기준으로 sectionOrder를 채운다.
 * 규칙(핵심):
 * - 자산(pageNumber)보다 "이전에 시작한" 가장 최근 섹션에 귀속한다.
 *   즉, startPage <= asset.pageNumber 인 섹션 중 startPage가 가장 큰 섹션을 선택한다.
 * 전제:
 * - pageNumber는 1-based로 통일
 * - startPageMap의 pageNumber도 1-based
 */
public final class SectionOrderFiller {

    private SectionOrderFiller() {}

    /**
     * Table/Figure에 sectionOrder를 채운 새 리스트를 반환한다. (원본 불변 가정)
     *
     * @param startPageMap sectionOrder -> startPageNumber
     * @param tables       추출된 tables (pageNumber는 채워져 있어야 함)
     * @param figures      추출된 figures (pageNumber는 채워져 있어야 함)
     */
    public static Result fill(
            Map<Integer, Integer> startPageMap,
            List<TableAsset> tables,
            List<FigureAsset> figures
    ) {
        NavigableMap<Integer, Integer> startPageToSection =
                toStartPageToSectionMap(startPageMap);

        List<TableAsset> mappedTables = (tables == null) ? List.of()
                : tables.stream()
                .map(t -> copyWithSectionOrder(t, resolve(startPageToSection, t.getPageNumber())))
                .toList();

        List<FigureAsset> mappedFigures = (figures == null) ? List.of()
                : figures.stream()
                .map(f -> copyWithSectionOrder(f, resolve(startPageToSection, f.getPageNumber())))
                .toList();

        return new Result(mappedTables, mappedFigures);
    }

    /** 결과 래퍼 */
    public static final class Result {
        private final List<TableAsset> tables;
        private final List<FigureAsset> figures;

        public Result(List<TableAsset> tables, List<FigureAsset> figures) {
            this.tables = tables;
            this.figures = figures;
        }

        public List<TableAsset> getTables() { return tables; }
        public List<FigureAsset> getFigures() { return figures; }
    }

    // -----------------------------
    // 핵심 매핑 로직
    // -----------------------------

    /**
     * 자산 페이지(assetPage)보다 "이전에 시작한" 가장 최근 섹션을 찾는다.
     * - startPage <= assetPage 인 섹션 중 startPage가 가장 큰 섹션
     */
    private static int resolve(NavigableMap<Integer, Integer> startPageToSection, int assetPage) {
        if (assetPage <= 0) return 0;
        if (startPageToSection == null || startPageToSection.isEmpty()) return 0;

        Map.Entry<Integer, Integer> e = startPageToSection.floorEntry(assetPage);
        if (e == null) {
            // assetPage보다 작거나 같은 startPage가 하나도 없으면 -> 0(미매핑) or 1(첫 섹션) 중 정책 선택
            return 0;
        }
        return e.getValue();
    }

    /**
     * startPageMap(sectionOrder -> startPage)을
     * startPage -> sectionOrder 로 뒤집어서 NavigableMap으로 만든다.
     * 주의:
     * - 서로 다른 섹션이 같은 startPage를 가질 수 없다고 가정(정상 PDF라면 거의 없음).
     * - 만약 충돌하면 "더 큰 sectionOrder"를 우선하도록 처리.
     */
    private static NavigableMap<Integer, Integer> toStartPageToSectionMap(Map<Integer, Integer> startPageMap) {
        if (startPageMap == null || startPageMap.isEmpty()) {
            return new TreeMap<>();
        }

        // (startPage -> sectionOrder)
        TreeMap<Integer, Integer> map = new TreeMap<>();

        for (Map.Entry<Integer, Integer> e : startPageMap.entrySet()) {
            Integer sectionOrder = e.getKey();
            Integer startPage = e.getValue();
            if (sectionOrder == null || startPage == null) continue;
            if (sectionOrder <= 0 || startPage <= 0) continue;

            map.merge(startPage, sectionOrder, Math::max);
        }

        return map;
    }

    // -----------------------------
    // 복사(불변 Asset 가정) - 너희 Builder 필드에 맞춰 조정
    // -----------------------------

    private static TableAsset copyWithSectionOrder(TableAsset t, int sectionOrder) {
        if (t == null) return null;

        return TableAsset.builder()
                .paperId(t.getPaperId())
                .pageNumber(t.getPageNumber())
                .sectionOrder(sectionOrder)
                .tablePath(t.getTablePath())
                .region(t.getRegion())
                .build();
    }

    private static FigureAsset copyWithSectionOrder(FigureAsset f, int sectionOrder) {
        if (f == null) return null;

        return FigureAsset.builder()
                .paperId(f.getPaperId())
                .pageNumber(f.getPageNumber())
                .sectionOrder(sectionOrder)
                .imagePath(f.getImagePath())
                .build();
    }

    // -----------------------------
    // (선택) 디버깅 헬퍼
    // -----------------------------

    /**
     * 디버깅용: startPageMap을 보기 좋게 출력
     */
    public static String debugStartPageMap(Map<Integer, Integer> startPageMap) {
        if (startPageMap == null || startPageMap.isEmpty()) return "(empty)";
        return startPageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> "section " + e.getKey() + " starts at page " + e.getValue())
                .collect(Collectors.joining("\n"));
    }
}
