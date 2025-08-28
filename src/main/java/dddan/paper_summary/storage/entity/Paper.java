package dddan.paper_summary.storage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "papers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "arxiv_id", unique = true, nullable = false)
    private String arxivId;

    // 필요한 경우 다른 필드도 추가
}
