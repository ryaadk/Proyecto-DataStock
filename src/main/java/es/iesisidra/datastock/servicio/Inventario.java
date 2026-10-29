package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.LectorConsola;

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
  
    public void explorarRuta() {
        // Pido ruta y hago validaciones (Si existe, si esta vacía y si es un directorio)
        String entrada = LectorConsola.leerTexto("Ruta: ");

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File ruta = new File(entrada);

        if (!ruta.exists()) {
            System.out.println("[ERROR] La ruta introducida no existe.");
            return;
        }

        if (!ruta.isDirectory()) {
            System.out.println("[ERROR] La ruta no es un directorio.");
            return;
        }

        // 2 contadores, Ficheros - Directorios
        // Paso el contador a la otra funcion pora modificarlas
        // El contador cuenta también los subdirectorios
        int[] contadores = {0, 0}; 
        

        explorarDirectorio(ruta, contadores);

        // Una vez termina la función imprimo por pantalla el total
        System.out.println();
        System.out.println("Total ficheros: " + contadores[0] + " | Total directorios: " + contadores[1]);
    }

    private void explorarDirectorio(File ruta, int[] contadores) {
        // Hago un bucle, si hay elementos, se ejecutará el bucle, si no hay elementos no ejecutará nada
        File[] elementos = ruta.listFiles();

        if (elementos == null) {
            System.out.println("[ERROR] No se puede acceder a: " + ruta.getAbsolutePath());
            return;
        }

        for (File elemento : elementos) {
            // Comprobar si es archivo o directorio e imprimir datos correspondientes
            if (elemento.isFile()) {
                System.out.println("[FICHERO] " + elemento.getName());
                System.out.println("[RUTA ABSOLUTA] " + elemento.getAbsolutePath());
                System.out.println("[BYTES] " + elemento.length());

                contadores[0]++;

            } else if (elemento.isDirectory()) {
                System.out.println("[DIRECTORIO] " + elemento.getName());

                contadores[1]++;
                // Vuelve a ejecutar esta función para hacer otra vez el bucle y revisar el contenido de este directorio.
                explorarDirectorio(elemento, contadores);
            }
        }
    }

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

    public void importarJson() {
        String entrada = LectorConsola.leerTexto("Ruta: ");
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

        try (FileReader reader = new FileReader(fichero)) {
            JsonElement raiz = JsonParser.parseReader(reader);

            // Comprobar si la raiz es un objeto json
            if (!raiz.isJsonObject()) {
                System.out.println("[ERROR] La raíz del JSON debe ser un objeto.");
                return;
            }

            // Obtener la raiz y comprobar si tiene un array de productos
            JsonObject objetoRaiz = raiz.getAsJsonObject();

            if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
                System.out.println("[ERROR] No existe un array válido llamado productos.");
                return;
            }

            // Devuelve el array JSON que contiene los productos
            JsonArray productos = objetoRaiz.getAsJsonArray("productos");

            int encontrados = productos.size(); // Productos encontrados
            int importados = 0;
            int rechazados = 0;

            List<String> incidencias = new ArrayList<>();

            System.out.println("===== IMPORTAR JSON =====");
            System.out.println("Productos encontrados: " + encontrados);

            for (int i = 0; i < encontrados; i++) {
                // Deserializado en una instancia de la clase Producto
                try {
                    Producto producto = new Gson().fromJson(productos.get(i), Producto.class);

                    List<String> errores = validarProducto(producto, PRODUCTOS);

                    if (errores.isEmpty()) {
                        PRODUCTOS.add(producto);
                        importados++;

                    } else {
                        rechazados++;

                        // Bucle para poner las incidencias
                        for (int j = 0; j < errores.size(); j++) {
                            incidencias.add("Producto " + (i + 1) + ": "+ errores.get(j) + ".");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("[Error] Error al parsear los productos.");
                }   
            }

            System.out.println();
            System.out.println("IMPORTACIÓN DE PRODUCTOS");
            System.out.println("--------------------------------------");
            System.out.println("Productos encontrados:");
            System.out.println(encontrados);

            System.out.println("Productos importados:");
            System.out.println(importados);

            System.out.println("Productos rechazados:");
            System.out.println(rechazados);

            System.out.println("Incidencias:");

            if (incidencias.isEmpty()) {
                System.out.println("No hay incidencias.");
            } else {
                for (int i = 0; i < incidencias.size(); i++) {
                    System.out.println("- " + incidencias.get(i));
                }
            }

            System.out.println("Importación finalizada.");

        } catch (JsonSyntaxException e) {
            System.out.println("[ERROR] Error a la hora de leer el archivo, JSON mal formado");
        } catch (IOException e) {
            System.out.println("[ERROR] Error a la hora de leer el archivo con el reader.");
        }
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
        String entrada = LectorConsola.leerTexto("Ruta: ");
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

    public void listarProductos() {
        if (PRODUCTOS.isEmpty()) {
            System.out.println("No hay productos en el inventario.");
            return;
        }

        // Crear una copia para no modificar la lista original
        List<Producto> productosOrdenados = new ArrayList<>(PRODUCTOS);

        // Ordenar por código usando el metodo de ordenacion por burbuja
        // El primer bucle hace la pasada por productos 
        // El segundo bucle comprueba los productos que estan al lado
        for (int i = 0; i < productosOrdenados.size() - 1; i++) {
            for (int j = 0; j < productosOrdenados.size() - 1 - i; j++) {

                String codigo1 = productosOrdenados.get(j).getCodigo();
                String codigo2 = productosOrdenados.get(j + 1).getCodigo();

                // Comparar codigos, si es distinto de 0 el resultado, el codigo es mayor o menor.
                // Si es positivo significa que el codigo 1 es mayor que el codigo 2
                if (codigo1.compareToIgnoreCase(codigo2) > 0) {
                    Producto auxiliar = productosOrdenados.get(j);

                    productosOrdenados.set(j, productosOrdenados.get(j + 1));
                    productosOrdenados.set(j + 1, auxiliar);
                }
            }
        }

        // Mostrar los productos ordenados
        System.out.println();
        System.out.println("LISTADO DE PRODUCTOS");
        System.out.println("----------------------------------------");

        for (int i = 0; i < productosOrdenados.size(); i++) {
            Producto p = productosOrdenados.get(i);

            System.out.println("Código: " + p.getCodigo());
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Precio: " + p.getPrecio() + " €");
            System.out.println("Stock: " + p.getStock());
            System.out.println("Categoría: " + p.getCategoriaId());
            System.out.println("Proveedor: " + p.getProveedorId());

            System.out.println("----------------------------------------");
        }
    }

    public void buscarProductoPorCodigo() {
        if (PRODUCTOS.isEmpty()) {
            System.out.println("No hay productos en el inventario.");
            return;
        }

        String codigoBuscado = LectorConsola.leerTexto("Código: ");

        if (codigoBuscado == null || codigoBuscado.isBlank()) {
            System.out.println("[ERROR] Debes introducir un código.");
            return;
        }

        // ELiminar espacios al principio y al final
        codigoBuscado = codigoBuscado.trim();

        boolean encontrado = false;

        for (int i = 0; i < PRODUCTOS.size() && !encontrado; i++) {

            Producto p = PRODUCTOS.get(i);

            // SI lo encuentra, imprime por pantalla los datos del producto y cambia el valor de la bandera
            // Asi evitamos que siga iterando y terminamos el bucle
            if (p.getCodigo() != null && p.getCodigo().trim().equalsIgnoreCase(codigoBuscado)) {

                System.out.println();
                System.out.println("PRODUCTO ENCONTRADO");
                System.out.println("------------------------------");
                System.out.println("Código: " + p.getCodigo());
                System.out.println("Nombre: " + p.getNombre());
                System.out.println("Precio: " + p.getPrecio() + " €");
                System.out.println("Stock: " + p.getStock());
                System.out.println("Categoría: " + p.getCategoriaId());
                System.out.println("Proveedor: " + p.getProveedorId());
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("[ERROR] No existe ningún producto con ese código.");
        }
    }

    public void buscarProductoPorNombre() {
        if (PRODUCTOS.isEmpty()) {
            System.out.println("No hay productos en el inventario.");
            return;
        }

        String nombreBuscado = LectorConsola.leerTexto("Nombre: ");

        if (nombreBuscado == null || nombreBuscado.isBlank()) {
            System.out.println("[ERROR] Debes introducir un nombre.");
            return;
        }

        // ELiminar espacios al principio y al final
        nombreBuscado = nombreBuscado.trim();

        boolean encontrado = false;

        for (int i = 0; i < PRODUCTOS.size(); i++) {

            Producto p = PRODUCTOS.get(i);

            // Revisamos si el nombe del producto contiene por ejemplo Teclado.
            // Si tenemos  Productos, por ejemplo Teclado Mecanico, Teclado de Membrana, Teclado, devolverá ambos
            // ya que ambos contienen la palabra teclado.
            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(nombreBuscado.toLowerCase())) {

                System.out.println();
                System.out.println("PRODUCTO ENCONTRADO");
                System.out.println("------------------------------");
                System.out.println("Código: " + p.getCodigo());
                System.out.println("Nombre: " + p.getNombre());
                System.out.println("Precio: " + p.getPrecio() + " €");
                System.out.println("Stock: " + p.getStock());
                System.out.println("Categoría: " + p.getCategoriaId());
                System.out.println("Proveedor: " + p.getProveedorId());
                System.out.println("------------------------------");

                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("[ERROR] No se encontraron productos con ese nombre.");
        }
    }

    public void exportarJson() {

        // Comprobar si el inventario está vacío
        if (PRODUCTOS.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
            return;
        }

        // Solicitar la ruta del fichero
        String entrada = LectorConsola.leerTexto("Ruta del fichero JSON: ");

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

        // Comprobar si el valor de fichero es un directorio.
        if (fichero.isDirectory()) {
            System.out.println("[ERROR] La ruta indicada corresponde a un directorio, no a un archivo.");
            return;
        }

        // Comprobar si el fichero ya existe
        if (fichero.exists()) {

            if (!LectorConsola.confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
                System.out.println("Exportación cancelada.");
                return;
            }
        }

        try {
            // Crear el objeto raíz del JSON
            JsonObject raiz = new JsonObject();
            JsonArray productosJson = new JsonArray();

            // Añadir los productos al array
            for (int i = 0; i < PRODUCTOS.size(); i++) {

                Producto p = PRODUCTOS.get(i);

                JsonObject productoJson = new JsonObject();

                productoJson.addProperty("codigo", p.getCodigo());
                productoJson.addProperty("nombre", p.getNombre());
                productoJson.addProperty("precio", p.getPrecio());
                productoJson.addProperty("stock", p.getStock());
                productoJson.addProperty("categoriaId", p.getCategoriaId());
                productoJson.addProperty("proveedorId", p.getProveedorId());

                productosJson.add(productoJson);
            }

            // Añadir el array al objeto raíz
            raiz.add("productos", productosJson);

            // Configurar Gson para que el JSON tenga indentación
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Escribir el JSON en el fichero
            try (FileWriter escritor = new FileWriter(fichero)) {
                gson.toJson(raiz, escritor);
            }

            System.out.println();
            System.out.println("EXPORTACIÓN JSON FINALIZADA");
            System.out.println("Ruta: " + fichero.getAbsolutePath());
            System.out.println("Formato: JSON");
            System.out.println("Productos exportados: " + PRODUCTOS.size());

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo escribir el fichero JSON.");
        }
    }

    public void exportarXml() {
        // Comprobar si el inventario está vacío
        if (PRODUCTOS.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
            return;
        }

        // Solicitar la ruta del fichero
        String entrada = LectorConsola.leerTexto("Ruta del fichero XML: ");

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
            if (!LectorConsola.confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
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
        String entrada = LectorConsola.leerTexto("Ruta del fichero: ");

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
            if (!LectorConsola.confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
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
        String entrada = LectorConsola.leerTexto("Ruta del fichero: ");

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
            if (!LectorConsola.confirmar("El inventario contiene productos. ¿Quieres sustituirlo?")) {
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