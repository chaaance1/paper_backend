package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TableResultDto {
    private int index;          // 0-based
    private String csvPath;     // 저장 경로 또는 URL(없으면 빈 문자열)
}
