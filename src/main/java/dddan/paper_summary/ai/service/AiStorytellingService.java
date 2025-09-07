package dddan.paper_summary.ai.service;

import dddan.paper_summary.ai.client.AiApiClient;
import dddan.paper_summary.ai.domain.Storytelling;
import dddan.paper_summary.ai.dto.StoryRequestDto;
import dddan.paper_summary.ai.dto.StorytellingRequestDto;
import dddan.paper_summary.ai.dto.StorytellingResponseDto;
import dddan.paper_summary.ai.repo.StorytellingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiStorytellingService {

    private final AiApiClient aiApiClient;
    private final StorytellingRepository storytellingRepository;

    /**
     * 전체 텍스트로 AI 스토리텔링 요청 후
     * - 비회원: DB 저장 없이 응답 반환
     * - 회원: 섹션 단위로 저장 후 응답 반환
     */
    @Transactional
    public StorytellingResponseDto requestAndSave(StoryRequestDto req) {
        StorytellingRequestDto aiReq = new StorytellingRequestDto(req.getFullText());
        StorytellingResponseDto resp = aiApiClient.requestStorytelling(aiReq);

        if (req.getUserId() == null) return resp;           // 비회원: 저장 생략
        List<StorytellingResponseDto.Section> sections = resp.getSections();
        if (sections == null || sections.isEmpty()) return resp;

        List<Storytelling> entities = sections.stream()
                .map(s -> Storytelling.builder()
                        .paperId(req.getPaperId())
                        .userId(req.getUserId())
                        .step(s.getStep())
                        .heading(s.getHeading())
                        .content(s.getContent())
                        .build())
                .collect(Collectors.toList()); // Collectors 사용

        storytellingRepository.saveAll(entities);
        return resp;
    }
}
