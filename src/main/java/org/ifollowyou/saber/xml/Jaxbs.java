package org.ifollowyou.saber.xml;


import org.ifollowyou.saber.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;

public final class Jaxbs {

    private static Unmarshaller getUnmarshaller(Class T) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(T);

        return jaxbContext.createUnmarshaller();
    }

    public static Object xml2Object(File xml, Class T) throws JAXBException {
        return xml.exists() ? getUnmarshaller(T).unmarshal(xml) : null;
    }

    public static Object xml2Object(InputStream is, Class T) throws JAXBException {
        return Objects.isNotNull(is) ? getUnmarshaller(T).unmarshal(is) : null;
    }

    public static Object xml2Object(Reader reader, Class T) throws JAXBException {
        return getUnmarshaller(T).unmarshal(reader);
    }

}
