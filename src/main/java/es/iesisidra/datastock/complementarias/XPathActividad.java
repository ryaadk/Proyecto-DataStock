
package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.LectorConsola;

public final class XPathActividad {

    private XPathActividad() {
    }

    /**
     * Actividad independiente para realizar consultas XPath sobre un catálogo XML.
     */
    public static void main(String[] args) {

        // Valido si la ruta recibida es nula o está vacía.
        String ruta = LectorConsola.leerTexto("Ruta del fichero XML: ");

        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println("ERROR: La ruta está vacía.");
            return;
        }

        // Compruebo la existencia del fichero.
        Path path = Paths.get(ruta.trim());

        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println("ERROR: El fichero no existe o es un directorio.");
            return;
        }

        try {
            // Leo el documento utilizando el repositorio del proyecto.
            ProductoXmlRepository xmlRepository = new ProductoXmlRepository();
            Document doc = xmlRepository.leer(path);

            // Compruebo que la raíz del documento sea catalogo.
            if (doc.getDocumentElement() == null
                    || !"catalogo".equals(doc.getDocumentElement().getTagName())) {
                System.out.println("ERROR: La raíz del XML debe ser catalogo.");
                return;
            }

            // Creo el evaluador XPath para ejecutar expresiones sobre el documento.
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Muestro las opciones y permito realizar varias consultas.
            int opcion;
            do {
                System.out.println("---- CONSULTAS XPATH ----");
                System.out.println("Fichero: " + ruta.trim());
                System.out.println("1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Volver");

                opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);

                switch (opcion) {
                    case 1 -> {
                        // Busco por código seleccionando nodos para evitar inyección XPath.
                        String codBuscado = LectorConsola.leerTexto("Introduce el código: ");

                        if (codBuscado == null || codBuscado.trim().isEmpty()) {
                            System.out.println("ERROR: El código está vacío.");
                            break;
                        }

                        // NODESET indica que esperamos una colección de nodos.
                        NodeList nodos = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);
                        int contador = 0;

                        // Iterar por el nodo
                        for (int i = 0; i < nodos.getLength(); i++) {
                            Element elem = (Element) nodos.item(i);
                            NodeList codigos = elem.getElementsByTagName("codigo");

                            // Compruebo que exista el campo código antes de leerlo.
                            if (codigos.getLength() > 0 && codigos.item(0).getTextContent().trim().equalsIgnoreCase(codBuscado.trim())) {
                                imprimirLineaProductoXml(elem);
                                contador++;
                            }
                        }
                        System.out.println("Productos encontrados: " + contador);
                    }
                    case 2 -> {
                        // Filtro por categoría mediante un predicado XPath.
                        int catId = LectorConsola.leerEntero("ID Categoría: ");

                        if (catId <= 0) {
                            System.out.println("ERROR: La categoría debe ser positiva.");
                            break;
                        }

                        String expresion = "/catalogo/producto[categoriaId=" + catId + "]";
                        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

                        // Iterar e imprimir
                        for (int i = 0; i < nodos.getLength(); i++) {
                            imprimirLineaProductoXml((Element) nodos.item(i));
                        }
                        System.out.println("Productos encontrados: " + nodos.getLength());
                    }
                    case 3 -> {
                        // Filtro por precio superior a un valor numérico.
                        double precioMin = LectorConsola.leerDouble("Precio mínimo: ");

                        if (!Double.isFinite(precioMin) || precioMin < 0) {
                            System.out.println("ERROR: Introduce un precio válido.");
                            break;
                        }

                        // Iterar e imprimir
                        String expresion = "/catalogo/producto[precio > " + precioMin + "]";
                        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

                        for (int i = 0; i < nodos.getLength(); i++) {
                            imprimirLineaProductoXml((Element) nodos.item(i));
                        }
                        System.out.println("Productos encontrados: " + nodos.getLength());
                    }
                    case 4 -> {
                        // Cuento los nodos producto mediante la función count().
                        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
                        System.out.println("Productos encontrados: " + total.intValue());
                    }
                    case 0 -> System.out.println("Saliendo de la consulta XPath...");
                }

            } while (opcion != 0);

        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.out.println("ERROR al leer el XML: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR en la consulta XPath: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para mostrar los datos de un nodo producto.
     */
    private static void imprimirLineaProductoXml(Element elem) {
        String cod = leerCampo(elem, "codigo");
        String nom = leerCampo(elem, "nombre");
        String precio = leerCampo(elem, "precio");

        System.out.println(cod + "   " + nom + "   " + precio + "   euros");
    }

    // Leo el contenido de un campo del producto.
    private static String leerCampo(Element elem, String campo) {
        NodeList nodos = elem.getElementsByTagName(campo);

        if (nodos.getLength() == 0) {
            return "";
        }

        return nodos.item(0).getTextContent().trim();
    }
}