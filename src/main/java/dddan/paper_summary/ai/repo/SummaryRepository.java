package dddan.paper_summary.ai.repo;

import dddan.paper_summary.ai.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    // 저장용이므로 커스텀 메서드 없음
}
