package com.stackoverlfow.largefile;

import java.util.Map;

public interface RecordHandler {
    /**
     * @param sheet
     * @param rowNum
     * @param formattedData
     *            key is the cell reference (e.g. A1, A2, B5 etc.) and data is
     *            formatted value
     */
    void onRow(String sheet, int rowNum, Map<String, String> formattedData);
}
