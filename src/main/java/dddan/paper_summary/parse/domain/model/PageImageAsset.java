// PageImageAsset.java
package dddan.paper_summary.parse.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageImageAsset {
    private final Long paperId;
    private final int pageNumber;      // 1-based
    private final String localPath;    // or storageUrl
}
