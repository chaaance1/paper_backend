package dddan.paper_summary.parse.infra;

import dddan.paper_summary.parse.domain.model.SectionAsset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** * NOTE:
 * 현재 구현은 PDF 텍스트 파싱의 안정성을 우선하여
 * "숫자 + 제목" 형식의 Top-level 섹션(예: "1 Introduction")만을 섹션 구분 기준으로 사용한다.
 * 숫자 없는 섹션 헤더(예: "Introduction")나 다양한 논문 포맷에 대한 지원은
 * 추후 파싱 정확도 개선 단계에서 확장 가능하도록 설계되어 있다.
 */
public final class SectionStartPageDetector {

    private SectionStartPageDetector() {}

    // SectionSplitter와 동일한 패턴(그대로 유지)
    private static final Pattern TOP_LEVEL_HEADER_PATTERN =
            Pattern.compile("^\\s*(\\d+)?:\\.?\\s+(.+?)\\s*$");

    private static final Pattern TOC_DOTS_PAGE_PATTERN =
            Pattern.compile(
                    "^\\s*(?:\\d+(?:\\.\\d+)*|[A-Z]|Appendix\\s+[A-Z])\\s+.+?\\.{2,}\\s*\\d+\\s*$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern TOC_START_PATTERN =
            Pattern.compile("^\\s*(contents|table\\s+of\\s+contents)\\s*$",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern ABSTRACT_START_PATTERN =
            Pattern.compile("^\\s*abstract\\b.*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern REFERENCES_PATTERN =
            Pattern.compile("^\\s*references\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * @param pageTexts page별 텍스트 (index+1 == pageNumber)
     * @param sections  SectionSplitter 결과(섹션 개수/순서 힌트용)
     */
    public static Map<Integer, Integer> detect(List<String> pageTexts,
                                               List<SectionAsset> sections) {

        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (pageTexts == null || pageTexts.isEmpty()) return result;
        if (sections == null || sections.isEmpty()) return result;

        boolean skippingTocBlock = false;
        boolean skippingAbstractBlock = false;
        Integer lastAcceptedSectionNo = null;

        int sectionOrder = 0;

        for (int pageIdx = 0; pageIdx < pageTexts.size(); pageIdx++) {
            int pageNumber = pageIdx + 1;
            String pageText = pageTexts.get(pageIdx);
            if (pageText == null || pageText.isBlank()) continue;

            String[] lines = pageText.split("\\R");

            for (String raw : lines) {
                String line = raw.trim();
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

                // References 만나면 종료
                if (REFERENCES_PATTERN.matcher(line).matches()) return result;

                Matcher m = TOP_LEVEL_HEADER_PATTERN.matcher(line);

                boolean isValidTopHeader = false;
                int headerNo = -1;

                if (m.matches()) {
                    // Splitter는 여기서 parseInt를 바로 해서 터질 수 있음.
                    // Detector는 안전하게 숫자 유무부터 확인.
                    String headerNoStr = m.group(1);
                    // String headerTitle = m.group(2); // 필요하면 사용 가능(지금은 start page만)

                    // 숫자가 없으면(top-level로 인정 불가) => 그냥 후보 탈락 처리
                    if (headerNoStr != null && !headerNoStr.isBlank()) {
                        try {
                            headerNo = Integer.parseInt(headerNoStr);

                            // Splitter와 동일한 증가 규칙
                            if (lastAcceptedSectionNo == null) {
                                isValidTopHeader = (headerNo == 1);
                            } else {
                                isValidTopHeader = (headerNo == lastAcceptedSectionNo + 1);
                            }
                        } catch (NumberFormatException ignore) {
                        }
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

                // 유효한 top-level 헤더면 섹션 시작 페이지 기록
                if (isValidTopHeader) {
                    sectionOrder++;
                    result.put(sectionOrder, pageNumber);
                    lastAcceptedSectionNo = headerNo;
                    break; // 한 페이지에서 섹션 시작은 1번만
                }
            }

            // (선택) 이미 Splitter가 만든 섹션 개수만큼 찾았으면 조기 종료 가능
            if (sectionOrder >= sections.size()) break;
        }

        return result;
    }
}
