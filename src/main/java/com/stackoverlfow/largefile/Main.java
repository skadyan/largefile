package com.stackoverlfow.largefile;

import java.io.IOException;
import java.util.Map;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Thread.sleep(10000);
        String filepath = args[0];

        LargeXmlFileLoader.load(filepath, new RecordHandler() {

            public void onRow(String sheet, int rowNum, Map<String, String> data) {
                if (rowNum % 10000 == 0) {
                    // System.out.printf("Processing %s of sheet: %s\n", rowNum,
                    // sheet);
                }
            }
        });
    }
}
