package dddan.paper_summary.ai.repo;

import dddan.paper_summary.ai.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {

    // 기존: List<Summary> findAllByPaperIdAndRequestId(Long paperId, UUID requestId);

    // 수정: 논문 ID + 사용자 ID로 조회
    List<Summary> findAllByPaperIdAndUserId(Long paperId, String userId);

    // 필요하다면 섹션 단위 조회도 가능
    List<Summary> findAllByPaperIdAndUserIdAndSectionOrder(Long paperId, String userId, int sectionOrder);
}
