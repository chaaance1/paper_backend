package dddan.paper_summary.arxiv.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 논문 섹션 엔티티
 * - 하나의 논문은 여러 개의 섹션으로 구성됨
 * - 각 섹션은 텍스트, 이미지, 표, 수식으로 구성
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sectionId;         // 논문 내 섹션 순번 (예: 1, 2, 3...)

    private String title;              // 섹션 제목 (예: Introduction, Method)

    private String tableOfContents;    // TOC 상 제목 (필요한 경우)

    @Column(columnDefinition = "TEXT")
    private String text;               // 섹션 본문 텍스트

    @ElementCollection
    @CollectionTable(name = "section_images", joinColumns = @JoinColumn(name = "section_id"))
    private List<String> images;       // 이미지 설명 텍스트

    @ElementCollection
    @CollectionTable(name = "section_tables", joinColumns = @JoinColumn(name = "section_id"))
    private List<String> tables;       // 표 설명 텍스트

    @ElementCollection
    @CollectionTable(name = "section_equations", joinColumns = @JoinColumn(name = "section_id"))
    private List<String> equations;    // 수식 설명 텍스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;               // 이 섹션이 속한 논문
}
