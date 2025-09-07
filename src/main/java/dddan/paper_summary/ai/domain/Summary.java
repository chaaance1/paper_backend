package dddan.paper_summary.ai.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 ID

    @Column(name = "paper_id", nullable = false)
    private Long paperId;  // 논문 고유 식별자

    @Column(name = "user_id")
    private String userId;  // 회원 ID (nullable 가능)

    @Column(name = "section_order")
    private Integer sectionOrder;  // 섹션 순서

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "summary_formula", columnDefinition = "TEXT")
    private String summaryFormula;

    @Column(name = "summary_figure", columnDefinition = "TEXT")
    private String summaryFigure;

    @Column(name = "summary_table", columnDefinition = "TEXT")
    private String summaryTable;

}
