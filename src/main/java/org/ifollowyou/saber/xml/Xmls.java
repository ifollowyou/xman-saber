package org.ifollowyou.saber.xml;


import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Xmls {
    /**
     * 将无格式的XML文件格式化。
     *
     * @param xml            文件
     * @param namespaceAware 是否启用命名空间
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void formatXml(File xml, boolean namespaceAware)
            throws ParserConfigurationException, SAXException, IOException {
        formatXml(xml, namespaceAware, "UTF-8");
    }

    /**
     * 将无格式的XML文件格式化。
     *
     * @param xml            文件
     * @param namespaceAware 是否启用命名空间
     * @param charset        字符集
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void formatXml(File xml, boolean namespaceAware, String charset)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(namespaceAware);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = new DOMReader().read(builder.parse(xml));

        XMLWriter writer = null;
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(xml, false), charset)) {
            // 备份文件
            Path source = Paths.get(xml.toURI());
            Path target = Paths.get(source.toFile().getAbsolutePath() + ".bak");
            Files.copy(source, target);
            // 格式化
            OutputFormat format = new OutputFormat("    ", true, charset);
            writer = new XMLWriter(osw, format);
            writer.write(document);
            writer.flush();
            osw.flush();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }
}
