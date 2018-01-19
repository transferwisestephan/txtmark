package com.github.rjeschke.txtmark;

import java.util.ArrayList;

/**
 * A GFM table definition
 * credit due to bogdan@quandora.com
 */
class Table {

    private int columnCount = 0;
    private ArrayList<ArrayList<String>> rows = new ArrayList<>();
    private ArrayList<String> header = new ArrayList<>();

    private void incrementColumnCount() {
        this.columnCount += 1;
    }

    public static Table parse(String headerLineText, String dividerLineText) {
        Table table = parseTableDividerLine(dividerLineText);
        if (table != null) {
            table.header = table.parseRow(headerLineText);
            if (table.header == null) {
                table = null;
            }
        }
        return table;
    }

    private static Table parseTableDividerLine(String lineText) {
        int s = Utils.skipSpaces(lineText, 0);
        if (s == -1) {
            return null; // a blank line cannot be a table divider
        }

        char c = lineText.charAt(s);
        if (c != '|') {
            return null; // must have left border
        }

        int e = Utils.skipSpacesBackwards(lineText, 0, lineText.length());
        c = lineText.charAt(e);
        if (c != '|') {
            return null; // must have right border
        }

        Table table = readTableDividerCols(lineText);
        return table;
    }

    private static Table readTableDividerCols(final String text) {
        Table table = new Table();

        int len = text.length();
        int dashCount = 0;

        for(int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c != '-' && c != '|') {
                return null;
            }
            if (c == '-') {
                dashCount += 1;
                continue;
            }

            if (dashCount < 3) {
                return null;
            }

            dashCount = 0;
            table.incrementColumnCount();
        }

        if (table.columnCount == 0) {
            return null;
        }

        return table;
    }

    public boolean addRow(String lineText) {
        ArrayList<String> row = parseRow(lineText);
        if (row != null) {
            this.rows.add(row);
            return true;
        }

        return false;
    }

    private ArrayList<String> parseRow(String lineText) {
        if (lineText == null || lineText.length() == 0) {
            return null;
        }

        int s = 0;
        int e = lineText.indexOf('|');

        if (e == -1) {
            return null;
        }

        ArrayList<String> cols = new ArrayList<>();
        do {
            String segment = lineText.substring(s, e).trim();
            cols.add(segment);
            s = e + 1;
            e = lineText.indexOf('|', s);
        } while (e != -1);

        if (s < lineText.length()) {
            String segment = lineText.substring(s).trim();
            cols.add(segment);
        } else {
            cols.add(""); // Why?
        }

        int columnCountDifference = this.columnCount - cols.size();
        if (columnCountDifference > 0) {
            while (columnCountDifference > 0) {
                cols.add("&nbsp;");
                columnCountDifference--;
            }
        } else if (columnCountDifference < 0) {
            return null;
        }

        return cols;
    }
}
