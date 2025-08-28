package dddan.paper_summary.arxiv.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "papers", indexes = {
        @Index(name = "ux_papers_arxiv_id", columnList = "arxiv_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arxiv_id", nullable = false, unique = true)
    private String arxivId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authors;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "arxiv_updated_date")
    private LocalDate updatedDate;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

}
