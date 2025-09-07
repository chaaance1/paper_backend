// dddan.paper_summary.storage.entity.ImageAsset
package dddan.paper_summary.storage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "figures")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class ImageAsset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // Paper.id FK를 정식 FK로 연결하려면 @ManyToOne 매핑해도 됨
    private Integer paperId;

    @Column(nullable = false)
    private Integer sectionOrder;

    @Column(nullable = false, length = 1000)
    private String imagePath;      // 스토리지의 퍼블릭 URL

    @Column(length = 400)
    private String originalName;   // 원본 파일명(옵션)

    private LocalDateTime createdAt;
}
