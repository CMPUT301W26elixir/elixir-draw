package com.example.allot.controller.organizer;

import com.example.allot.model.organizer.EntrantExportRow;
import java.util.List;

/**
 * Formats enrolled entrant details into CSV text.
 */
public class EventEntrantsCsvFormatter {
    private static final String HEADER = "Name,Email,Phone";

    /**
     * Converts entrant export rows into CSV text.
     *
     * @param rows the rows to include after the header
     * @return CSV text with a header and one line per row
     */
    public String format(List<EntrantExportRow> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER);

        if (rows == null || rows.isEmpty()) {
            return builder.toString();
        }

        for (EntrantExportRow row : rows) {
            builder.append('\n')
                    .append(escapeCell(row == null ? null : row.getName()))
                    .append(',')
                    .append(escapeCell(row == null ? null : row.getEmail()))
                    .append(',')
                    .append(escapeCell(row == null ? null : row.getPhone()));
        }

        return builder.toString();
    }

    private String escapeCell(String value) {
        String safeValue = value == null ? "" : value;
        boolean requiresQuotes = safeValue.contains(",")
                || safeValue.contains("\"")
                || safeValue.contains("\r")
                || safeValue.contains("\n");

        if (!requiresQuotes) {
            return safeValue;
        }

        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
