package dddan.paper_summary.parse.infra;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.*;

/**
 * ColumnAwareTextStripper
 * PDFBox 기본 TextStripper는 2단(두 컬럼) 논문에서 왼쪽/오른쪽 컬럼을 섞어서 읽는 문제가 있음.
 * 이 클래스는:
 * 1) 텍스트 조각들을 y좌표 기준으로 "줄" 단위로 묶고
 * 2) 각 줄의 x좌표 분포를 분석해 1단 / 2단을 판단한 뒤
 * 3) 2단이면 왼쪽 컬럼 → 오른쪽 컬럼 순서로 재정렬하여 출력함
 */

public class ColumnAwareTextStripper extends PDFTextStripper {

    // "한 줄"을 표현하는 정보
    private static class LineInfo {
        final float y;              // 줄의 세로 위치(위치 기준)
        float midX;                 // 줄의 가로 중심 (컬럼 판별용)
        final StringBuilder text;   // 이 줄의 전체 텍스트
        int chunks;                 // 이 줄에 합쳐진 조각 개수

        LineInfo(float y, float midX, String firstText) {
            this.y = y;
            this.midX = midX;
            this.text = new StringBuilder(firstText);
            this.chunks = 1;
        }

        void append(float midX, String more) {
            // 앞에 내용이 있고, 공백 없이 붙을 것 같으면 공백 하나 넣어주기
            if (!text.isEmpty()
                    && !Character.isWhitespace(text.charAt(text.length() - 1))
                    && !more.isEmpty()
                    && !Character.isWhitespace(more.charAt(0))) {
                text.append(' ');
            }
            text.append(more);

            // midX는 평균값으로 업데이트
            this.midX = (this.midX * chunks + midX) / (chunks + 1);
            this.chunks++;
        }
    }

    private final List<LineInfo> currentPageLines = new ArrayList<>();

    public ColumnAwareTextStripper() throws IOException {
        // X/Y 좌표 기준으로 정렬해서 넘겨달라고 설정
        setSortByPosition(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        currentPageLines.clear();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            // 완전 공백 조각은 버려도 됨 (줄 간 공백은 y 값으로 구분됨)
            return;
        }

        float sumX = 0f;
        float sumY = 0f;
        int count = 0;
        for (TextPosition pos : textPositions) {
            sumX += pos.getXDirAdj();
            sumY += pos.getYDirAdj();
            count++;
        }

        float midX = (count == 0 ? 0f : sumX / count);
        float midY = (count == 0 ? 0f : sumY / count);

        // 🔹 Y 좌표가 비슷한 애들끼리 같은 "줄"로 합치기
        final float lineMergeTolerance = 2.0f; // 같은 줄로 볼 Y 오차 범위
        LineInfo target = null;
        for (LineInfo li : currentPageLines) {
            if (Math.abs(li.y - midY) <= lineMergeTolerance) {
                target = li;
                break;
            }
        }

        if (target == null) {
            currentPageLines.add(new LineInfo(midY, midX, text));
        } else {
            target.append(midX, text);
        }
    }

    @Override
    protected void endPage(PDPage page) throws IOException {
        if (currentPageLines.isEmpty()) {
            super.endPage(page);
            return;
        }

        // 🔹 위에서 아래로 정렬 (줄 순서)
        currentPageLines.sort(Comparator.comparing(li -> li.y));

        // midX 값들만 추출 (컬럼 판단용)
        List<Float> xs = new ArrayList<>();
        for (LineInfo li : currentPageLines) {
            String trimmed = li.text.toString().trim();
            if (trimmed.isEmpty()) continue;
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
            String line = li.text.toString().trim();
            if (line.isEmpty()) continue;

            super.writeString(line);
            super.writeLineSeparator();
        }
    }

    private void flushTwoColumns(float threshold) throws IOException {
        List<LineInfo> left = new ArrayList<>();
        List<LineInfo> right = new ArrayList<>();

        for (LineInfo li : currentPageLines) {
            String line = li.text.toString().trim();
            if (line.isEmpty()) continue;

            if (li.midX < threshold) {
                left.add(li);
            } else {
                right.add(li);
            }
        }

        // 이미 y 기준으로 정렬돼 있음 (start에서 sort 했으니까)

        // 왼쪽 컬럼 먼저 다 출력
        for (LineInfo li : left) {
            super.writeString(li.text.toString().trim());
            super.writeLineSeparator();
        }

        // 컬럼 사이에 빈 줄 하나 정도 넣고 싶으면:
        if (!left.isEmpty() && !right.isEmpty()) {
            super.writeLineSeparator();
        }

        // 오른쪽 컬럼 출력
        for (LineInfo li : right) {
            super.writeString(li.text.toString().trim());
            super.writeLineSeparator();
        }
    }
}
