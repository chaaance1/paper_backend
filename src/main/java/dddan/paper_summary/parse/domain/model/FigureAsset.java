package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FigureAsset {
    private String id;          // 고유 식별자
    private String caption;     // 그림 캡션
    private String imagePath;   // 저장된 이미지 경로 (스토리지용)
}


