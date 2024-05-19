package com.mono.miscellaneous.common.utilities;

import org.springframework.stereotype.Component;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

@Component
public class XMLHelper {
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;

    public XMLHelper() throws ParserConfigurationException
    {
        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
    }

    public Document parseOutputStream(InputStream stream) throws SAXException, IOException {
        try {
            return dBuilder.parse(stream);

        } catch (SAXException | IOException e) {
            throw e;
        }
        finally {
            stream.close();
        }
    }

    public Document parseOutputString(String stream) throws SAXException, IOException {
        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(stream));
            return dBuilder.parse(stream);
        } catch (SAXException | IOException e) {
            throw e;
        }
        finally {

        }
    }

    public Node parseOutputStringToNode(String stream) throws SAXException, IOException, ParserConfigurationException {
        try {
            return dBuilder.parse(new InputSource(new StringReader(stream)));
        } catch (SAXException | IOException e) {
            throw e;
        }
        finally {

        }
    }

    public NodeList getNodeListFromDocument(Document doc, String tagName)
    {
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(tagName);
    }

    public NodeList getNodeListFromInputStream(InputStream stream, String tagName) throws IOException, SAXException {
        return getNodeListFromDocument(parseOutputStream(stream), tagName);
    }

    public String getStringFromElementTag(Element eElement, String tagName)
    {
        return eElement.getElementsByTagName(tagName).item(0).getTextContent();
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}
