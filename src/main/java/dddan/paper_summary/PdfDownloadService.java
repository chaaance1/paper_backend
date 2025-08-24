package dddan.paper_summary;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;


/**
 * arXiv 논문의 PDF를 로컬 서버에 다운로드하여 저장하는 서비스 클래스.
 * 주요 기능:
 *  - abs 링크를 pdf 링크로 변환
 *  - .pdf 확장자 보정
 *  - 파일 이름을 논문 ID 기반으로 생성
 *  - 다운로드 결과를 지정된 디렉토리에 저장
 */
@Service
public class PdfDownloadService {

    // PDF 저장 디렉토리
    private static final String PDF_DIR = "pdfs";

    /**
     * arXiv의 abs 또는 pdf 링크에서 PDF 다운로드
     * @param url arXiv의 abs 링크 또는 pdf 링크
     * @return 저장된 PDF 파일의 절대 경로
     * @throws IOException 다운로드 실패 시
     */
    public String downloadFromArxivUrl(String url) throws IOException {
        String normalized = normalizeArxivPdfUrl(url);      // abs→pdf, .pdf 보정
        String fileName = buildPdfFileNameFromUrl(normalized); // 안전한 파일명

        Path filePath = Paths.get(PDF_DIR, fileName);
        download(normalized, filePath);
        return filePath.toAbsolutePath().toString();
    }

    /**
     * 입력된 링크를 pdf 다운로드 가능한 링크로 정규화
     * @param url 원본 arXiv 링크
     * @return 정규화된 PDF 다운로드 링크
     */
    private String normalizeArxivPdfUrl(String url) {
        // abs 링크면 → pdf + .pdf
        if (url.contains("arxiv.org/abs/")) {
            url = url.replace("/abs/", "/pdf/");
        }
        // pdf 링크에.pdf가 없으면 붙여줌
        if (url.contains("arxiv.org/pdf/") && !url.endsWith(".pdf")) {
            url = url + ".pdf";
        }

        // http → https 보정 추가
        if (url.startsWith("http://")) {
            url = url.replace("http://", "https://");
        }
        return url;
    }

    /**
     * arXiv PDF 링크에서 논문 ID 추출 -> 저장할 PDF 파일 이름 반환하는 함수
     * @param url arXiv PDF URL
     * @return 저장할 파일 이름
     */
    private String buildPdfFileNameFromUrl(String url) {
        // 마지막 세그먼트 추출
        String last = url.substring(url.lastIndexOf('/') + 1);
        // 쿼리스트립 + 확장자 보정
        int q = last.indexOf('?');
        if (q >= 0) last = last.substring(0, q);
        if (!last.endsWith(".pdf")) last = last + ".pdf";
        return last;
    }

    /**
     * HTTP GET 방식으로 URL에서 파일을 다운로드하여 지정된 경로에 저장
     * @param url      다운로드 대상 URL
     * @param filePath 저장할 로컬 경로
     * @throws IOException 다운로드 실패 시
     */
    private void download(String url, Path filePath) throws IOException {
        Files.createDirectories(filePath.getParent());

        URL u = URI.create(url).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(60_000);

        // arXiv는 User Agent 요구: 프로젝트 식별 가능한 User Agent로 설정 권장
        conn.setRequestProperty("User-Agent", "PaperSummaryBot/1.0 (+contact: youremail@example.com)");
        conn.setRequestProperty("Accept", "application/pdf,*/*;q=0.8");

        int code = conn.getResponseCode();
        if (code / 100 != 2) {
            // 디버그: 에러 스트림 내용 함께 로깅
            try (InputStream err = conn.getErrorStream()) {
                byte[] msg = err != null ? err.readAllBytes() : new byte[0];
                throw new IOException("Failed to download PDF. HTTP " + code + " from " + url
                        + (msg.length > 0 ? " | " + new String(msg) : ""));
            }
        }

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }
    }
}
