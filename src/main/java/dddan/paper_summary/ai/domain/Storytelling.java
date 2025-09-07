package dddan.paper_summary.ai.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "storytelling")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Storytelling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "step", nullable = false)
    private Integer step;  // 1 이상 정수

    @Column(name = "heading")
    private String heading;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

}
