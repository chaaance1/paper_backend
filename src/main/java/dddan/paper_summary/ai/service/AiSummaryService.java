package dddan.paper_summary.ai.service;

import dddan.paper_summary.ai.client.AiApiClient;
import dddan.paper_summary.ai.dto.*;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.domain.PaperSection;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 논문 섹션 요약 서비스
 * - 논문 ID를 기반으로 섹션별 파싱된 내용을 가져와 AI 요약을 수행
 */
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final PaperRepository paperRepository;
    private final AiApiClient aiApiClient;

    /**
     * 논문 ID를 기반으로 섹션별 요약을 수행하고 결과를 조합하여 반환
     *
     * @param requestDto paperId, userId가 포함된 요청 DTO
     * @return 전체 논문 요약 응답 DTO
     */
    public AiSummaryResponseDto summarizePaper(AiSummaryRequestDto requestDto) {
        Long paperId = requestDto.getPaperId();

        // 1. 논문 조회
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("해당 논문이 존재하지 않습니다."));

        // 2. 논문 섹션들 가져오기
        List<PaperSection> sections = paper.getSections(); // Paper 엔티티에 getSections() 있어야 함

        List<SectionSummaryDto> summaryList = new ArrayList<>();

        for (PaperSection section : sections) {
            // 3. 섹션별 요약 요청 구성
            SummaryApiRequest summaryRequest = new SummaryApiRequest(
                    section.getSectionId(),
                    section.getTableOfContents(),
                    paper.getTitle(),
                    section.getTitle(),
                    section.getText(),
                    section.getImages(),
                    section.getTables(),
                    section.getEquations()
            );

            // 4. AI에 요청 → 요약 응답 받기
            SummaryApiResponse aiResponse = aiApiClient.requestSectionSummary(summaryRequest);

            // 5. 응답을 SectionSummaryDto로 변환
            SectionSummaryDto summaryDto = new SectionSummaryDto();
            summaryDto.setSectionId(section.getSectionId());
            summaryDto.setSectionTitle(section.getTitle());
            summaryDto.setTextResult(aiResponse.getTextResult());
            summaryDto.setImageResults(aiResponse.getImageResults());
            summaryDto.setTableResults(aiResponse.getTableResults());
            summaryDto.setEquationResults(aiResponse.getEquationResults());

            summaryList.add(summaryDto);
        }

        // 6. 전체 논문 요약 응답 조립
        AiSummaryResponseDto result = new AiSummaryResponseDto();
        result.setPaperTitle(paper.getTitle());
        result.setAuthors(paper.getAuthors());
        result.setPublishedDate(paper.getPublishedDate().toString());
        result.setSectionSummaries(summaryList);

        return result;
    }
}
