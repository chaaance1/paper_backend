package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.Formula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    boolean existsByPaperIdAndSectionOrderAndFormulaPath(Integer paperId, Integer sectionOrder, String formulaPath);
}