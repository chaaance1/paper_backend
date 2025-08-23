package dddan.paper_summary;

import org.w3c.dom.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.URI;
import java.net.URL;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ArxivService {

    // 로그 생성을 위한 Logger 객체 생성
    private static final Logger log = LoggerFactory.getLogger(ArxivService.class);


    private static final String PDF_DIR = "pdfs";

    // HTTP 요청 전송을 위한 도구
    private final RestTemplate restTemplate = new RestTemplate();

    // PDF 파일을 실제로 다운로드하는 기능을 담당하는 서비스
    private final PdfDownloadService pdfDownloadService;

    /**
     * 사용자가 입력한 검색어를 바탕으로 arXiv 논문 검색 API를 호출함
     * 결과는 XML 형식의 문자열로 반환됨
     */
    public String searchPapersByTitle(String query) {
        // arXiv API의 요청 URL을 구성함
        URI uri = UriComponentsBuilder
                .fromUriString("https://export.arxiv.org/api/query")
                .queryParam("search_query", "all:" + query)
                .queryParam("start", 0)
                .queryParam("max_results", 5)
                .queryParam("sortBy", "relevance")
                .queryParam("sortOrder", "descending")
                .build()
                .encode()
                .toUri();

        // 본문 내용 추출해서 반환
        String body = restTemplate.getForObject(uri, String.class);
        return body == null ? "" : body;
    }

    /**
     * 사용자가 논문 링크를 직접 입력했을 때, 해당 논문의 arXiv ID를 추출해 논문의 상세 정보를 요청
     */
    public String fetchPaperByUrl(String url) {
        // 논문 ID 추출 (예: https://arxiv.org/pdf/2401.00001.pdf → 2401.00001)
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

        // 요청 결과 반환

    }

    // 논문 URL에서 ID만 추출하는 유틸리티 함수
    private String extractIdFromUrl(String url) {
        String last = url.substring(url.lastIndexOf('/') + 1);
        if (last.endsWith(".pdf")) last = last.substring(0, last.length() - 4);
        return last;
    }

    /**
     * arXiv API로부터 받은 XML 데이터를 파싱하여 논문 목록을 만들어냄
     * 각 논문은 ArxivPaperDto 객체로 표현
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

    // XML 태그에서 텍스트 값을 추출하는 함수
    private String getText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return "";
    }

    /**
     * 검색어로 논문을 조회한 뒤, 각 논문의 PDF를 자동으로 다운로드하고 저장 경로를 DTO에 포함시킴
     */
    public List<ArxivPaperDto> searchAndDownloadPapers(String query) {
        String xml = searchPapersByTitle(query); // arXiv 검색 결과 XML 받아오기
        List<ArxivPaperDto> papers = parseArxivResponse(xml); // XML 파싱해서 논문 리스트로 변환

        for (ArxivPaperDto dto : papers) {
            try {
                // PDF 자동 다운로드
                String savedPath = pdfDownloadService.downloadFromArxivUrl(dto.getPdfUrl());
                dto.setLocalPdfPath(savedPath); // 저장된 경로를 DTO에 기록
            } catch (Exception e) {
                log.error("[PDF Download Failed] {}", dto.getTitle(), e);
            }
        }

        return papers;
    }

    /**
     * 사용자가 arXiv 논문 URL을 입력했을 때 수행되는 메서드.
     *
     * 전체 동작 흐름:
     * 1. 입력된 URL에서 논문 ID를 추출하여 arXiv API를 호출함
     *    - 내부적으로 fetchPaperByUrl(url) → arXiv API 호출 (id_list 기반 요청)
     * 2. 응답받은 XML 데이터를 파싱하여 ArxivPaperDto 객체로 변환
     *    - parseArxivResponse(xml): <entry> 항목을 객체로 변환
     * 3. 첫 번째 논문 DTO를 선택하고, 해당 PDF를 서버에 자동 다운로드함
     *    - pdfDownloadService.downloadPdfToServer(pdfUrl, title): 서버의 지정 디렉토리에 PDF 저장
     * 4. 저장된 파일 경로를 DTO의 localPdfPath 필드에 기록함
     *
     * 최종적으로:
     * - 메타데이터가 포함된 ArxivPaperDto 객체 반환
     * - PDF는 서버에 저장된 상태
     * - 실패 시 localPdfPath는 null로 남겨짐
     *
     * @param url 사용자가 입력한 arXiv 논문 URL (예: https://arxiv.org/abs/2401.00001)
     * @return PDF 경로가 포함된 ArxivPaperDto 객체
     * @throws IllegalArgumentException 논문을 찾을 수 없을 경우 예외 발생
     */
    public ArxivPaperDto fetchAndDownloadByUrl(String url) {
        // 1. 입력된 URL에서 논문 ID를 추출하여 arXiv API를 호출함
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


}
