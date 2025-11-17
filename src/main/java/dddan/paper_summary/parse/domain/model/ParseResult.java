package dddan.paper_summary.parse.domain.model;

import dddan.paper_summary.parse.dto.*;
import lombok.*;

import java.util.List;
import java.util.stream.IntStream;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult {
    private Long paperId;
    private TextResultDto text;
    private List<TableResultDto> tables;
    private List<FigureResultDto> figures;
    private List<EquationResultDto> equations;

    public static ParseResult empty(Long paperId) {
        return ParseResult.builder()
                .paperId(paperId)
                .text(null)
                .tables(List.of())
                .figures(List.of())
                .equations(List.of())
                .build();
    }
    public static ParseResult success(Long paperId,
                                      TextAsset textAsset,
                                      List<TableAsset> tableAssets) {

        // TextAsset → TextResultDto
        // ⬇⬇⬇ TextAsset 안의 실제 getter 이름에 맞게 getFullText() 부분만 바꿔줘 ⬇⬇⬇
        TextResultDto textDto = null;
        if (textAsset != null) {
            textDto = TextResultDto.builder()
                    .fullText(textAsset.getText()) // TODO: getText(), getContent() 등 실제 이름으로 수정
                    .pageCount(0)                      // 페이지 수 아직 안 쓰면 0으로 둬도 됨
                    .build();
        }

        // TableAsset 리스트 → TableResultDto 리스트
        // 일단 index만 채우고, csvPath는 나중에 파일 저장 로직 붙일 때 채우자
        List<TableResultDto> tableDtos = (tableAssets != null)
                ? IntStream.range(0, tableAssets.size())
                .mapToObj(i -> TableResultDto.builder()
                        .index(i)
                        .csvPath("")      // TODO: TableAsset에 경로 있으면 거기서 꺼내기
                        .build())
                .toList()
                : List.of();

        return ParseResult.builder()
                .paperId(paperId)
                .text(textDto)
                .tables(tableDtos)
                .figures(List.of())
                .equations(List.of())
                .build();
    }

    // 파싱 실패용 팩토리 메서드
    public static ParseResult error(Long paperId, String message) {
        TextResultDto textDto = TextResultDto.builder()
                .fullText(message)  // 에러 메시지를 fullText에 넣어두기 (원하면 null로 해도 됨)
                .pageCount(0)
                .build();

        return ParseResult.builder()
                .paperId(paperId)
                .text(textDto)
                .tables(List.of())
                .figures(List.of())
                .equations(List.of())
                .build();
    }
}

