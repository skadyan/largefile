package com.stackoverlfow.largefile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

public class SheetContentsHandlerImpl implements SheetContentsHandler {

    private RecordHandler recordHandler;
    private Map<String, String> formattedValues;
    private String sheetName;

    public SheetContentsHandlerImpl(String sheetName, RecordHandler recordHandler) {
        this.sheetName = sheetName;
        this.recordHandler = recordHandler;
    }

    @Override
    public void startRow(int rowNum) {
        this.formattedValues = new LinkedHashMap<>();
    }

    @Override
    public void endRow(int rowNum) {
        recordHandler.onRow(sheetName, rowNum, formattedValues);
        formattedValues = null;
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        formattedValues.put(cellReference, formattedValue);
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        // ignore
    }

}
