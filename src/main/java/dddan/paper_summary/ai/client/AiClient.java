package dddan.paper_summary.ai.client;

import dddan.paper_summary.ai.dto.AiRequestDto;
import dddan.paper_summary.ai.dto.AiFullTextRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 서버와의 통신을 담당하는 Client 클래스
 * - 논문의 섹션별 콘텐츠 또는 전체 텍스트를 AI 서버에 전달
 * - 현재는 로그만 남기며, 추후 HTTP(WebClient) 호출이 추가될 예정
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AiClient {

    public void sendSectionRequest(AiRequestDto dto) {
        log.info("[AI] sendSectionRequest paperId={}, sections={}",
                dto.getPaperId(),
                dto.getSections() != null ? dto.getSections().size() : 0
        );
        // TODO: 여기서 실제 HTTP 호출 붙이면 됨
    }

    public void sendFullTextRequest(AiFullTextRequest dto) {
        log.info("[AI] sendFullTextRequest paperId={}, textLength={}",
                dto.getPaperId(),
                dto.getFullText() != null ? dto.getFullText().length() : 0
        );
    }
}
