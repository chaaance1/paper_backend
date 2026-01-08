package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.SectionTextExtractor;
import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.model.SectionAsset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PDF에서 논문 전체 텍스트를 추출한 뒤,
 * 이를 섹션 단위(Introduction, Method, Conclusion 등)로 분리하는 클래스
 * - infra 계층 구현체
 * - PDFBox 기반 TextExtractor 결과를 사용
 * - 실제 PDF 파싱 로직은 직접 담당하지 않고, 섹션 분리 역할에만 집중
 */
@Component
@RequiredArgsConstructor
public class PdfboxSectionTextExtractor implements SectionTextExtractor {

    private final TextExtractor textExtractor;

    /**
     * 논문(PaperRef)을 입력으로 받아
     * 섹션 단위 텍스트 자산(SectionAsset) 목록을 생성한다.
     *
     * @param ref 논문 식별 정보 및 PDF 경로를 담은 객체
     * @return 섹션 단위로 분리된 텍스트 자산 리스트
     * @throws DomainException PDF 파싱 또는 텍스트 처리 중 오류 발생 시
     */

    @Override
    public List<SectionAsset> extract(PaperRef ref) throws DomainException {
        // 1) 먼저 전체 텍스트 추출
        var textAsset = textExtractor.extract(ref);

        // 2) fullText 기준으로 섹션 나누기
        return SectionSplitter.split(
                ref.getPaperId(),
                textAsset.getFullText()
        );
    }
}
