package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.Util;

public class InventarioXML {
    private InventarioXML() {
    }

    private static String leerCampoXml(Element producto, String nombreCampo) {
        NodeList campos = producto.getElementsByTagName(nombreCampo);

        if (campos.getLength() == 0) {
            return null;
        }

        return campos.item(0).getTextContent().trim();
    }

    private static boolean comprobarRaizXml(Document documento) {
        Element raiz = documento.getDocumentElement();

        if (!raiz.getTagName().equals("catalogo")) {
            System.out.println("[ERROR] La raíz debe ser <catalogo>.");
            return false;
        }

        return true;
    }

    private static Producto crearProductoXml(Element elemento) {

        String codigo = leerCampoXml(elemento, "codigo");
        String nombre = leerCampoXml(elemento, "nombre");

        String precioTexto = leerCampoXml(elemento, "precio");
        String stockTexto = leerCampoXml(elemento, "stock");
        String categoriaTexto = leerCampoXml(elemento, "categoriaId");
        String proveedorTexto = leerCampoXml(elemento, "proveedorId");

        // Comprobar campos obligatorios
        if (precioTexto == null || precioTexto.isBlank()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }

        if (stockTexto == null || stockTexto.isBlank()) {
            throw new IllegalArgumentException("El stock es obligatorio.");
        }

        if (categoriaTexto == null || categoriaTexto.isBlank()) {
            throw new IllegalArgumentException("El ID de categoría es obligatorio.");
        }

        if (proveedorTexto == null || proveedorTexto.isBlank()) {
            throw new IllegalArgumentException("El ID de proveedor es obligatorio.");
        }

        // Convertir valores
        BigDecimal precio = new BigDecimal(precioTexto);
        int stock = Integer.parseInt(stockTexto);
        int categoriaId = Integer.parseInt(categoriaTexto);
        int proveedorId = Integer.parseInt(proveedorTexto);

        return new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
    }

    private static void addIncidencias(List<String> incidencias, List<String> errores, int numeroProducto) {

        for (int i = 0; i < errores.size(); i++) {
            incidencias.add("Producto " + numeroProducto + ": "
                    + errores.get(i) + ".");
        }
    }

    private static boolean procesarProductoXml(Element elemento, List<String> incidencias, int numeroProducto, List<Producto> productos) {

        try {
            Producto producto = crearProductoXml(elemento);

            List<String> errores = GestorProductos.validarProducto(producto, productos);

            if (errores.isEmpty()) {
                productos.add(producto);
                return true;

            } else {
                addIncidencias(incidencias, errores, numeroProducto);
                return false;
            }

        } catch (IllegalArgumentException e) {

            incidencias.add("Producto " + numeroProducto + ": " + e.getMessage());
            return false;
        }
    }

    private static void mostrarResumenImportacion(int encontrados, int importados, int rechazados, List<String> incidencias) {

        System.out.println();
        System.out.println("IMPORTACIÓN DE PRODUCTOS");
        System.out.println("--------------------------------------");
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Productos importados: " + importados);
        System.out.println("Productos rechazados: " + rechazados);
        System.out.println("Incidencias:");

        if (incidencias.isEmpty()) {
            System.out.println("No hay incidencias.");

        } else {
            for (int i = 0; i < incidencias.size(); i++) {
                System.out.println("- " + incidencias.get(i));
            }
        }

        System.out.println("Importación finalizada.");
    }

    public static void importarXml(List<Producto> productos) {
        File fichero = Util.pedirArchivo();

        if (fichero == null) {
            return;
        }

        try {
            ProductoXmlRepository repositorio = new ProductoXmlRepository();
            Document documento = repositorio.leer(Path.of(fichero.getPath()));

            if (!comprobarRaizXml(documento)) {
                return;
            }

            Element raiz = documento.getDocumentElement();
            NodeList productosXml = raiz.getChildNodes();

            int encontrados = 0;
            int importados = 0;
            int rechazados = 0;
            int numeroProducto = 0;

            for (int i = 0; i < productosXml.getLength(); i++) {
                if (productosXml.item(i) instanceof Element) {
                    Element elemento = (Element) productosXml.item(i);

                    if (elemento.getTagName().equals("producto")) {
                        encontrados++;
                    }
                }
            }

            List<String> incidencias = new ArrayList<>();

            for (int i = 0; i < productosXml.getLength(); i++) {

                if (!(productosXml.item(i) instanceof Element)) {
                    rechazados++;
                    continue;
                }

                Element elemento = (Element) productosXml.item(i);

                if (!elemento.getTagName().equals("producto")) {
                    rechazados++;
                    continue;
                }

                numeroProducto++;

                if (procesarProductoXml(elemento, incidencias, numeroProducto, productos)) {
                    importados++;
                } else {
                    rechazados++;
                }
            }

            mostrarResumenImportacion(encontrados, importados, rechazados, incidencias);

        } catch (ParserConfigurationException e) {
            System.out.println("[ERROR] No se pudo configurar el analizador XML.");

        } catch (SAXException e) {
            System.out.println("[ERROR] El XML está mal formado o contiene DTD no permitidas.");

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo leer el fichero.");
        }
    }

    private static Document crearDocumentoXml(List<Producto> productos) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document documento = factory.newDocumentBuilder().newDocument();

        Element raiz = documento.createElement("catalogo");
        documento.appendChild(raiz);

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);

            Element productoXml = crearProductoXml(documento, producto);
            raiz.appendChild(productoXml);
        }

        return documento;
    }

    private static Element crearProductoXml(Document documento, Producto producto) {

        Element productoXml = documento.createElement("producto");

        Element codigo = documento.createElement("codigo");
        codigo.setTextContent(producto.getCodigo());
        productoXml.appendChild(codigo);

        Element nombre = documento.createElement("nombre");
        nombre.setTextContent(producto.getNombre());
        productoXml.appendChild(nombre);

        Element precio = documento.createElement("precio");
        precio.setTextContent(producto.getPrecio().toString());
        productoXml.appendChild(precio);

        Element stock = documento.createElement("stock");
        stock.setTextContent(String.valueOf(producto.getStock()));
        productoXml.appendChild(stock);

        Element categoriaId = documento.createElement("categoriaId");
        categoriaId.setTextContent(String.valueOf(producto.getCategoriaId()));
        productoXml.appendChild(categoriaId);

        Element proveedorId = documento.createElement("proveedorId");
        proveedorId.setTextContent(String.valueOf(producto.getProveedorId()));
        productoXml.appendChild(proveedorId);

        return productoXml;
    }

    public static void exportarXml(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println("[ERROR] No hay productos para exportar.");
            return;
        }

        File fichero = Util.pedirArchivo();

        if (fichero == null) {
            return;
        }

        if (!Util.validarDestino(fichero)) {
            return;
        }

        if (!Util.confirmarSobrescritura(fichero)) {
            System.out.println("Guardado cancelado.");
            return;
        }

        try {
            Document documento = crearDocumentoXml(productos);

            ProductoXmlRepository repositorio = new ProductoXmlRepository();
            repositorio.guardar(documento, fichero.toPath());

            System.out.println();
            System.out.println("EXPORTACIÓN XML FINALIZADA");
            System.out.println("Ruta: " + fichero.getAbsolutePath());
            System.out.println("Formato: XML");
            System.out.println("Productos exportados: " + productos.size());

        } catch (ParserConfigurationException e) {
            System.out.println("[ERROR] No se pudo crear el documento XML.");

        } catch (IOException | TransformerException e) {
            System.out.println("[ERROR] No se pudo guardar el fichero XML.");
        }
    }
}