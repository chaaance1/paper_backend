package dddan.paper_summary.ai.controller;

import dddan.paper_summary.ai.dto.*;
import dddan.paper_summary.ai.service.AiSummaryService;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 섹션 요약 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class AiSummaryController {

    private final AiSummaryService summaryService;
    private final PaperRepository paperRepository;

    @PostMapping
    public ResponseEntity<?> summarizeAll(@RequestBody SummaryRequestDto request) {
        if (request.getPaperId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paperId 누락"));
        }

        Paper paper = paperRepository.findById(request.getPaperId()).orElse(null);
        if (paper == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "존재하지 않는 논문 ID"));
        }

        try {
            List<AiSummaryResponseDto> result = summaryService.summarizeAllSections(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "요약 처리 중 서버 오류"));
        }
    }
}