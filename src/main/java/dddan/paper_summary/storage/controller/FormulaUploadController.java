package dddan.paper_summary.storage.controller;

import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.service.ObjectStorageService;
import dddan.paper_summary.storage.entity.Formula;
import dddan.paper_summary.storage.repository.FormulaRepository;
import dddan.paper_summary.storage.service.PaperSummaryService;
import dddan.paper_summary.ai.dto.AiSummaryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/formula")
public class FormulaUploadController {

    private final PaperRepository paperRepository;
    private final FormulaRepository formulaRepository;
    private final ObjectStorageService objectStorageService;
    private final PaperSummaryService paperSummaryService;

    @Value("${ncp.s3.folder.formula:images/formula/}")
    private String formulaFolder;

    @Value("${ai.summary.api-url}")
    private String aiSummaryApiUrl;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFormulaImage(
            @RequestParam("arxiv_id") String arxivId,
            @RequestParam("section_order") Integer sectionOrder,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Optional<Paper> paperOpt = paperRepository.findByArxivId(arxivId);
            if (paperOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("[ERROR] 해당 arxiv_id의 논문이 존재하지 않습니다.");
            }
            Integer paperId = Math.toIntExact(paperOpt.get().getId());

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return ResponseEntity.badRequest().body("[ERROR] 파일 이름이 유효하지 않습니다.");
            }

            String key = formulaFolder + arxivId + "_" + sectionOrder + "_" + originalFilename;
            String imageUrl = objectStorageService.upload(file, key, true);

            boolean exists = formulaRepository.existsByPaperIdAndSectionOrderAndFormulaPath(paperId, sectionOrder, imageUrl);
            if (exists) {
                return ResponseEntity.badRequest().body("[ERROR] 해당 수식은 이미 업로드되어 있습니다.");
            }

            Formula formula = Formula.builder()
                    .paperId(paperId)
                    .sectionOrder(sectionOrder)
                    .formulaPath(imageUrl)
                    .createdAt(LocalDateTime.now())
                    .build();

            formulaRepository.save(formula);
            return ResponseEntity.ok("[ERROR] 업로드 성공");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("[ERROR] 예기치 않은 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/done")
    public ResponseEntity<List<Map<String, Object>>> handleFormulaAnalysisDone(@RequestBody Map<String, String> body) {
        String arxivId = body.get("arxiv_id");

        if (arxivId == null || arxivId.isBlank()) {
            return ResponseEntity.badRequest().body(List.of(Map.of("error", "arxiv_id가 누락되었습니다.")));
        }

        System.out.println("[INFO] 수식 분석 완료 알림 수신: " + arxivId);

        return paperSummaryService.getSummaryByArxivId(arxivId)
                .map(summaryList -> {
                    String toc = summaryList.stream()
                            .map(sec -> sec.get("section_order") + ". " + sec.get("section_title"))
                            .reduce((s1, s2) -> s1 + "\n" + s2)
                            .orElse("");


                    for (Map<String, Object> section : summaryList) {
                        section.put("table_of_contents", toc);  // 여기에 전체 목차를 넣음
                        AiSummaryRequestDto dto = convertToDto(section);
                        System.out.println("[DEBUG] 생성된 DTO: " + dto);
                        sendToAiSummaryApi(dto);
                    }
                    return ResponseEntity.ok(summaryList);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(List.of(Map.of("error", "논문 정보 없음"))));
    }

    @PostMapping("/fail")
    public ResponseEntity<List<Map<String, Object>>> handleFormulaAnalysisFailed(@RequestBody Map<String, String> body) {
        String arxivId = body.get("arxiv_id");

        if (arxivId == null || arxivId.isBlank()) {
            return ResponseEntity.badRequest().body(List.of(Map.of("error", "arxiv_id가 누락되었습니다.")));
        }

        System.out.println("[WARN] 수식 분석 실패 알림 수신: " + arxivId);

        return paperSummaryService.getSummaryByArxivId(arxivId)
                .map(summary -> {
                    String toc = summary.stream()
                            .map(sec -> sec.get("section_order") + ". " + sec.get("section_title"))
                            .reduce((s1, s2) -> s1 + "\n" + s2)
                            .orElse("");

                    for (Map<String, Object> section : summary) {
                        section.put("equations", List.of());
                        section.put("table_of_contents", toc);
                    }
                    return ResponseEntity.ok(summary);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(List.of(Map.of("error", "논문 정보 없음"))));
    }

    @SuppressWarnings("unchecked")
    private AiSummaryRequestDto convertToDto(Map<String, Object> section) {
        return AiSummaryRequestDto.builder()
                .sectionId((Integer) section.get("section_order"))
                .tableOfContents((String) section.get("section_title"))
                .paperTitle((String) section.get("paper_title"))
                .sectionTitle((String) section.get("section_title"))
                .text((String) section.get("text"))
                .images((List<String>) section.get("images"))
                .tables((List<String>) section.get("tables"))
                .equations((List<String>) section.get("equations"))
                .build();
    }

    private void sendToAiSummaryApi(AiSummaryRequestDto dto) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AiSummaryRequestDto> request = new HttpEntity<>(dto, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(aiSummaryApiUrl, request, String.class);
            System.out.println("[AI 응답] section_id: " + dto.getSectionId() + " → " + response.getBody());
        } catch (Exception e) {
            System.out.println("[ERROR] AI 요약 서버 전송 실패: section_id = " + dto.getSectionId() + " → " + e.getMessage());
        }
    }
}
