package dddan.paper_summary.ai.controller;

import dddan.paper_summary.ai.dto.StoryRequestDto;
import dddan.paper_summary.ai.dto.StorytellingResponseDto;
import dddan.paper_summary.ai.service.AiStorytellingService;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 논문 전체 스토리텔링 요청 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storytelling")
public class AiStorytellingController {

    private final AiStorytellingService storytellingService;
    private final PaperRepository paperRepository;

    @PostMapping
    public ResponseEntity<?> handleStoryRequest(@RequestBody StoryRequestDto request) {
        if (request.getPaperId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paperId 누락"));
        }

        Paper paper = paperRepository.findById(request.getPaperId()).orElse(null);
        if (paper == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "존재하지 않는 논문 ID"));
        }

        try {
            StorytellingResponseDto result = storytellingService.requestAndSave(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "스토리텔링 처리 중 서버 오류"));
        }
    }
}
