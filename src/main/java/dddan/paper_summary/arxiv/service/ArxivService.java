package dddan.paper_summary.arxiv.service;

import dddan.paper_summary.arxiv.dto.ArxivPaperDto;
import dddan.paper_summary.arxiv.domain.Paper;
import dddan.paper_summary.arxiv.repo.PaperRepository;
import dddan.paper_summary.storage.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Value;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArxivService {

    private static final Logger log = LoggerFactory.getLogger(ArxivService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final PdfDownloadService pdfDownloadService;
    private final ObjectStorageService objectStorageService;
    private final PaperRepository paperRepository;

    @Value("${flask.base-url}")
    private String flaskBaseUrl;
    /**
     * 제목 기반 논문 검색 (최대 5개)
     */
    public String searchPapersByTitle(String query) {
        String encodedQuery = "ti:\"" + query.trim() + "\"";
        URI uri = UriComponentsBuilder
                .fromUriString("https://export.arxiv.org/api/query")
                .queryParam("search_query", encodedQuery)
                .queryParam("start", 0)
                .queryParam("max_results", 5)
                .queryParam("sortBy", "relevance")
                .queryParam("sortOrder", "descending")
                .build(false)
                .toUri();

        String body = restTemplate.getForObject(uri, String.class);
        return body == null ? "" : body;
    }

    /**
     * 논문 XML → DTO 리스트 변환
     */
    public List<ArxivPaperDto> parseArxivResponse(String xml) {
        List<ArxivPaperDto> papers = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList entries = doc.getElementsByTagName("entry");

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);

                String title = getText(entry, "title");
                String summary = getText(entry, "summary");
                String published = getText(entry, "published");
                String updated = getText(entry, "updated");

                NodeList authorNodes = entry.getElementsByTagName("author");
                List<String> authorsList = new ArrayList<>();
                for (int j = 0; j < authorNodes.getLength(); j++) {
                    Element author = (Element) authorNodes.item(j);
                    authorsList.add(getText(author, "name"));
                }
                String authors = String.join(", ", authorsList);

                String idUrl = getText(entry, "id").trim().replace("http://", "https://");
                String arxivId = extractIdFromUrl(idUrl).trim();

                // pdfUrl
                String pdfUrl = null;
                NodeList links = entry.getElementsByTagName("link");
                for (int j = 0; j < links.getLength(); j++) {
                    Element link = (Element) links.item(j);
                    if ("application/pdf".equals(link.getAttribute("type"))) {
                        pdfUrl = link.getAttribute("href");
                        break;
                    }
                }

                if (pdfUrl == null || pdfUrl.isBlank()) {
                    pdfUrl = "https://arxiv.org/pdf/" + arxivId + ".pdf";
                } else {
                    pdfUrl = pdfUrl.replace("http://", "https://");
                    if (!pdfUrl.endsWith(".pdf")) pdfUrl += ".pdf";
                }

                papers.add(new ArxivPaperDto(
                        arxivId, title.trim(), summary.trim(), published, updated,
                        authorsList, idUrl, pdfUrl, null
                ));
            }

        } catch (Exception e) {
            log.error("[ERROR] Failed to parse arXiv XML", e);
        }

        return papers;
    }

    /**
     * 논문 상세 URL → 메타데이터 + PDF 다운로드 + 클라우드 업로드
     */
    public ArxivPaperDto uploadPaperFromUrl(String url) {
        String xml = fetchPaperByUrl(url);
        List<ArxivPaperDto> papers = parseArxivResponse(xml);

        if (papers.isEmpty()) {
            throw new IllegalArgumentException("No paper found for the given URL.");
        }

        ArxivPaperDto dto = papers.getFirst();

        try {
            // 1. PDF 다운로드
            String savedPath = pdfDownloadService.downloadFromArxivUrl(dto.getPdfUrl());
            Path localPath = Path.of(savedPath);

            // 2. 클라우드 업로드
            String storageUrl = objectStorageService.uploadLocalFile(localPath, true);
            dto.setStorageUrl(storageUrl);

            // 3. 로컬 파일 삭제
            Files.deleteIfExists(localPath);

            // 4. 데이터베이스에 메타데이터 업로드
            Paper entity = paperRepository.findByArxivId(dto.getArxivId())
                    .map(p -> {
                        // 기존 레코드 갱신
                        p.setTitle(dto.getTitle());
                        p.setAuthors(String.join(", ", dto.getAuthors()));
                        p.setPublishedDate(dto.getPublishedDate());  // 아래 DTO 변환 메서드 사용(아래 참고)
                        p.setUpdatedDate(dto.getUpdatedDate());
                        p.setPdfPath(dto.getStorageUrl());
                        p.setAbstractText(dto.getSummary());
                        return p;
                    })
                    .orElseGet(() -> Paper.builder()
                            .arxivId(dto.getArxivId())
                            .title(dto.getTitle())
                            .authors(String.join(", ", dto.getAuthors()))
                            .publishedDate(dto.getPublishedDate())
                            .updatedDate(dto.getUpdatedDate())
                            .pdfPath(dto.getStorageUrl())
                            .abstractText(dto.getSummary())
                            .build());

            paperRepository.save(entity);

            String paperId = String.valueOf(entity.getId());
            requestParsingToFlask(paperId, storageUrl);

        } catch (IOException e) {
            log.error("[UPLOAD ERROR] {}", dto.getPdfUrl(), e);
            throw new RuntimeException("PDF 처리 중 오류 발생");
        }

        return dto;
    }

    /**
     * 논문 상세 API 호출 (ID 기반)
     */
    private String fetchPaperByUrl(String url) {
        String paperId = extractIdFromUrl(url);
        URI uri = UriComponentsBuilder
                .fromUriString("https://export.arxiv.org/api/query")
                .queryParam("id_list", paperId)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_ATOM_XML));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    private String extractIdFromUrl(String url) {
        String last = url.substring(url.lastIndexOf('/') + 1);
        return last.replace(".pdf", "");
    }

    private static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    private String getText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(ATOM_NS, tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
    @Async
    public void requestParsingToFlask(String paperId, String pdfUrl) {
        try {
            String url = flaskBaseUrl + "/parse";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String json = """
                {"paperId":"%s","pdfUrl":"%s"}
            """.formatted(paperId, pdfUrl);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                log.warn("[Flask] 파싱요청 실패 status={} body={}", res.getStatusCode(), res.getBody());
            } else {
                log.info("[Flask] 파싱요청 성공 body={}", res.getBody());
            }
        } catch (Exception e) {
            log.warn("[Flask] 파싱요청 예외: {}", e.getMessage(), e);
        }
    }
}
