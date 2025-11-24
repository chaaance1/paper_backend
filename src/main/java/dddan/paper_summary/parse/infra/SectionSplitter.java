package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.model.SectionAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SectionSplitter {

    // 예: "1 Introduction", "2. Related Work", "3.1 Method"
    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^\\s*(\\d+(?:\\.\\d+)*)\\s+(.+)$");

    private SectionSplitter() {}

    public static List<SectionAsset> split(Long paperId, String fullText) {
        String[] lines = fullText.split("\\R");
        List<SectionAsset> sections = new ArrayList<>();

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();
        int sectionOrder = 0;

        for (String line : lines) {
            Matcher m = HEADER_PATTERN.matcher(line);
            if (m.matches()) {
                // 이전 섹션 저장
                if (currentTitle != null) {
                    sections.add(SectionAsset.builder()
                            .paperId(paperId)
                            .sectionOrder(sectionOrder)
                            .title(currentTitle)
                            .content(currentContent.toString().trim())
                            .build());
                }
                // 새 섹션 시작
                sectionOrder++;
                currentTitle = line.trim();
                currentContent.setLength(0);
            } else {
                if (currentContent.length() > 0) currentContent.append("\n");
                currentContent.append(line);
            }
        }

        // 마지막 섹션 저장
        if (currentTitle != null) {
            sections.add(SectionAsset.builder()
                    .paperId(paperId)
                    .sectionOrder(sectionOrder)
                    .title(currentTitle)
                    .content(currentContent.toString().trim())
                    .build());
        }

        // 한 번도 못 나눴으면 그냥 fullText 통짜 하나로
        if (sections.isEmpty()) {
            sections.add(SectionAsset.builder()
                    .paperId(paperId)
                    .sectionOrder(1)
                    .title("FULL_TEXT")
                    .content(fullText.trim())
                    .build());
        }

        return sections;
    }
}
