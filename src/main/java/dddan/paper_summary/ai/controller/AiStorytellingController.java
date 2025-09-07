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
    public ResponseEntity<?> handleStoryRequest(@RequestBody StoryRequestDto request,
                                                java.security.Principal principal) {
        if (request.getPaperId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paperId가 누락되었습니다."));
        }

        Paper paper = paperRepository.findById(request.getPaperId()).orElse(null);
        if (paper == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "존재하지 않는 논문 ID입니다."));
        }

        // 로그인된 경우 userId 자동 세팅 (request_id 관련 로직은 제거)
        if (request.getUserId() == null && principal != null) {
            request.setUserId(principal.getName());
        }

        try {
            StorytellingResponseDto result = storytellingService.requestAndSave(request);
            // request_id 래핑 없이 결과만 반환
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "스토리텔링 처리 중 서버 오류가 발생했습니다."));
        }
    }
}
