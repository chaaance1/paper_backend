package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.model.SectionAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SectionSplitter {

    /**
     * NOTE:
     * 현재 구현은 PDF 텍스트 파싱의 안정성을 우선하여
     * "숫자 + 제목" 형식의 Top-level 섹션(예: "1 Introduction")만을
     * 섹션 구분 기준으로 사용한다.
     * 숫자 없는 섹션 헤더(예: "Introduction")나
     * 다양한 논문 포맷에 대한 지원은
     * 추후 파싱 정확도 개선 단계에서 확장 가능하도록 설계되어 있다.
     */

    private static final Pattern TOP_LEVEL_HEADER_PATTERN =
            Pattern.compile("^\\s*(\\d+)?:\\.?\\s+(.+?)\\s*$");

    /**
     *  목차 점선 리더 + 페이지 번호 라인 제거
     * 예)
     * - 1 Introduction .......... 3
     * - 2.1 Machine Translation .... 5
     * - Appendix A .... 12
     */
    private static final Pattern TOC_DOTS_PAGE_PATTERN =
            Pattern.compile("^\\s*(?:\\d+(?:\\.\\d+)*|[A-Z]|Appendix\\s+[A-Z])\\s+.+?\\.{2,}\\s*\\d+\\s*$",
                    Pattern.CASE_INSENSITIVE);

    /**  목차 시작 감지(블록 스킵용) */
    private static final Pattern TOC_START_PATTERN =
            Pattern.compile("^\\s*(contents|table\\s+of\\s+contents)\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * Abstract 시작 감지
     * - "Abstract" 단독 라인뿐 아니라
     * - "Abstract. This paper ..."처럼 같은 줄에 붙어도 감지
     */
    private static final Pattern ABSTRACT_START_PATTERN =
            Pattern.compile("^\\s*abstract\\b.*$", Pattern.CASE_INSENSITIVE);

    /** (선택) References제거 */
    private static final Pattern REFERENCES_PATTERN =
            Pattern.compile("^\\s*references\\s*$", Pattern.CASE_INSENSITIVE);

    private SectionSplitter() {}

    public static List<SectionAsset> split(Long paperId, String fullText) {
        String[] lines = fullText.split("\\R");
        List<SectionAsset> sections = new ArrayList<>();

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();
        int sectionOrder = 0;

        boolean skippingTocBlock = false;
        boolean skippingAbstractBlock = false;

        Integer lastAcceptedSectionNo = null;

        for (String raw : lines) {
            String line = (raw == null) ? "" : raw.trim();
            if (line.isEmpty()) continue;

            // 목차 점선+페이지번호 제거
            if (TOC_DOTS_PAGE_PATTERN.matcher(line).matches()) continue;

            // 목차 블록 시작
            if (!skippingTocBlock && TOC_START_PATTERN.matcher(line).matches()) {
                skippingTocBlock = true;
                continue;
            }

            // Abstract 시작
            if (!skippingAbstractBlock && ABSTRACT_START_PATTERN.matcher(line).matches()) {
                skippingAbstractBlock = true;
                continue;
            }

            // References 만나면 종료(원하면 break 제거)
            if (REFERENCES_PATTERN.matcher(line).matches()) break;

            Matcher m = TOP_LEVEL_HEADER_PATTERN.matcher(line);

            // Top-level 헤더 후보면 추가 검증
            boolean isValidTopHeader = false;
            int headerNo = -1;
            String headerTitle = null;

            if (m.matches()) {
                headerNo = Integer.parseInt(m.group(1));
                headerTitle = m.group(2);

                // 핵심 규칙:
                // - 첫 섹션은 보통 1부터 시작(최소 1 허용)
                // - 섹션 번호는 단조 증가(이전보다 커야 함)
                //   => 본문 리스트의 "1 ..., 2 ..." 같은 반복 번호를 헤더로 오인 방지
                if (lastAcceptedSectionNo == null) {
                    isValidTopHeader = (headerNo == 1);   // 첫 헤더는 1만 허용(필요하면 1~2 허용으로 완화 가능)
                } else {
                    isValidTopHeader = (headerNo == lastAcceptedSectionNo + 1);
                    // 논문에 섹션 번호가 건너뛰는 경우(1,2,4)도 허용하려면:
                    // isValidTopHeader = (headerNo > lastAcceptedSectionNo);
                }
            }

            // 목차/Abstract 스킵 처리 (유효 헤더 만나면 종료)
            if (skippingTocBlock) {
                if (isValidTopHeader) skippingTocBlock = false;
                else continue;
            }
            if (skippingAbstractBlock) {
                if (isValidTopHeader) skippingAbstractBlock = false;
                else continue;
            }

            // 유효한 top-level 헤더면 섹션 전환
            if (isValidTopHeader) {
                // 이전 섹션 저장
                if (currentTitle != null) {
                    sections.add(SectionAsset.builder()
                            .paperId(paperId)
                            .sectionOrder(sectionOrder)
                            .title(currentTitle)
                            .content(currentContent.toString().trim())
                            .build());
                }

                sectionOrder++;
                currentTitle = headerNo + " " + headerTitle;
                currentContent.setLength(0);

                lastAcceptedSectionNo = headerNo;
                continue;
            }

            // 첫 유효 섹션 전 텍스트는 버림(표지/저자/키워드 등)
            if (currentTitle == null) continue;

            // 본문 누적
            if (!currentContent.isEmpty()) currentContent.append("\n");
            currentContent.append(line);
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

        // 섹션을 하나도 못 찾으면 통짜
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
