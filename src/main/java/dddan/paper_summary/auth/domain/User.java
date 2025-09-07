package dddan.paper_summary.auth.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")  // PostgreSQL 테이블명
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 20, nullable = false, unique = true)
    private String userId;

    @Column(name = "user_pwd", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

}
