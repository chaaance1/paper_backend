package dddan.paper_summary;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arxiv")
public class ArxivController {

    private final ArxivService arxivService;
    private final PdfDownloadService pdfDownloadService;

    // URL 기반 논문 메타데이터 조회
    @GetMapping("/fetch")
    public ResponseEntity<ArxivPaperDto> fetchAndDownloadByUrl(@RequestParam String url) {
        try {
            ArxivPaperDto dto = arxivService.fetchAndDownloadByUrl(url);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 논문의 메타데이터 중 pdfURL 기반 논문 자동 다운로드
    @PostMapping("/direct-download")
    public ResponseEntity<?> downloadFromDirectUrlPost(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'url' in request body.");
        }

        try {
            String savedPath = pdfDownloadService.downloadFromArxivUrl(url);
            return ResponseEntity.ok("PDF saved at: " + savedPath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Download failed: " + e.getMessage());
        }
    }

    // 제목 기반 논문 검색
    @GetMapping("/search")
    public ResponseEntity<?> smartSearch(@RequestParam String query) {
        String xml = arxivService.searchPapersByTitle(query);
        List<ArxivPaperDto> papers = arxivService.parseArxivResponse(xml);

        if (papers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No papers found.");
        }
        // 논문 객체 리스트 반환
        return ResponseEntity.ok(papers);
    }

}
