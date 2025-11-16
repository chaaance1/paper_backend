package dddan.paper_summary.parse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParseRequestDto {
    private Long paperId;     // 선택: DB 연동용
    private String fileName;    // 업로드 파일명
}
