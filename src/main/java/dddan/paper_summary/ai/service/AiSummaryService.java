package dddan.paper_summary.ai.service;

import dddan.paper_summary.ai.dto.SummaryRequestDto;
import dddan.paper_summary.ai.dto.AiSummaryResponseDto;
import java.util.List;

public interface AiSummaryService {
    void summarizeAndSaveAllSections(Long paperId, String userId);
    List<AiSummaryResponseDto> summarizeAllSections(SummaryRequestDto requestDto);
}
