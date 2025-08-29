package dddan.paper_summary.arxiv.controller;

import dddan.paper_summary.arxiv.dto.ArxivPaperDto;
import dddan.paper_summary.arxiv.service.ArxivService;
import dddan.paper_summary.arxiv.service.FlaskFormulaNotifyService;
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
    private final FlaskFormulaNotifyService flaskFormulaNotifyService;

    /**
     * 제목 기반 논문 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "query 파라미터가 비어 있습니다."));
        }

        String xml = arxivService.searchPapersByTitle(query);
        List<ArxivPaperDto> papers = arxivService.parseArxivResponse(xml);

        if (papers.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "검색 결과가 없습니다."));
        }

        return ResponseEntity.ok(papers);

    }

    /**
     * 통합 API: 논문 URL → 메타데이터 조회 → PDF 다운로드 → 클라우드 업로드 → storageUrl 포함 응답
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "url 파라미터가 비어 있습니다."));
        }

        try {
            ArxivPaperDto dto = arxivService.uploadPaperFromUrl(url);

            if (dto.getStorageUrl() != null && !dto.getStorageUrl().isBlank()) {
                flaskFormulaNotifyService.sendToFlask(dto.getStorageUrl()); // 수식 파싱 API 호출
            }

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "업로드에 실패했습니다."));
        }
    }
}
