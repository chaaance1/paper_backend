// dddan.paper_summary.storage.entity.TableAsset
package dddan.paper_summary.storage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tables")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class TableAsset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)       // Paper.id (FK로 바꾸고 싶으면 ManyToOne으로 확장 가능)
    private Integer paperId;

    @Column(nullable = false)
    private Integer sectionOrder;

    @Column(nullable = false, length = 1000)
    private String tablePath;       // 스토리지 퍼블릭 URL (CSV)

    @Column(length = 600)
    private String storageKey;      // 스토리지 키(옵션: 도메인 바뀌어도 복구 쉬움)

    @Column(length = 400)
    private String originalName;    // 원본 파일명

    private LocalDateTime createdAt;

}
