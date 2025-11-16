package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaperRef {
    private Long paperId;
    private String filename;
    private String contentType;
    private long size;
    private InputStreamSupplier inputStreamSupplier; // 파일은 필요 시에만 오픈(지연)
    private String localPath; // 로컬 파일 경로
}
