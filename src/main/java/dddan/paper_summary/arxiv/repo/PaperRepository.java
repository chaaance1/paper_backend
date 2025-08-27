package dddan.paper_summary.arxiv.repo;

import dddan.paper_summary.arxiv.domain.Paper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaperRepository extends JpaRepository<Paper, Long> {
    Optional<Paper> findByArxivId(String arxivId);
}
