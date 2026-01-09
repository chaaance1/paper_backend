package dddan.paper_summary.parse.controller;

import dddan.paper_summary.parse.domain.model.ParseResult;
import dddan.paper_summary.parse.dto.ParseResponseDto;
import org.springframework.stereotype.Component;

/**
 * Parse 도메인 결과(ParseResult)를
 * API 응답용 DTO(ParseResponseDto)로 변환하는 매퍼 클래스
 */

@Component
public class ParseDtoMapper {
    public ParseResponseDto toResponseDto(ParseResult result) {
        if (result == null) {
            return ParseResponseDto.builder().paperId(null).build();
        }
        return ParseResponseDto.builder()
                .paperId(result.getPaperId() != null ? result.getPaperId().toString() : null)
                .text(result.getText())
                .tables(result.getTables())
                .figures(result.getFigures())
                .build();
    }
}
