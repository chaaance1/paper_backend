package dddan.paper_summary.arxiv.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class ArxivPaperDto {
    private String arxivId;
    private String title;
    private String summary;

    @JsonIgnore
    private String published;

    @JsonIgnore
    private String updated;

    private List<String> authors;
    private String idUrl;
    private String pdfUrl;
    private String storageUrl;

    // 날짜만 담는 필드
    private LocalDate publishedDate;
    private LocalDate updatedDate;

    public ArxivPaperDto(String arxivId, String title, String summary, String published, String updated,
                         List<String> authors, String idUrl, String pdfUrl, String storageUrl) {
        this.arxivId = arxivId;
        this.title = title;
        this.summary = summary;
        this.published = published;
        this.updated = updated;
        this.authors = authors;
        this.idUrl = idUrl;
        this.pdfUrl = pdfUrl;
        this.storageUrl = storageUrl;

        // 생성자에서 날짜 변환
        this.publishedDate = OffsetDateTime.parse(published).toLocalDate();
        this.updatedDate = updated != null ? OffsetDateTime.parse(updated).toLocalDate() : null;
    }
}
