package dddan.paper_summary.storage.controller;

import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.entity.*;
import dddan.paper_summary.storage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class PaperSummaryController {

    private final PaperRepository paperRepository;
    private final SectionRepository sectionRepository;
    private final FormulaRepository formulaRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final TableAssetRepository tableAssetRepository;

    @GetMapping
    public ResponseEntity<?> getPaperSummary(@RequestParam("arxiv_id") String arxivId) {

        // 1. 논문 존재 확인
        Optional<Paper> paperOpt = paperRepository.findByArxivId(arxivId);
        if (paperOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("[ERROR] 해당 arxiv_id의 논문이 존재하지 않습니다.");
        }

        Paper paper = paperOpt.get();
        Integer paperId = Math.toIntExact(paper.getId());

        // 2. 섹션별로 정리
        List<Section> sections = sectionRepository.findByPaperIdOrderBySectionOrderAsc(paperId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Section section : sections) {
            Integer sectionOrder = section.getSectionOrder();

            // 3. 수식
            List<String> equations = formulaRepository.findAllByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream()
                    .map(Formula::getFormulaPath)
                    .filter(Objects::nonNull)
                    .toList();

            // 4. 이미지 (ImageAsset으로 변경됨)
            List<String> images = imageAssetRepository.findByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream()
                    .map(ImageAsset::getImagePath)
                    .filter(Objects::nonNull)
                    .toList();

            // 5. 표 (TableAsset으로 변경됨)
            List<String> tables = tableAssetRepository.findByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream()
                    .map(TableAsset::getTablePath)
                    .filter(Objects::nonNull)
                    .toList();

            // 6. JSON 객체 생성
            Map<String, Object> json = new HashMap<>();
            json.put("section_order", sectionOrder);
            json.put("paper_title", paper.getTitle());
            json.put("section_title", section.getTitle());
            json.put("text", section.getContent());
            json.put("images", images);
            json.put("tables", tables);
            json.put("equations", equations);

            result.add(json);
        }

        return ResponseEntity.ok(result);
    }
}
