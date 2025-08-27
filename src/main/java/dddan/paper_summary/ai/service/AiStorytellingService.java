package dddan.paper_summary.ai.service;

import dddan.paper_summary.ai.client.AiApiClient;
import dddan.paper_summary.ai.dto.StorytellingRequestDto;
import dddan.paper_summary.ai.dto.StorytellingResponseDto;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 논문 전체 텍스트 기반 스토리텔링 생성 서비스
 * - 논문 ID를 통해 논문 전체 텍스트를 가져오고
 * - AI API로 요청 후 요약 결과를 반환
 */
@Service
@RequiredArgsConstructor
public class AiStorytellingService {

    private final PaperRepository paperRepository;
    private final AiApiClient aiApiClient;

    /**
     * 논문 ID를 기반으로 전체 텍스트 기반 스토리 요약 요청
     * @param paperId 논문 ID
     * @return 스토리 요약 결과 DTO
     */
    public StorytellingResponseDto generateStory(Long paperId) {
        // 1. 논문 조회
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("해당 논문이 존재하지 않습니다."));

        // 2. 전체 텍스트 추출 -> 이 부분 채워야 함
        String fullText = paper.getParsedFullText();
        if (fullText == null || fullText.isBlank()) {
            throw new IllegalStateException("논문 전체 텍스트가 존재하지 않습니다.");
        }

        // 3. 요청 DTO 생성
        StorytellingRequestDto requestDto = new StorytellingRequestDto(fullText);

        // 4. AI 서버에 요청 후 응답 반환
        return aiApiClient.requestStorySummary(requestDto);
    }
}
