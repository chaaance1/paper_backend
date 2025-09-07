package dddan.paper_summary.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dddan.paper_summary.ai.domain.Summary;
import dddan.paper_summary.ai.dto.AiSummaryRequestDto;
import dddan.paper_summary.ai.dto.AiSummaryResponseDto;
import dddan.paper_summary.ai.dto.SummaryRequestDto;
import dddan.paper_summary.ai.repo.SummaryRepository;
import dddan.paper_summary.auth.repo.UserRepository;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.service.PaperSummaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

    private final SummaryRepository summaryRepository;
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    private final PaperSummaryService paperSummaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.summary.api-url}")
    private String aiApiUrl;

    @Override
    @Transactional
    public void summarizeAndSaveAllSections(Long paperId, String userId) {
        boolean isMember = (userId != null && !userId.isBlank());
        int savedCount = 0;

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("논문이 존재하지 않습니다."));
        String arxivId = paper.getArxivId();

        List<Map<String, Object>> sectionList = paperSummaryService.getSummaryByArxivId(arxivId)
                .orElseThrow(() -> new IllegalArgumentException("요약할 섹션 정보가 존재하지 않습니다."));

        String toc = buildTableOfContents(sectionList);

        log.info("[LOG] 요약 시작 - paperId: {}, userId: {}, section 수: {}", paperId, userId, sectionList.size());

        for (Map<String, Object> section : sectionList) {
            int sectionId = getNumber(section.get("section_order"));
            try {
                AiSummaryRequestDto dto = convertToDto(section, paper.getTitle(), toc);
                AiSummaryResponseDto aiResponse = callAiSummaryApi(dto);

                if (isMember && userRepository.existsByUserId(userId)) {
                    Summary summary = Summary.builder()
                            .paperId(paperId)
                            .userId(userId)
                            .sectionOrder(dto.getSectionId())
                            .summaryText(aiResponse.getTextResult())
                            .summaryFormula(String.join("\n",
                                    Optional.ofNullable(aiResponse.getEquationResults()).orElse(List.of())))
                            .summaryFigure(String.join("\n",
                                    Optional.ofNullable(aiResponse.getImageResults()).orElse(List.of())))
                            .summaryTable(String.join("\n",
                                    Optional.ofNullable(aiResponse.getTableResults()).orElse(List.of())))
                            .build();

                    summaryRepository.save(summary);
                    log.info("[LOG] 요약 저장 완료 - sectionId={}, userId={}", dto.getSectionId(), userId);
                    savedCount++;
                } else {
                    log.warn("[ERROR] 저장 스킵 - 사용자 없음 또는 비회원: userId={}", userId);
                }

            } catch (Exception e) {
                log.error("[ERROR] 요약 실패 - sectionId={}, error={}", sectionId, e.getMessage(), e);
            }
        }

        log.info("[LOG] 요약 저장 완료 - 총 저장된 섹션 수: {}", savedCount);
    }

    private String buildTableOfContents(List<Map<String, Object>> sections) {
        return sections.stream()
                .map(s -> getNumber(s.get("section_order")) + ". " + Objects.toString(s.get("section_title"), ""))
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("");
    }

    private int getNumber(Object v) {
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) try { return (int) Double.parseDouble(s); } catch (Exception ignored) {}
        return 0;
    }

    private List<String> toStringList(Object v) {
        if (v == null) return List.of();

        return switch (v) {
            case List<?> list -> list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
            case String s -> {
                try {
                    yield objectMapper.readValue(s, new TypeReference<List<String>>() {});
                } catch (Exception ignore) {
                    if (s.startsWith("http")) yield List.of(s);
                    yield List.of();
                }
            }
            default -> List.of();
        };
    }

    private AiSummaryRequestDto convertToDto(Map<String, Object> section, String paperTitle, String toc) {
        return AiSummaryRequestDto.builder()
                .sectionId(getNumber(section.get("section_order")))
                .sectionTitle(Objects.toString(section.get("section_title"), ""))
                .text(Objects.toString(section.get("text"), ""))
                .images(toStringList(section.get("images")))
                .tables(toStringList(section.get("tables")))
                .equations(toStringList(section.get("equations")))
                .tableOfContents(toc)
                .paperTitle(paperTitle)
                .build();
    }

    private AiSummaryResponseDto callAiSummaryApi(AiSummaryRequestDto dto) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<AiSummaryRequestDto> request = new HttpEntity<>(dto, headers);

        try {
            if (log.isDebugEnabled()) {
                log.debug("[AI-REQ] {}", objectToJson(dto));
            }
            ResponseEntity<AiSummaryResponseDto> response =
                    restTemplate.postForEntity(aiApiUrl, request, AiSummaryResponseDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("[AI-ERR] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private String objectToJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { return String.valueOf(o); }
    }

    @Override
    public List<AiSummaryResponseDto> summarizeAllSections(SummaryRequestDto requestDto) {
        Paper paper = paperRepository.findById(requestDto.getPaperId())
                .orElseThrow(() -> new IllegalArgumentException("논문이 존재하지 않습니다."));
        String arxivId = paper.getArxivId();

        List<Map<String, Object>> sectionList = paperSummaryService.getSummaryByArxivId(arxivId)
                .orElseThrow(() -> new IllegalArgumentException("요약할 섹션 정보가 존재하지 않습니다."));

        String toc = buildTableOfContents(sectionList);

        List<AiSummaryResponseDto> resultList = new ArrayList<>();
        for (Map<String, Object> section : sectionList) {
            AiSummaryRequestDto dto = convertToDto(section, paper.getTitle(), toc);
            resultList.add(callAiSummaryApi(dto));
        }
        return resultList;
    }
}
