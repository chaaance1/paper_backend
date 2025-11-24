package dddan.paper_summary.parse.infra;

import lombok.Value;

@Value
public class TableRect {
    int pageNumber;
    float x;
    float y;
    float width;
    float height;
}
