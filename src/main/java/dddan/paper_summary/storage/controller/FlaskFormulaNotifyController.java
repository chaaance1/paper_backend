package dddan.paper_summary.storage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/notify")
public class FlaskFormulaNotifyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${flask.url}") // application.yml 또는 환경변수에서 주입
    private String flaskUrl;

    @PostMapping("/send-to-flask")
    public ResponseEntity<?> sendPdfUrlToFlask(
            @RequestParam("pdfUrl") String pdfUrl,
            Principal principal // 👈 인증된 사용자 정보 주입
    ) {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            return ResponseEntity.badRequest().body("[ERROR] 전달받은 PDF URL이 비어 있습니다.");
        }

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.badRequest().body("[ERROR] 로그인된 사용자 정보를 확인할 수 없습니다.");
        }

        String userId = principal.getName(); // 👈 로그인된 사용자 ID

        // 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 요청 본문 구성 (user_id 포함)
        Map<String, String> payload = Map.of(
                "pdf_url", pdfUrl,
                "user_id", userId
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        // 3. Flask 서버로 POST 요청 전송
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);
            return ResponseEntity.ok("[LOG] Flask 응답: " + response.getBody());
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body("[ERROR] Flask 요청 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
