package dddan.paper_summary.auth.repo;

import dddan.paper_summary.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserIdIgnoreCase(String userId);
    Optional<User> findByUserId(String userId);
}
