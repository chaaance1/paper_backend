package dddan.paper_summary.ai.mapper;

import dddan.paper_summary.ai.dto.AiRequestDto;
import dddan.paper_summary.ai.dto.AiSectionRequest;
import dddan.paper_summary.parse.dto.SectionResultDto;
import dddan.paper_summary.parse.dto.TableResultDto;
import dddan.paper_summary.parse.dto.FigureResultDto;

import java.util.*;
import java.util.stream.Collectors;

public final class AiRequestMapper {

    private AiRequestMapper() {
    }

    /**
     * Parse 결과 + 메타데이터 → AI 요청용 JSON DTO로 변환
     */
    public static AiRequestDto toAiRequest(
            String paperId,
            String paperTitle,
            String tableOfContents,
            List<String> formulaPages,                // 페이지 PNG URL 리스트
            List<SectionResultDto> sectionResults,    // 섹션 텍스트
            List<TableResultDto> tableResults,        // 표 (sectionOrder 포함)
            List<FigureResultDto> figureResults       // 그림 (sectionOrder 포함)
    ) {
        // null 방어
        List<SectionResultDto> safeSections =
                sectionResults != null ? sectionResults : Collections.emptyList();
        List<TableResultDto> safeTables =
                tableResults != null ? tableResults : Collections.emptyList();
        List<FigureResultDto> safeFigures =
                figureResults != null ? figureResults : Collections.emptyList();
        List<String> safeFormulaPages =
                formulaPages != null ? formulaPages : Collections.emptyList();

        // 1) 섹션별 tables, figures 매핑
        Map<Integer, List<String>> tablesBySection = safeTables.stream()
                .collect(Collectors.groupingBy(
                        TableResultDto::getSectionOrder,
                        Collectors.mapping(TableResultDto::getTablePath, Collectors.toList())
                ));

        Map<Integer, List<String>> figuresBySection = safeFigures.stream()
                .collect(Collectors.groupingBy(
                        FigureResultDto::getSectionOrder,
                        Collectors.mapping(FigureResultDto::getImagePath, Collectors.toList())
                ));

        // 2) 섹션별 payload 만들기
        List<AiSectionRequest> sectionPayloads = safeSections.stream()
                .map(sec -> {
                    int sectionId = sec.getSectionOrder();

                    List<String> sectionTables =
                            tablesBySection.getOrDefault(sectionId, Collections.emptyList());
                    List<String> sectionFigures =
                            figuresBySection.getOrDefault(sectionId, Collections.emptyList());

                    return AiSectionRequest.builder()
                            .sectionId(sectionId)              // JSON: section_id
                            .sectionTitle(sec.getTitle())      // JSON: section_title
                            .text(sec.getContent())            // JSON: text
                            .tables(sectionTables)             // JSON: tables
                            .figures(sectionFigures)           // JSON: figures
                            .build();
                })
                .toList();

        // 3) 최종 요청 DTO
        return AiRequestDto.builder()
                .paperId(paperId)
                .paperTitle(paperTitle)
                .tableOfContents(tableOfContents)
                .formulaPages(safeFormulaPages)
                .sections(sectionPayloads)
                .build();
    }
}
