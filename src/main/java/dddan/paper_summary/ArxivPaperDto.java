package dddan.paper_summary;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArxivPaperDto {
    private String title;
    private String summary;
    private String published;
    private String updated;
    private String authors;
    private String idUrl;        // https://arxiv.org/abs/...
    private String pdfUrl;       // https://arxiv.org/pdf/...
    private String localPdfPath; // 서버에 저장된 로컬 경로 (예: papers/2401.00001.pdf)
    private String downloadUrl;  // 프론트엔드에서 접근 가능한 다운로드 링크 (필요시)

    // 생성자 (localPdfPath와 downloadUrl은 setter로 추후 설정)
    public ArxivPaperDto(String title, String summary, String published, String updated,
                         String authors, String idUrl, String pdfUrl) {
        this.title = title;
        this.summary = summary;
        this.published = published;
        this.updated = updated;
        this.authors = authors;
        this.idUrl = idUrl;
        this.pdfUrl = pdfUrl;
    }

}
