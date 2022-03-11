package com.wisea.cloud.wbfceditor.generator.config;

import org.mybatis.generator.codegen.XmlConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

public class WbfcParserEntityResolver implements EntityResolver {

    public WbfcParserEntityResolver() {
        super();
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (XmlConstants.MYBATIS_GENERATOR_CONFIG_PUBLIC_ID
                .equalsIgnoreCase(publicId)) {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "config/wbfc-generator-config.dtd"); //$NON-NLS-1$
            return new InputSource(is);
        } else {
            return null;
        }
    }
}
