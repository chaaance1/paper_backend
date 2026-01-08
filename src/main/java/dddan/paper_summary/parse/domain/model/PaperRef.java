package dddan.paper_summary.parse.domain.model;

import lombok.*;

/**
 * 파싱 대상이 되는 "논문 PDF에 대한 참조 정보"를 표현하는 도메인 모델
 * - 실제 PDF 파일 자체가 아니라, 파일에 접근하기 위한 메타정보를 관리한다
 * - 로컬 파일, 업로드 파일, 외부 다운로드(arXiv 등) 등
 *   다양한 입력 소스를 동일한 방식으로 처리하기 위한 추상화 객체
 * - ParseUseCase 및 각 Extractor(Text / Table / Figure / Formula)의 공통 입력으로 사용된다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaperRef {
    private Long paperId;
    private String filename;
    private InputStreamSupplier inputStreamSupplier; // 파일은 필요 시에만 오픈(지연)
    private String localPath; // 로컬 파일 경로
}
