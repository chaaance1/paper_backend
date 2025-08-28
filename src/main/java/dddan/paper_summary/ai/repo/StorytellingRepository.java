package dddan.paper_summary.ai.repo;

import dddan.paper_summary.ai.domain.Storytelling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorytellingRepository extends JpaRepository<Storytelling, Long> {
    // 저장용이므로 커스텀 메서드 없음
}
