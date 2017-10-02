package com.stackoverlfow.largefile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import org.apache.poi.POIXMLTypeLoader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.xmlbeans.XmlFactoryHook;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.ctc.wstx.sax.WstxSAXParser;

public class LargeXmlFileLoader {

    private String filepath;

    private boolean hookEnabled = false;
    private boolean useWstxParse = true;

    public LargeXmlFileLoader(String filepath) {
        this.filepath = filepath;
    }

    public static void load(String filepath, RecordHandler recordHandler) throws IOException {
        Instant start = Instant.now();
        LargeXmlFileLoader loader = new LargeXmlFileLoader(filepath);
        loader.process(recordHandler);
        Instant stop = Instant.now();
        Duration duration = Duration.between(start, stop);
        System.out.printf("Duration '%s' elapsed while loading file: '%s'", duration, filepath);
    }

    private OPCPackage open() throws IOException {
        try {
            return OPCPackage.open(filepath, PackageAccess.READ);
        } catch (InvalidOperationException | InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    public void process(RecordHandler recordHandler) throws IOException {
        try {
            process0(recordHandler);
        } catch (SAXException | OpenXML4JException e) {
            throw new IOException(e);
        }
    }

    private void process0(RecordHandler recordHandler) throws IOException, SAXException, OpenXML4JException {
        POIXMLTypeLoader.DEFAULT_XML_OPTIONS.put(XmlOptions.UNSYNCHRONIZED, true);
        try (OPCPackage xlsxPackage = open()) {

            XmlFactoryHook remember = null;
            if (hookEnabled) {
                remember = XmlFactoryHook.ThreadContext.getHook();
                XmlFactoryHook.ThreadContext.setHook(new CachableXmlFactoryHook());
            }
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage) {
                @Override
                public void readFrom(InputStream is) throws IOException, SAXException {
                    if (useWstxParse) {
                        InputSource sheetSource = new InputSource(is);
                        try {
                            XMLReader sheetParser = new WstxSAXParser();
                            sheetParser.setContentHandler(this);
                            sheetParser.parse(sheetSource);
                        } catch (Exception e) {
                            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
                        }
                    }else {
                        super.readFrom(is);
                    }
                }
            };

            XSSFReader xssfReader = new XSSFReader(xlsxPackage);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
                InputStream stream = iter.next();
                String sheetName = iter.getSheetName();
                SheetContentsHandlerImpl handlerImpl = new SheetContentsHandlerImpl(sheetName, recordHandler);
                processSheet(styles, strings, handlerImpl, stream);
                stream.close();
            }

            if (hookEnabled) {
                XmlFactoryHook.ThreadContext.setHook(remember);
            }
        }
    }

    private void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, SheetContentsHandlerImpl handlerImpl,
            InputStream stream) throws SAXException, IOException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(stream);
        XmlFactoryHook remember = null;
        if (hookEnabled) {
            remember = XmlFactoryHook.ThreadContext.getHook();
            XmlFactoryHook.ThreadContext.setHook(new CachableXmlFactoryHook());
        }

        try {
            XMLReader sheetParser = useWstxParse  ? new WstxSAXParser() : SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles,
                    null,
                    strings,
                    handlerImpl,
                    formatter,
                    false);
            sheetParser.setContentHandler(handler);

            sheetParser.parse(sheetSource);
        } catch (Exception e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        } finally {
            if (hookEnabled) {
                XmlFactoryHook.ThreadContext.setHook(remember);
            }
        }

    }
}
