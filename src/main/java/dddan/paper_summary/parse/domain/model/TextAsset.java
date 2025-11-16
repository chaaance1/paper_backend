package dddan.paper_summary.parse.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextAsset {
    private String text;  // 해당 페이지의 텍스트 내용

    // PdfboxTextExtractor에서 사용 가능한 정적 팩토리 메서드 추가
    public static TextAsset of(String text) {
        return TextAsset.builder()
                .text(text)
                .build();
    }
}
