package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.Paper;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Integer> {
    Optional<Paper> findByArxivId(String arxivId);
}
