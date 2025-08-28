package dddan.paper_summary.ai.client;

import dddan.paper_summary.ai.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class AiApiClient {

    private final @Qualifier("aiWebClient") WebClient aiWebClient;

    public StorytellingResponseDto requestStorytelling(StorytellingRequestDto dto) {
        return aiWebClient.post().uri("/storytelling")
                .bodyValue(dto).retrieve()
                .bodyToMono(StorytellingResponseDto.class).block();
    }

    public AiSummaryResponseDto requestSummary(AiSummaryRequestDto dto) {
        return aiWebClient.post().uri("/summary")
                .bodyValue(dto).retrieve()
                .bodyToMono(AiSummaryResponseDto.class).block();
    }
}
