package dddan.paper_summary.arxiv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlaskFormulaNotifyService {

    private final RestTemplate restTemplate;

    @Value("${flask.url}")
    private String flaskUrl;

    /**
     * 수식 파싱을 위해 Flask 서버에 storageUrl 전달
     *
     * @param storageUrl S3 또는 오브젝트 스토리지의 PDF URL
     */
    public void sendToFlask(String storageUrl) {
        if (flaskUrl == null || flaskUrl.isBlank()) {
            log.warn("⚠️ Flask URL이 설정되지 않았습니다. 요청을 생략합니다.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = Map.of("pdf_url", storageUrl);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Flask 수식 파싱 요청 성공: {}", response.getBody());
            } else {
                log.warn("⚠️ Flask 응답 코드: {} - 응답 본문: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Flask 수식 파싱 API 호출 중 오류 발생", e);
        }
    }
}
