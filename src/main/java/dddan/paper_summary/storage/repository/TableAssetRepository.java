// dddan.paper_summary.storage.repository.TableAssetRepository
package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.TableAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableAssetRepository extends JpaRepository<TableAsset, Long> {
    boolean existsByPaperIdAndSectionOrderAndTablePath(Integer paperId, Integer sectionOrder, String tablePath);
    List<TableAsset> findByPaperIdAndSectionOrderOrderByIdAsc(Integer paperId, Integer sectionOrder);
}
