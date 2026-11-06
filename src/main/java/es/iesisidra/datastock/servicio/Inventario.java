package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

/**
 * Estado común del inventario durante la ejecución de UT1.
 * <p>Esta clase solo proporciona la colección compartida. No implementa
 * importación, validación, búsqueda, exportación ni persistencia: esos son
 * ejercicios que deberá desarrollar el alumnado.</p>
 */
public class Inventario {
    private final List<Producto> PRODUCTOS = new ArrayList<>();

    /**
     * Obtiene la colección que comparten las opciones del menú.
     * <p>ATENCIÓN: la lista es modificable. El alumnado deberá aplicar las
     * reglas del enunciado antes de añadir o sustituir elementos.</p>
     */
    public List<Producto> getProductos() { return PRODUCTOS; }

    private List<String> validarProducto(Producto p, List<Producto> lista) {
        List<String> errores = new ArrayList<>();

        // Validar codigo
        if (p.getCodigo() == null || p.getCodigo().trim().isBlank()) {
            errores.add("El código no puede estar vacío");

        } else if (p.getCodigo().trim().length() > 12) {
            errores.add("El código no debe superar los 12 caracteres");

        } else if (!p.getCodigo().trim().matches("[A-Z0-9-]+")) {
            errores.add("El código no respeta el patrón");
        }

        boolean codigoDuplicado = false;

        for (int i = 0; i < lista.size() && !codigoDuplicado; i++) {
           if (lista.get(i).getCodigo() != null && lista.get(i).getCodigo().trim().equalsIgnoreCase(p.getCodigo().trim())) {
                codigoDuplicado = true;
            }
        }

        if (codigoDuplicado) {
            errores.add("El código ya existe en la lista.");
        }

        // Validar nombre
        if (p.getNombre() == null || p.getNombre().trim().isBlank()) {
            errores.add("El nombre no puede estar vacío");

        } else if (p.getNombre().trim().length() < 3 || p.getNombre().trim().length() > 100) {
            errores.add("El nombre debe tener entre 3 y 100 caracteres");
        }

        // Validar precio
        if (p.getPrecio() == null) {
            errores.add("El precio no puede estar vacío");

        } else if (p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El precio debe ser mayor que cero");
        }

        // Validar stock
        if (p.getStock() < 0) {
        errores.add("El stock no puede ser negativo");
        }

        // Validar categoría
        if (p.getCategoriaId() <= 0) {
            errores.add("El ID de categoría debe ser positivo");
        }

        // Validar proveedor
        if (p.getProveedorId() <= 0) {
            errores.add("El ID de proveedor debe ser positivo");
        }

        return errores;
    }

    public void explorarRuta() {
        InventarioArchivos.explorar();
    }

    public void importarJson() {
        InventarioJSON.importar(PRODUCTOS);
    }

    public void listarProductos() {
        Consultas.listarProductos(PRODUCTOS);
    }

    public void buscarProductoPorCodigo() {
        Consultas.buscarProductoPorCodigo(PRODUCTOS);
    }

    public void buscarProductoPorNombre() {
        Consultas.buscarProductoPorNombre(PRODUCTOS);
    }

    // FUncion para leer los 6 campos de XML
    private String leerCampoXml(Element producto, String nombreCampo) {
        NodeList campos = producto.getElementsByTagName(nombreCampo);

        if (campos.getLength() == 0) {
            return null;
        }

        return campos.item(0).getTextContent().trim();
    }

