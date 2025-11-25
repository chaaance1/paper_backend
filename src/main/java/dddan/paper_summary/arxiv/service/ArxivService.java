package dddan.paper_summary.arxiv.service;

import dddan.paper_summary.arxiv.dto.ArxivPaperDto;
//parse
import dddan.paper_summary.parse.ParseService;
import dddan.paper_summary.parse.dto.ParseRequestDto;
import dddan.paper_summary.parse.domain.model.PaperRef;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// 맨 위 import 영역에 추가
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;



@RequiredArgsConstructor
@Service
public class ArxivService {

    private static final Logger log = LoggerFactory.getLogger(ArxivService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final PdfDownloadService pdfDownloadService;
    private final ParseService parseService;   // ★ 파싱 서비스

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
                        authorsList, idUrl, pdfUrl
                ));
            }

        } catch (Exception e) {
            log.error("[ERROR] Failed to parse arXiv XML", e);
        }

        return papers;
    }

    /**
     * 논문 상세 URL → 메타데이터 + PDF 다운로드
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


            // 파일 이름만 추출 (예: 2201.00276.pdf)
            String filename = localPath.getFileName().toString();

            // 2. PaperRef 만들어서 파싱 서비스 호출
            PaperRef ref = PaperRef.builder()
                    .paperId(null)                        // 나중에 Paper 엔티티 생기면 그 ID 넣으면 됨
                    .filename(filename)                   // 원래 파일 이름
                    .localPath(localPath.toString())      // 로컬 전체 경로
                    .inputStreamSupplier(() -> openStream(localPath.toString()))
                    .build();

            parseService.parse(ref);

        } catch (IOException e) {
            log.error("[UPLOAD ERROR] {}", dto.getPdfUrl(), e);
            throw new RuntimeException("PDF 처리 중 오류 발생", e);
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
    // ArxivService.java 하단에 헬퍼 추가
    private InputStream openStream(String pathOrUrl) {
        try {
            if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
                return new URL(pathOrUrl).openStream();
            }
            return Files.newInputStream(Path.of(pathOrUrl));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

