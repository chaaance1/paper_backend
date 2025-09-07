package dddan.paper_summary.storage.repository;

import dddan.paper_summary.storage.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    // 특정 논문의 모든 섹션을 섹션 순서대로 정렬해서 가져오기
    List<Section> findByPaperIdOrderBySectionOrderAsc(Integer paperId);
}
