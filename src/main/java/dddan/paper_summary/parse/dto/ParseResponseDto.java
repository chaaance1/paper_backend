package dddan.paper_summary.parse.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParseResponseDto {
    private String paperId;
    private TextResultDto text;
    private List<TableResultDto> tables;
    private List<FigureResultDto> figures;
    private List<EquationResultDto> equations;
}
