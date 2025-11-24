package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRegion {
    private float x;
    private float y;
    private float width;
    private float height;
}
