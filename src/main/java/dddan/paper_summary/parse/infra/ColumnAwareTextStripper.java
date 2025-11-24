package dddan.paper_summary.parse.infra;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.*;

public class ColumnAwareTextStripper extends PDFTextStripper {

    private static class LineInfo {
        final float midX;
        final String text;

        LineInfo(float midX, String text) {
            this.midX = midX;
            this.text = text;
        }
    }

    // 페이지별 표 영역 정보
    private final Map<Integer, List<TableRect>> tableRegionsByPage;

    private final List<LineInfo> currentPageLines = new ArrayList<>();

    public ColumnAwareTextStripper(Map<Integer, List<TableRect>> tableRegionsByPage) throws IOException {
        // null 방어
        this.tableRegionsByPage = (tableRegionsByPage != null) ? tableRegionsByPage : Collections.emptyMap();
        // 좌표 기준 정렬을 켜줘야 X기반 판별이 잘 됨
        setSortByPosition(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        currentPageLines.clear();
    }

    // 현재 페이지에서 사용될 표 영역만 가져오기
    private List<TableRect> getCurrentPageTables() {
        int pageNo = getCurrentPageNo(); // PDFTextStripper가 관리하는 페이지 번호 (1-based)
        List<TableRect> list = tableRegionsByPage.get(pageNo);
        return (list != null) ? list : Collections.emptyList();
    }

    // 이 TextPosition이 어떤 표 영역 안에 들어가는지 체크
    private boolean isInsideAnyTable(TextPosition pos) {
        float px = pos.getXDirAdj();
        float py = pos.getYDirAdj();

        for (TableRect r : getCurrentPageTables()) {
            if (px >= r.getX() && px <= r.getX() + r.getWidth()
                    && py >= r.getY() && py <= r.getY() + r.getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        // 1) 이 줄이 표 영역 안에 있는지 먼저 검사
        for (TextPosition pos : textPositions) {
            if (isInsideAnyTable(pos)) {
                // 표 안 텍스트 → 텍스트 추출에서 완전히 스킵
                return;
            }
        }

        // 2) 기존 컬럼 감지용 로직 그대로
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            // 빈 줄은 X=0으로 저장해두고 나중에 그대로 흘려보낼 수 있음
            currentPageLines.add(new LineInfo(0f, text));
            return;
        }

        float sumX = 0f;
        int count = 0;
        for (TextPosition pos : textPositions) {
            sumX += pos.getXDirAdj();
            count++;
        }
        float midX = (count == 0) ? 0f : sumX / count;
        currentPageLines.add(new LineInfo(midX, text));
    }

    @Override
    protected void endPage(PDPage page) throws IOException {
        if (currentPageLines.isEmpty()) {
            super.endPage(page);
            return;
        }

        // midX 값들만 추출 (빈 줄 제외)
        List<Float> xs = new ArrayList<>();
        for (LineInfo li : currentPageLines) {
            if (li.text.trim().isEmpty()) continue;
            xs.add(li.midX);
        }

        if (xs.size() < 8) {
            // 라인이 너무 적으면 그냥 1컬럼 취급
            flushOneColumn();
        } else {
            Collections.sort(xs);
            float first = xs.getFirst();
            float last = xs.getLast();

            float maxGap = 0f;
            float threshold = (first + last) / 2f;

            for (int i = 0; i < xs.size() - 1; i++) {
                float gap = xs.get(i + 1) - xs.get(i);
                if (gap > maxGap) {
                    maxGap = gap;
                    threshold = (xs.get(i) + xs.get(i + 1)) / 2f;
                }
            }

            float totalRange = last - first;
            boolean twoColumn = (totalRange > 0) && (maxGap > totalRange * 0.4f);

            if (!twoColumn) {
                flushOneColumn();
            } else {
                flushTwoColumns(threshold);
            }
        }

        currentPageLines.clear();
        super.endPage(page);
    }

    private void flushOneColumn() throws IOException {
        for (LineInfo li : currentPageLines) {
            super.writeString(li.text);
            super.writeLineSeparator();
        }
    }

    private void flushTwoColumns(float threshold) throws IOException {
        StringBuilder left = new StringBuilder();
        StringBuilder right = new StringBuilder();

        boolean firstLeft = true;
        boolean firstRight = true;

        for (LineInfo li : currentPageLines) {
            String trimmed = li.text.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (li.midX < threshold) {
                if (!firstLeft) left.append(getLineSeparator());
                left.append(li.text);
                firstLeft = false;
            } else {
                if (!firstRight) right.append(getLineSeparator());
                right.append(li.text);
                firstRight = false;
            }
        }

        if (!left.isEmpty()) {
            super.writeString(left.toString());
            super.writeLineSeparator();
        }
        if (!right.isEmpty()) {
            super.writeString(right.toString());
            super.writeLineSeparator();
        }
    }
}
