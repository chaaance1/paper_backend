package dddan.paper_summary.parse.domain;

import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.ParseResult;
import dddan.paper_summary.parse.domain.error.DomainException;

/**
 * 전체 PDF 파싱 유스케이스의 도메인 진입점.
 * 컨트롤러(API)나 배치/큐 컨슈머 등 애플리케이션 계층은
 * 이 인터페이스만 호출하여 텍스트/표/그림/수식 추출을 오케스트레이션한다.
 *
 * 기술/라이브러리(PDFBox, Tabula, OCR, JPA 등) 의존성을 포함하지 않는다.
 */
public interface ParseUseCase {

    /**
     * 주어진 PDF 참조(PaperRef)를 기반으로 파싱을 수행한다.
     *
     * @param ref 파싱 대상 PDF에 대한 참조(식별자, 파일명, 크기, 입력 스트림 공급자 등)
     * @return 추출된 텍스트/표/그림/수식 및 메타데이터를 담은 결과
     * @throws DomainException 도메인 규칙 위반(입력 불량, 지원 불가 포맷 등) 시
     */
    ParseResult parse(PaperRef ref) throws DomainException;
}
