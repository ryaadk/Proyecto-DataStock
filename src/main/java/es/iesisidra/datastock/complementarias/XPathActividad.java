package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.Util;

public final class XPathActividad {

    private XPathActividad() {
    }

    public static void main(String[] args) {
        String ruta = Util.leerTextoNoVacio("Ruta del fichero XML: ");
        Path path = Paths.get(ruta.trim());

        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println("ERROR: El fichero no existe o es un directorio.");
            return;
        }

        try {
            // Leer el documento XML
            ProductoXmlRepository xmlRepository = new ProductoXmlRepository();
            Document doc = xmlRepository.leer(path);

            // Comprobar la raíz
            if (doc.getDocumentElement() == null
                    || !"catalogo".equals(doc.getDocumentElement().getTagName())) {
                System.out.println("ERROR: La raíz del XML debe ser catalogo.");
                return;
            }

            // Crear XPath y ejecutar las consultas
            XPath xpath = XPathFactory.newInstance().newXPath();

            ejecutarConsultas(doc, xpath, ruta.trim());

        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.out.println("ERROR al leer el XML: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("ERROR en la consulta XPath: " + e.getMessage());
        }
    }

    // Mostrar el menú y gestionar las opciones
    private static void ejecutarConsultas(Document doc, XPath xpath, String ruta) throws XPathExpressionException {
        int opcion;

        do {
            System.out.println("---- CONSULTAS XPATH ----");
            System.out.println("Fichero: " + ruta);
            System.out.println("1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Volver");

            opcion = Util.leerOpcion("Opción: ", 0, 4);

            switch (opcion) {
                case 1 -> buscarPorCodigo(doc, xpath);
                case 2 -> buscarPorCategoria(doc, xpath);
                case 3 -> buscarPorPrecio(doc, xpath);
                case 4 -> contarProductos(doc, xpath);
                case 0 -> System.out.println("Saliendo de la consulta XPath...");
            }

        } while (opcion != 0);
    }

    // Buscar productos por código
    private static void buscarPorCodigo(Document doc, XPath xpath) throws XPathExpressionException {
        String codBuscado = Util.leerTextoNoVacio("Introduce el código: ");
        NodeList nodos = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);

        int contador = 0;

        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            String codigo = leerCampo(elem, "codigo");

            if (codigo.equalsIgnoreCase(codBuscado.trim())) {
                imprimirLineaProductoXml(elem);
                contador++;
            }
        }

        System.out.println("Productos encontrados: " + contador);
    }

    // Buscar productos por categoría
    private static void buscarPorCategoria(Document doc, XPath xpath) throws XPathExpressionException {
        int catId = Util.leerEntero("ID Categoría: ");

        if (catId <= 0) {
            System.out.println("ERROR: La categoría debe ser positiva.");
            return;
        }

        String expresion = "/catalogo/producto[categoriaId=" + catId + "]";

        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
        imprimirResultados(nodos);
    }

    // Buscar productos por precio mínimo
    private static void buscarPorPrecio(Document doc, XPath xpath) throws XPathExpressionException {
        double precioMin = Util.leerDouble("Precio mínimo: ");

        if (!Double.isFinite(precioMin) || precioMin < 0) {
            System.out.println("ERROR: Introduce un precio válido.");
            return;
        }

        String expresion = "/catalogo/producto[precio > " + precioMin + "]";

        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
        imprimirResultados(nodos);
    }

    // Contar los productos del documento
    private static void contarProductos(Document doc, XPath xpath) throws XPathExpressionException {
        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
        System.out.println("Productos encontrados: " + total.intValue());
    }

    // Imprimir los resultados de una consulta
    private static void imprimirResultados(NodeList nodos) {
        for (int i = 0; i < nodos.getLength(); i++) {
            imprimirLineaProductoXml((Element) nodos.item(i));
        }

        System.out.println("Productos encontrados: " + nodos.getLength());
    }

    // Mostrar los datos de un producto
    private static void imprimirLineaProductoXml(Element elem) {
        String cod = leerCampo(elem, "codigo");
        String nom = leerCampo(elem, "nombre");
        String precio = leerCampo(elem, "precio");
        System.out.println(cod + "   " + nom + "   " + precio + "   euros");
    }

    // Leer el contenido de un campo del producto
    private static String leerCampo(Element elem, String campo) {
        NodeList nodos = elem.getElementsByTagName(campo);

        if (nodos.getLength() == 0) {
            return "";
        }

        return nodos.item(0).getTextContent().trim();
    }
}