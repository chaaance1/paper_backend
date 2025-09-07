package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    boolean existsByPaperIdAndSectionOrderAndImagePath(Integer paperId, Integer sectionOrder, String imagePath);

    // 섹션별 조회
    List<ImageAsset> findByPaperIdAndSectionOrderOrderByIdAsc(Integer paperId, Integer sectionOrder);
}
