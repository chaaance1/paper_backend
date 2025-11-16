package dddan.paper_summary.parse.controller;

import dddan.paper_summary.parse.domain.model.ParseResult;
import dddan.paper_summary.parse.dto.ParseResponseDto;
import org.springframework.stereotype.Component;

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
                .equations(result.getEquations())
                .build();
    }
}
