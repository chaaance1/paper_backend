package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableAsset {
    private int pageNumber;
    private String csv;

    public static TableAsset of(int pageNumber, String csv) {
        return TableAsset.builder()
                .pageNumber(pageNumber)
                .csv(csv)
                .build();
    }
}
