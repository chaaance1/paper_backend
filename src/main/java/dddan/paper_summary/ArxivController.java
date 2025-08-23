package dddan.paper_summary;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arxiv")
public class ArxivController {

    private final ArxivService arxivService;
    private final PdfDownloadService pdfDownloadService;

    // 제목 기반 논문 검색
    @GetMapping("/search")
    public List<ArxivPaperDto> search(@RequestParam String query) {
        String xml = arxivService.searchPapersByTitle(query);
        return arxivService.parseArxivResponse(xml);
    }

    // 링크 기반 논문 조회
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

    // 논문 검색 후 PDF 다운로드
    @GetMapping("/search-download")
    public List<ArxivPaperDto> searchAndDownload(@RequestParam String query) {
        return arxivService.searchAndDownloadPapers(query);
    }

    // 논문 자동 다운로드
    @GetMapping("/direct-download")
    public ResponseEntity<?> downloadFromDirectUrl(@RequestParam String url) {
        try {
            String savedPath = pdfDownloadService.downloadFromArxivUrl(url);
            return ResponseEntity.ok("PDF saved at: " + savedPath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Download failed: " + e.getMessage());
        }
    }


}
