package dddan.paper_summary.storage.service;

import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.entity.*;
import dddan.paper_summary.storage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaperSummaryService {

    private final PaperRepository paperRepository;
    private final SectionRepository sectionRepository;
    private final FormulaRepository formulaRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final TableAssetRepository tableAssetRepository;

    public Optional<List<Map<String, Object>>> getSummaryByArxivId(String arxivId) {
        Optional<Paper> paperOpt = paperRepository.findByArxivId(arxivId);
        if (paperOpt.isEmpty()) return Optional.empty();

        Paper paper = paperOpt.get();
        Integer paperId = Math.toIntExact(paper.getId());

        List<Section> sections = sectionRepository.findByPaperIdOrderBySectionOrderAsc(paperId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Section section : sections) {
            Integer sectionOrder = section.getSectionOrder();

            List<String> equations = formulaRepository.findAllByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream().map(Formula::getFormulaPath).filter(Objects::nonNull).toList();

            List<String> images = imageAssetRepository.findByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream().map(ImageAsset::getImagePath).filter(Objects::nonNull).toList();

            List<String> tables = tableAssetRepository.findByPaperIdAndSectionOrderOrderByIdAsc(paperId, sectionOrder)
                    .stream().map(TableAsset::getTablePath).filter(Objects::nonNull).toList();

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

        return Optional.of(result);
    }
}