    public void importarXml() {
        String entrada = Util.leerTexto("Ruta: ");
        System.out.println();

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File fichero = new File(entrada);

        if (!fichero.exists() || !fichero.isFile()) {
            System.out.println("[ERROR] El fichero no existe/no es un fichero.");
            return;
        }

        try {
            // Leer el XML utilizando tu repositorio
            ProductoXmlRepository repositorio = new ProductoXmlRepository();
            Document documento = repositorio.leer(Path.of(entrada));

            Element raiz = documento.getDocumentElement();

            // Comprobar la raíz
            if (!raiz.getTagName().equals("catalogo")) {
                System.out.println("[ERROR] La raíz debe ser <catalogo>.");
                return;
            }

            NodeList productosXml = raiz.getChildNodes();

            int encontrados = 0;
            int importados = 0;
            int rechazados = 0;

            List<String> incidencias = new ArrayList<>();

            // Contar únicamente los elementos producto directos
            // Comprueba si es un elemento XML y si la etiqueta igual a producto.
            for (int i = 0; i < productosXml.getLength(); i++) {
                if (productosXml.item(i) instanceof Element && ((Element) productosXml.item(i)).getTagName().equals("producto")) {
                    encontrados++;
                }
            }

            // Contador que va aumentando cada vez que iteramos en un producto
            int numeroProducto = 0;

            // Si el no es un elemento, hace un continue para volver a iterar otra vez
            // Si no, coge los datos usando la funcion leerCampoXml
            for (int i = 0; i < productosXml.getLength(); i++) {
                if (!(productosXml.item(i) instanceof Element)) {
                    continue;
                }

                Element elemento = (Element) productosXml.item(i);

                // SI el tag no es producto pasa a la siguiente iteracion
                if (!elemento.getTagName().equals("producto")) {
                    continue;
                }

                numeroProducto++;

                try {
                    String codigo = leerCampoXml(elemento, "codigo");
                    String nombre = leerCampoXml(elemento, "nombre");
                    String precioTexto = leerCampoXml(elemento, "precio");
                    String stockTexto = leerCampoXml(elemento, "stock");
                    String categoriaTexto = leerCampoXml(elemento, "categoriaId");
                    String proveedorTexto = leerCampoXml(elemento, "proveedorId");

                    // Comprobar campos ausentes o vacíos antes de convertir
                    // Hago esta comprobacion porque si pongo valores por default estaría funcionando el programa.
                    // Con esto hago que el proceso "pete" si no hay un valor valido.
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

                    BigDecimal precio = new BigDecimal(precioTexto);
                    int stock = Integer.parseInt(stockTexto);
                    int categoriaId = Integer.parseInt(categoriaTexto);
                    int proveedorId = Integer.parseInt(proveedorTexto);

                    Producto producto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);

                    // Usar las mismas validaciones que en JSON
                    List<String> errores = validarProducto(producto, PRODUCTOS);

                    // Añadir producto y poner errores.
                    if (errores.isEmpty()) {
                        PRODUCTOS.add(producto);
                        importados++;

                    } else {
                        rechazados++;
                        
                        // Agregar las incidencias a un ArrayList
                        for (int j = 0; j < errores.size(); j++) {
                            incidencias.add("Producto " + numeroProducto + ": " + errores.get(j) + ".");
                        }
                    }
                    
                    // Catch para cundo introduzca un valor no valido en el xml
                } catch (IllegalArgumentException e) {
                    rechazados++;
                    incidencias.add("Producto " + numeroProducto + ": " + e.getMessage());
                }
            }

            // Mostrar el resumen
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

        } catch (ParserConfigurationException e) {
            System.out.println("[ERROR] No se pudo configurar el analizador XML.");

        } catch (SAXException e) {
            System.out.println("[ERROR] El XML está mal formado o contiene DTD no permitidas.");

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo leer el fichero.");
        }
    }

    public void exportarJson() {
        InventarioJSON.exportarJson(PRODUCTOS);
    }

    public void exportarXml() {
        // Comprobar si el inventario está vacío
        if (PRODUCTOS.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
            return;
        }

        // Solicitar la ruta del fichero
        String entrada = Util.leerTexto("Ruta del fichero XML: ");

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File fichero = new File(entrada);
        File directorio = fichero.getAbsoluteFile().getParentFile();

        // Comprobar si existe el directorio
        if (directorio == null || !directorio.exists() || !directorio.isDirectory()) {
            System.out.println("[ERROR] El directorio de destino no existe.");
            return;
        }

        // Comprobar si la ruta es un directorio
        if (fichero.isDirectory()) {
            System.out.println("[ERROR] La ruta indicada corresponde a un directorio, no a un archivo.");
            return;
        }

        // Comprobar si el fichero ya existe
        if (fichero.exists()) {
            if (!Util.confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
                System.out.println("Exportación cancelada.");
                return;
            }
        }

        try {
            // Crear el documento XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document documento = factory.newDocumentBuilder().newDocument();

            // Crear el elemento raíz
            Element raiz = documento.createElement("catalogo");
            documento.appendChild(raiz);

            // Crear y Añadir los productos al XML
            for (int i = 0; i < PRODUCTOS.size(); i++) {
                Producto p = PRODUCTOS.get(i);

                Element productoXml = documento.createElement("producto");
                raiz.appendChild(productoXml);

                Element codigo = documento.createElement("codigo");
                codigo.setTextContent(p.getCodigo());
                productoXml.appendChild(codigo);

                Element nombre = documento.createElement("nombre");
                nombre.setTextContent(p.getNombre());
                productoXml.appendChild(nombre);

                Element precio = documento.createElement("precio");
                precio.setTextContent(p.getPrecio().toString());
                productoXml.appendChild(precio);

                Element stock = documento.createElement("stock");
                stock.setTextContent(String.valueOf(p.getStock()));
                productoXml.appendChild(stock);

                Element categoriaId = documento.createElement("categoriaId");
                categoriaId.setTextContent(String.valueOf(p.getCategoriaId()));
                productoXml.appendChild(categoriaId);

                Element proveedorId = documento.createElement("proveedorId");
                proveedorId.setTextContent(String.valueOf(p.getProveedorId()));
                productoXml.appendChild(proveedorId);
            }

            // Guardar el documento XML
            ProductoXmlRepository repositorio = new ProductoXmlRepository();
            repositorio.guardar(documento, fichero.toPath());

            System.out.println();
            System.out.println("EXPORTACIÓN XML FINALIZADA");
            System.out.println("Ruta: " + fichero.getAbsolutePath());
            System.out.println("Formato: XML");
            System.out.println("Productos exportados: " + PRODUCTOS.size());

        // Manejo de excepciones
        } catch (ParserConfigurationException e) {
            System.out.println("[ERROR] No se pudo crear el documento XML.");
        } catch (IOException | TransformerException e) {
            System.out.println("[ERROR] No se pudo guardar el fichero XML.");
        }
    }

    public void guardarInventario() {
        // Comprobar si el inventario está vacío
        if (PRODUCTOS.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
            return;
        }

        // Solicitar la ruta del fichero
        String entrada = Util.leerTexto("Ruta del fichero: ");

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File fichero = new File(entrada);
        File directorio = fichero.getAbsoluteFile().getParentFile();

        // Comprobar si existe el directorio
        if (directorio == null || !directorio.exists() || !directorio.isDirectory()) {
            System.out.println("[ERROR] El directorio de destino no existe.");
            return;
        }

        // Comprobar si la ruta es un directorio
        if (fichero.isDirectory()) {
            System.out.println("[ERROR] La ruta corresponde a un directorio.");
            return;
        }

        // Confirmar sobrescritura
        if (fichero.exists()) {
            if (!Util.confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
                System.out.println("Guardado cancelado.");
                return;
            }
        }

        // Guardar la colección completa
        // Try-With Ressources creando el objetctoutputstream
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(fichero))) {
            salida.writeObject(new ArrayList<>(PRODUCTOS));

            System.out.println();
            System.out.println("INVENTARIO GUARDADO CORRECTAMENTE");
            System.out.println("Ruta: " + fichero.getAbsolutePath());
            System.out.println("Productos guardados: " + PRODUCTOS.size());

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo guardar el inventario.");
        }
    }

    public void recuperarInventario() {
        // Solicitar la ruta del fichero
        String entrada = Util.leerTexto("Ruta del fichero: ");

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File fichero = new File(entrada);

        // Comprobar que el fichero existe
        if (!fichero.exists() || !fichero.isFile()) {
            System.out.println("[ERROR] El fichero no existe.");
            return;
        }

        // Confirmar la sustitución del inventario actual
        if (!PRODUCTOS.isEmpty()) {
            if (!Util.confirmar("El inventario contiene productos. ¿Quieres sustituirlo?")) {
                System.out.println("Recuperación cancelada.");
                return;
            }
        }

        try (ObjectInputStream entradaObjeto = new ObjectInputStream(new FileInputStream(fichero))) {

            Object objeto = entradaObjeto.readObject();

            // Comprobar que el objeto sea una lista
            if (!(objeto instanceof ArrayList<?>)) {
                System.out.println("[ERROR] El fichero no contiene una lista válida.");
                return;
            }

            // Tiene ? porque lanza una advertencia de casting
            ArrayList<?> listaLeida = (ArrayList<?>) objeto;
            List<Producto> productosRecuperados = new ArrayList<>();
            boolean valido = true;

            // Comprobar los productos recuperados
            for (int i = 0; i < listaLeida.size() && valido; i++) {
                Object elemento = listaLeida.get(i);

                // Si es un objeto Producto, es valido
                if (!(elemento instanceof Producto)) {
                    valido = false;

                } else {
                    Producto p = (Producto) elemento;
                    // Comprobar que no haya ninguno duplicado y que respeten las reglas
                    List<String> errores = validarProducto(p, productosRecuperados);

                    // Valida que el producto este bien
                    if (errores.isEmpty()) {
                        productosRecuperados.add(p);

                    } else {
                        valido = false;
                    }
                }
            }

            // Sustituir el inventario solo si todo es correcto
            if (valido) {
                PRODUCTOS.clear();
                PRODUCTOS.addAll(productosRecuperados);

                System.out.println("Inventario recuperado correctamente.");
                System.out.println("Productos recuperados: " + PRODUCTOS.size());

            } else {
                System.out.println("[ERROR] El fichero contiene productos inválidos.");
            }

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo leer el fichero.");
        } catch (ClassNotFoundException e) {
            System.out.println("[ERROR] No se ha encontrado una clase necesaria.");
        }
    }
}