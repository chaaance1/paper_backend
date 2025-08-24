package dddan.paper_summary;

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
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArxivService {

    // 로그 생성을 위한 Logger 객체 생성
    private static final Logger log = LoggerFactory.getLogger(ArxivService.class);

    // HTTP 요청 전송을 위한 도구
    private final RestTemplate restTemplate = new RestTemplate();

    // PDF 파일을 실제로 다운로드하는 기능을 담당하는 서비스
    private final PdfDownloadService pdfDownloadService;

    /**
     * 검색어로 논문 검색 요청 (제목 기반)
     * @param query 제목 키워드
     * @return XML 응답 본문
     */
    public String searchPapersByTitle(String query) {
        String encodedQuery = "ti:\"" + query.trim() + "\"";
        // arXiv API의 요청 URL을 구성함
        URI uri = UriComponentsBuilder
                .fromUriString("https://export.arxiv.org/api/query")
                .queryParam("search_query",  encodedQuery)
                .queryParam("start", 0)
                .queryParam("max_results", 5)       // 반환되는 논문 개수
                .queryParam("sortBy", "relevance")
                .queryParam("sortOrder", "descending")
                .build(false)
                .toUri();

        // 본문 내용 추출해서 반환
        String body = restTemplate.getForObject(uri, String.class);
        return body == null ? "" : body;
    }

    /**
     * 논문 검색 결과 XML을 DTO 리스트로 변환
     * @param xml arXiv API 응답 XML
     * @return 논문 DTO 리스트
     */
    public List<ArxivPaperDto> parseArxivResponse(String xml) {
        List<ArxivPaperDto> papers = new ArrayList<>();

        try {
            // XML 파서 초기화
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            // <entry> 태그: 논문 하나
            NodeList entries = doc.getElementsByTagName("entry");

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);

                // 논문 기본 정보 추출
                String title = getText(entry, "title");
                String summary = getText(entry, "summary");
                String published = getText(entry, "published");
                String updated = getText(entry, "updated");

                // 논문 작성자 목록 추출
                NodeList authorNodes = entry.getElementsByTagName("author");
                List<String> authorsList = new ArrayList<>();
                for (int j = 0; j < authorNodes.getLength(); j++) {
                    Element author = (Element) authorNodes.item(j);
                    authorsList.add(getText(author, "name"));
                }
                String authors = String.join(", ", authorsList);

                // 논문 URL 및 PDF 링크 추출
                String idUrl = getText(entry, "id");
                String pdfUrl = null;
                NodeList links = entry.getElementsByTagName("link");
                for (int j = 0; j < links.getLength(); j++) {
                    Element link = (Element) links.item(j);
                    if ("application/pdf".equals(link.getAttribute("type"))) {
                        pdfUrl = link.getAttribute("href");
                        break;
                    }
                }

                // ArxivPaperDto 객체 생성 후 리스트에 추가
                papers.add(new ArxivPaperDto(
                        title.trim(), summary.trim(), published, updated,
                        authors, idUrl, pdfUrl
                ));
            }

        } catch (Exception e) {
            log.error("[ERROR] An error occurred while parsing the response", e);
        }

        return papers;
    }

    /**
     * 논문 링크 입력 시, ID 추출 후 상세 정보 조회
     * @param url arXiv 논문 URL
     * @return XML 응답 본문
     */
    public String fetchPaperByUrl(String url) {
        // 논문 ID 추출
        String paperId = extractIdFromUrl(url);

        // 해당 논문 ID로 API 요청 URL 구성
        URI uri = UriComponentsBuilder
                .fromUriString("https://export.arxiv.org/api/query")
                .queryParam("id_list", paperId)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_ATOM_XML));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 요청 결과 반환
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    /**
     * 논문 URL 기반 PDF 다운로드 포함 동작 전체 수행
     * @param url arXiv 논문 URL
     * @return PDF 경로 포함된 논문 DTO
     */
    public ArxivPaperDto fetchAndDownloadByUrl(String url) {
        // 1. 입력된 URL에서 논문 ID를 추출하여 arXiv API 호출
        String xml = fetchPaperByUrl(url);

        // 2. 응답받은 XML 데이터를 파싱하여 ArxivPaperDto 객체로 변환
        List<ArxivPaperDto> papers = parseArxivResponse(xml);

        // 일치하는 URL이 없는 경우
        if (papers.isEmpty()) {
            throw new IllegalArgumentException("No paper found for the given URL.");
        }

        // 3. 첫 번째 논문 DTO를 선택하고, 해당 PDF를 서버에 자동 다운로드함
        ArxivPaperDto dto = papers.getFirst();
        try {
            // 다운로드 성공 시 arXiv ID 기반으로 저장
            String savedPath = pdfDownloadService.downloadFromArxivUrl(dto.getPdfUrl());
            dto.setLocalPdfPath(savedPath);
        } catch (IOException e) {
            // 다운로드 실패 시 저장 경로는 null로 설정
            log.error("[PDF Download Failed] {}", dto.getPdfUrl(), e);
        }

        return dto;
    }


    /**
     * arXiv 논문 URL에서 논문 ID만 추출
     */
    private String extractIdFromUrl(String url) {
        String last = url.substring(url.lastIndexOf('/') + 1);
        if (last.endsWith(".pdf")) last = last.substring(0, last.length() - 4);
        return last;
    }

    /**
     * XML 요소에서 텍스트 값을 추출하는 유틸 함수
     */
    private String getText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return "";
    }

}
