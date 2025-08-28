package dddan.paper_summary.storage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "formulas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"paper_id", "section_order", "formula_path"})
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Formula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paper_id", nullable = false)
    private Integer paperId;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "formula_path", nullable = false, columnDefinition = "TEXT")
    private String formulaPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
