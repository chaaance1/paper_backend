package dddan.paper_summary.parse.domain;
import java.util.Optional;
import dddan.paper_summary.parse.domain.model.ParseResult;
import dddan.paper_summary.parse.domain.error.DomainException;

/**
 * 파싱 결과를 DB 등에 저장/조회하는 포트.
 */
public interface ResultRepository {

    /**
     * 파싱 결과를 영속화한다.
     *
     * @param result 저장할 파싱 결과
     * @throws DomainException 저장 실패 시
     */
    void save(ParseResult result) throws DomainException;

    /**
     * paperId로 파싱 결과를 조회한다.
     *
     * @param paperId 논문 식별자
     * @return Optional ParseResult
     */
    Optional<ParseResult> findByPaperId(Long paperId);
}

