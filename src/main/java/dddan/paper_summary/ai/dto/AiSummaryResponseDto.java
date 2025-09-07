package dddan.paper_summary.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AiSummaryResponseDto {

    @JsonProperty("text_result")
    private String textResult;

    // 배열 자리에 단일 문자열이 와도 허용 + null이면 빈 리스트로
    @JsonProperty("image_results")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> imageResults;

    @JsonProperty("table_results")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> tableResults;

    @JsonProperty("equation_results")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> equationResults;

    // 서버가 에러 메시지를 별도 필드로 줄 수도 있어 대비(선택)
    private String error;
}
