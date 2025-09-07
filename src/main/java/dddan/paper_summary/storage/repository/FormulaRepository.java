package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.Formula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    boolean existsByPaperIdAndSectionOrderAndFormulaPath(Integer paperId, Integer sectionOrder, String formulaPath);
    List<Formula> findAllByPaperIdAndSectionOrderOrderByIdAsc(Integer paperId, Integer sectionOrder);
}