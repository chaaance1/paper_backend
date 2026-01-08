package dddan.paper_summary.parse.infra;

import technology.tabula.Table;
/**
 * Tabula Table 객체를 CSV 문자열로 변환하는 유틸리티 클래스
 * (인프라 계층 전용, 외부 라이브러리 의존)
 */

final class TabulaMapper {
    private TabulaMapper() {}

    /**
     * Tabula Table → CSV 문자열 변환
     *
     * @param table PDF에서 추출된 표 객체
     * @return CSV 형식의 문자열
     */

    static String toCSV(Table table) {
        StringBuilder sb = new StringBuilder();
        table.getRows().forEach(row -> {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i).getText().replace("\"", "\"\"");
                sb.append("\"").append(cell).append("\"");
                if (i < row.size() - 1) sb.append(",");
            }
            sb.append("\n");
        });
        return sb.toString();
    }
}
