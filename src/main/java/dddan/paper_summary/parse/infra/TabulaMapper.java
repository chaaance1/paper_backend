package dddan.paper_summary.parse.infra;

import technology.tabula.Table;

final class TabulaMapper {
    private TabulaMapper() {}

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
