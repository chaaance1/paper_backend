package dddan.paper_summary.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dddan.paper_summary.ai.client.AiApiClient;
import dddan.paper_summary.ai.domain.Summary;
import dddan.paper_summary.ai.dto.AiSummaryRequestDto;
import dddan.paper_summary.ai.dto.AiSummaryResponseDto;
import dddan.paper_summary.ai.dto.SummaryRequestDto;
import dddan.paper_summary.ai.repo.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final AiApiClient aiApiClient;
    private final SummaryRepository summaryRepository;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public List<AiSummaryResponseDto> summarizeAllSections(SummaryRequestDto req) {
        List<AiSummaryResponseDto> results = new ArrayList<>();

        for (AiSummaryRequestDto sectionReq : req.getSections()) {
            // 방어: 섹션 요청에 paperTitle이 비어있으면 상위 값을 사용
            if (sectionReq.getPaperTitle() == null) {
                sectionReq.setPaperTitle(req.getPaperTitle());
            }

            AiSummaryResponseDto resp = aiApiClient.requestSummary(sectionReq);
            results.add(resp);

            // 회원만 저장
            if (req.getUserId() != null) {
                summaryRepository.save(
                        Summary.builder()
                                .paperId(req.getPaperId())
                                .userId(req.getUserId())
                                .sectionOrder(resp.getSectionId())
                                .summaryText(resp.getTextResult())
                                .summaryFigure(toJson(resp.getImageResults()))
                                .summaryTable(toJson(resp.getTableResults()))
                                .summaryFormula(toJson(resp.getEquationResults()))
                                .build()
                );
            }
        }
        return results;
    }

    private String toJson(List<String> v) {
        try { return om.writeValueAsString(v == null ? List.of() : v); }
        catch (Exception e) { return "[]"; }
    }
}
