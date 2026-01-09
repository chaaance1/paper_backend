package dddan.paper_summary.parse.domain.model;

import dddan.paper_summary.parse.dto.*;
import lombok.*;

import java.util.List;
import java.util.stream.IntStream;

/**
 * PDF 파싱 전체 결과를 표현하는 도메인 결과 객체
 * - ParseUseCase의 최종 반환 타입
 * - 성공/실패 여부 + 파싱 산출물(Text / Section / Table / Figure)을 함께 포함
 * - 컨트롤러, AI 요청, 로그 등에서 공통으로 사용됨
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult {

    private Long paperId;


    private boolean success;           // 성공 여부
    private String errorMessage;       // 실패 시 메시지


    private TextResultDto text;
    private List<SectionResultDto> sections;
    private List<TableResultDto> tables;
    private List<FigureResultDto> figures;



    public boolean isSuccess() {
        return success;
    }


    public static ParseResult empty(Long paperId) {
        return ParseResult.builder()
                .paperId(paperId)
                .success(true)
                .errorMessage(null)
                .text(null)
                .sections(List.of())
                .tables(List.of())
                .figures(List.of())
                .build();
    }

    /**
     * 풀 버전 성공 팩토리
     */
    public static ParseResult success(Long paperId,
                                      TextAsset textAsset,
                                      List<SectionAsset> sectionAssets,
                                      List<TableAsset> tableAssets,
                                      List<FigureAsset> figureAssets) {

        // 1) Text
        TextResultDto textDto = null;
        if (textAsset != null) {
            textDto = TextResultDto.builder()
                    .fullText(textAsset.getFullText())
                    .pageCount(0)
                    .build();
        }

        // 2) Sections
        List<SectionResultDto> sectionDtos = (sectionAssets != null)
                ? sectionAssets.stream()
                .map(sa -> SectionResultDto.builder()
                        .sectionOrder(sa.getSectionOrder())
                        .title(sa.getTitle())
                        .content(sa.getContent())
                        .build())
                .toList()
                : List.of();

        // 3) Tables
        List<TableResultDto> tableDtos = (tableAssets != null)
                ? IntStream.range(0, tableAssets.size())
                .mapToObj(i -> {
                    TableAsset ta = tableAssets.get(i);
                    return TableResultDto.builder()
                            .index(i)
                            .sectionOrder(ta.getSectionOrder()) // sectionOrder 있으면 반영
                            .tablePath("")                       // 필요 시 수정
                            .build();
                })
                .toList()
                : List.of();

        // 4) Figures
        List<FigureResultDto> figureDtos = (figureAssets != null)
                ? IntStream.range(0, figureAssets.size())
                .mapToObj(i -> {
                    FigureAsset fa = figureAssets.get(i);
                    return FigureResultDto.builder()
                            .index(i)
                            .sectionOrder(fa.getSectionOrder()) // sectionOrder 있으면 반영
                            .imagePath(fa.getImagePath())
                            .build();
                })
                .toList()
                : List.of();

        return ParseResult.builder()
                .paperId(paperId)
                .success(true)
                .errorMessage(null)
                .text(textDto)
                .sections(sectionDtos)
                .tables(tableDtos)
                .figures(figureDtos)
                .build();
    }

    /**
     * 예전 시그니처 호환용
     */
    public static ParseResult success(Long paperId,
                                      TextAsset textAsset,
                                      List<TableAsset> tableAssets) {
        return success(paperId, textAsset, List.of(), tableAssets, List.of());
    }

    // 파싱 실패용
    public static ParseResult error(Long paperId, String message) {
        TextResultDto textDto = TextResultDto.builder()
                .fullText(message)
                .pageCount(0)
                .build();

        return ParseResult.builder()
                .paperId(paperId)
                .success(false)
                .errorMessage(message)
                .text(textDto)
                .sections(List.of())
                .tables(List.of())
                .figures(List.of())
                .build();
    }
}
