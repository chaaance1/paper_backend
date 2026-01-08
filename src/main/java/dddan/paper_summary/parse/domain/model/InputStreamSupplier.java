package dddan.paper_summary.parse.domain.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * PDF 파일을 InputStream 형태로 지연(lazy) 제공하기 위한 함수형 인터페이스
 * - 실제 파일 스트림을 즉시 열지 않고,
 *   필요 시점에만 InputStream을 생성하도록 추상화한다
 * - 로컬 파일, 업로드 파일(MultipartFile),
 *   외부 스토리지(S3 등) 등 다양한 입력 소스를
 *   동일한 방식으로 다루기 위해 사용된다
 * - PaperRef에서 PDF 접근 전략을 분리하기 위한 핵심 인터페이스
 */
@FunctionalInterface
public interface InputStreamSupplier {
    InputStream get() throws IOException;
}
