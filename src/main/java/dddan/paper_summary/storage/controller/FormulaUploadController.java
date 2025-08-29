package dddan.paper_summary.storage.controller;

import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.service.ObjectStorageService;
import dddan.paper_summary.storage.entity.Formula;
import dddan.paper_summary.storage.repository.FormulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/formula")
public class FormulaUploadController {

    private final PaperRepository paperRepository;
    private final FormulaRepository formulaRepository;
    private final ObjectStorageService objectStorageService;

    @Value("${ncp.s3.folder.formula:images/formula/}") // 예: formulas/
    private String formulaFolder;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFormulaImage(
            @RequestParam("arxiv_id") String arxivId,
            @RequestParam("section_order") Integer sectionOrder,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1) 논문 존재 확인
            Optional<Paper> paperOpt = paperRepository.findByArxivId(arxivId);
            if (paperOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("[ERROR] 해당 arxiv_id의 논문이 존재하지 않습니다.");
            }
            Integer paperId = Math.toIntExact(paperOpt.get().getId());

            // 2) 파일명 유효성 검증
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename == null || originalFilename.isBlank()) {
                return ResponseEntity.badRequest().body("[ERROR] 파일 이름이 유효하지 않습니다.");
            }

            // 3) 업로드 키 생성 (스토리지 내 경로)
            String key = formulaFolder + arxivId + "_" + sectionOrder + "_" + originalFilename;

            // 4) 오브젝트 스토리지 업로드 → 공개 URL 획득
            String imageUrl = objectStorageService.upload(file, key, true);

            // 5) 중복 체크 (URL 기준)
            boolean exists = formulaRepository.existsByPaperIdAndSectionOrderAndFormulaPath(paperId, sectionOrder, imageUrl);
            if (exists) {
                return ResponseEntity.badRequest().body("[ERROR] 해당 수식은 이미 업로드되어 있습니다.");
            }

            // 6) DB 저장
            Formula formula = Formula.builder()
                    .paperId(paperId)
                    .sectionOrder(sectionOrder)
                    .formulaPath(imageUrl)        // URL 저장
                    .createdAt(LocalDateTime.now())
                    .build();

            formulaRepository.save(formula);
            return ResponseEntity.ok("[ERROR] 업로드 성공");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("[ERROR] 예기치 않은 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/done")
    public ResponseEntity<String> handleFormulaAnalysisDone(@RequestBody Map<String, String> body) {
        String arxivId = body.get("arxiv_id");

        if (arxivId == null || arxivId.isBlank()) {
            return ResponseEntity.badRequest().body("[ERROR] arxiv_id가 누락되었습니다.");
        }

        System.out.println("[INFO] 수식 분석 완료 알림 수신: " + arxivId);

        // 이후 자동화 처리 로직을 여기에 넣을 수 있음
        // ai에 넘길 json
        // 데이터베이스에서 꺼내올 것

        return ResponseEntity.ok("[SUCCESS] 수식 분석 완료 수신 처리됨: " + arxivId);
    }

    @PostMapping("/fail")
    public ResponseEntity<String> handleFormulaAnalysisFailed(@RequestBody Map<String, String> body) {
        String arxivId = body.get("arxiv_id");

        if (arxivId == null || arxivId.isBlank()) {
            return ResponseEntity.badRequest().body("[ERROR] arxiv_id가 누락되었습니다.");
        }

        System.out.println("[WARN] 수식 분석 실패 알림 수신: " + arxivId);

        // 수식 null 값으로 채우고 반환

        return ResponseEntity.ok("[SUCCESS] 수식 분석 실패 수신 처리됨: " + arxivId);
    }


}
