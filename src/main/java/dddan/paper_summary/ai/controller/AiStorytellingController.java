package dddan.paper_summary.ai.controller;

import dddan.paper_summary.ai.dto.StorytellingResponseDto;
import dddan.paper_summary.ai.service.AiStorytellingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 스토리텔링 API 컨트롤러
 * - 프론트에서 논문 ID를 요청하면 전체 텍스트 기반 요약을 반환
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storytelling")
public class AiStorytellingController {

    private final AiStorytellingService storytellingService;

    /**
     * 논문 ID로 스토리 요약 생성 요청
     * @param paperId 논문 ID
     * @return 스토리 요약 결과
     */
    @GetMapping("/{paperId}")
    public StorytellingResponseDto generateStory(@PathVariable Long paperId) {
        return storytellingService.generateStory(paperId);
    }
}
