package dddan.paper_summary.ai.client;

import dddan.paper_summary.ai.dto.StorytellingRequestDto;
import dddan.paper_summary.ai.dto.StorytellingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import dddan.paper_summary.ai.dto.SummaryApiRequest;
import dddan.paper_summary.ai.dto.SummaryApiResponse;

/**
 * AI API 호출 클라이언트
 * - 스토리텔링 및 섹션 요약 API 호출을 담당
 */
@Component
@RequiredArgsConstructor
public class AiApiClient {

    private final RestTemplate restTemplate;

    private final String STORYTELLING_API_URL = "http://localhost:8000/api/story"; // 실제 AI API 주소로 교체

    /**
     * 논문 전체 텍스트 기반 스토리텔링 요청
     *
     * @param requestDto 전체 텍스트 포함한 요청 DTO
     * @return AI 응답 DTO
     */
    public StorytellingResponseDto requestStorySummary(StorytellingRequestDto requestDto) {
        ResponseEntity<StorytellingResponseDto> response = restTemplate.postForEntity(
                STORYTELLING_API_URL,
                requestDto,
                StorytellingResponseDto.class
        );

        return response.getBody();
    }

    public SummaryApiResponse requestSectionSummary(SummaryApiRequest dto) {
        String url = "http://localhost:8000/api/summary"; // 실제 AI 요약 API 주소로 교체

        ResponseEntity<SummaryApiResponse> response = restTemplate.postForEntity(
                url,
                dto,
                SummaryApiResponse.class
        );

        return response.getBody();
    }


}
