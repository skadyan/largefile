package com.stackoverlfow.largefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.IdentityHashMap;
import java.util.function.Function;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlFactoryHook;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.impl.store.Locale;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;

@SuppressWarnings("deprecation")
// Experimental not fully tested
public class CachableXmlFactoryHook implements XmlFactoryHook {
    private IdentityHashMap<SchemaType, XmlObject> caches = new IdentityHashMap<>();
    private IdentityHashMap<SchemaTypeLoader, Locale> localcCahce = new IdentityHashMap<>();

    @Override
    public XmlObject newInstance(SchemaTypeLoader loader, SchemaType type, XmlOptions options) {
        getLocale(loader, type, options);

        return Locale.newInstance(loader, type, options);
    }

    private void getLocale(SchemaTypeLoader loader, SchemaType type, XmlOptions options) {
        localcCahce.computeIfAbsent(loader, new Function<SchemaTypeLoader, Locale>() {

            @Override
            public Locale apply(SchemaTypeLoader t) {
                XmlObject instance = Locale.newInstance(t, type, options);

                Locale monitor = (Locale) instance.monitor();
                options.put(Locale.USE_SAME_LOCALE, monitor);
                return monitor;
            }
        });

    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, String xmlText, SchemaType type, XmlOptions options) throws XmlException {
        getLocale(loader, type, options);
        return Locale.parseToXmlObject(loader, xmlText, type, options);
    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, InputStream jiois, SchemaType type, XmlOptions options)
            throws XmlException, IOException {
        getLocale(loader, type, options);

        return Locale.parseToXmlObject(loader, jiois, type, options);
    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, XMLStreamReader xsr, SchemaType type, XmlOptions options) throws XmlException {
        getLocale(loader, type, options);

        return Locale.parseToXmlObject(loader, xsr, type, options);
    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, Reader jior, SchemaType type, XmlOptions options)
            throws XmlException, IOException {
        return Locale.parseToXmlObject(loader, jior, type, options);
    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, Node node, SchemaType type, XmlOptions options) throws XmlException {
        return Locale.parseToXmlObject(loader, node, type, options);
    }

    @Override
    public XmlObject parse(SchemaTypeLoader loader, XMLInputStream xis, SchemaType type, XmlOptions options)
            throws XmlException, XMLStreamException {
        return Locale.parseToXmlObject(loader, xis, type, options);
    }

    @Override
    public XmlSaxHandler newXmlSaxHandler(SchemaTypeLoader loader, SchemaType type, XmlOptions options) {
        getLocale(loader, type, options);

        return Locale.newSaxHandler(loader, type, options);
    }

    @Override
    public DOMImplementation newDomImplementation(SchemaTypeLoader loader, XmlOptions options) {
        return Locale.newDomImplementation(loader, options);
    }
}
