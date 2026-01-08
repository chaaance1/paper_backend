package dddan.paper_summary.parse.dto;

import lombok.*;
import java.util.List;

/**
 * PDF 파싱 요청에 대한 응답 DTO
 * - 서버가 PDF 파싱을 수행한 뒤 클라이언트에게 반환하는 최종 결과 객체
 * - 텍스트, 표, 그림 등 파싱된 자산들을 구조화하여 포함한다
 * - API 응답(JSON) 및 AI 요청 데이터의 기반이 되는 DTO
 */
@Getter
@Builder
public class ParseResponseDto {
    private String paperId;
    private TextResultDto text;
    private List<TableResultDto> tables;
    private List<FigureResultDto> figures;
}

