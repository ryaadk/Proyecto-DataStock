package es.iesisidra.datastock.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.config.XmlConfig;

/**
 * Encapsula la lectura y escritura de documentos XML con DOM.
 * Los recursos se cierran automáticamente con try-with-resources.
 */
public class AlumnoXmlRepository {
    public Document leer(Path ruta) throws IOException, SAXException, ParserConfigurationException {
        // El parser usa la configuración segura compartida para evitar XXE.
        try (InputStream entrada = Files.newInputStream(ruta)) {
            Document documento = XmlConfig.domFactory().newDocumentBuilder().parse(entrada);
            documento.getDocumentElement().normalize();
            return documento;
        }
    }

    public void guardar(Document documento, Path ruta) throws IOException, TransformerException {
        Path padre = ruta.toAbsolutePath().getParent();
        if (padre != null) Files.createDirectories(padre);
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformador = factory.newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        try (OutputStream salida = Files.newOutputStream(ruta)) {
            transformador.transform(new DOMSource(documento), new StreamResult(salida));
        }
    }
}
