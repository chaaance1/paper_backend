package dddan.paper_summary.ai.controller;

import dddan.paper_summary.ai.dto.AiSummaryRequestDto;
import dddan.paper_summary.ai.dto.AiSummaryResponseDto;
import dddan.paper_summary.ai.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 논문 섹션 요약 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    @PostMapping
    public AiSummaryResponseDto summarize(@RequestBody AiSummaryRequestDto requestDto) {
        return aiSummaryService.summarizePaper(requestDto);
    }
}
