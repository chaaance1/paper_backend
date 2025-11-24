package dddan.paper_summary.parse.domain.model;

import lombok.*;
@Getter
@Builder
public class TableAsset {

    private final Long paperId;
    private int pageNumber;
    private final int sectionOrder;
    private final String tablePath;
    private TableRegion region;  // 표가 있는 좌표 영역
}
