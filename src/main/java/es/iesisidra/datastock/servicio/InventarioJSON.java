package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class InventarioJSON {
    private InventarioJSON() {
    }

    public static void importar(List<Producto> listaProductos) {
        File fichero = Util.pedirArchivo();

        if (fichero == null) {
            return;
        }

        JsonArray productos = obtenerRaizJson(fichero);

        int encontrados = productos.size();
        int importados = 0;
        int rechazados = 0;

        List<String> incidencias = new ArrayList<>();

        if (encontrados <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se han encontrado productos en el fichero.json.", ColoresConsola.ROJO));
            return;
        }

        System.out.println("===== IMPORTAR JSON =====");
        System.out.println("Productos encontrados: " + encontrados);

        for (int i = 0; i < encontrados; i++) {

            boolean importado = procesarProducto(productos.get(i), listaProductos, incidencias, i + 1);

            if (importado) {
                importados++;

            } else {
                rechazados++;
            }
        }

        System.out.println();
        System.out.println("IMPORTACIÓN DE PRODUCTOS");
        System.out.println("--------------------------------------");
        System.out.println("Productos encontrados: " +  encontrados);
        System.out.println("Productos importados: " + importados);
        System.out.println("Productos rechazados:" +  rechazados);
        System.out.println("Incidencias:");

        if (incidencias.isEmpty()) {
            System.out.println("No hay incidencias.");
            
        } else {
            for (int i = 0; i < incidencias.size(); i++) {
                System.out.println("- " + incidencias.get(i));
            }
        }

        System.out.println(ColoresConsola.pintar("Importación finalizada.", ColoresConsola.VERDE));
    }

    private static JsonArray obtenerRaizJson(File fichero) {
        JsonArray productos = null;

        try (FileReader reader = new FileReader(fichero)) {
            JsonElement raiz = JsonParser.parseReader(reader);

            // Comprobar si la raiz es un objeto json
            if (!raiz.isJsonObject()) {
                System.out.println(ColoresConsola.pintar("[ERROR] La raíz del JSON debe ser un objeto.", ColoresConsola.ROJO));
                return productos;
            }

            // Obtener la raiz y comprobar si tiene un array de productos
            JsonObject objetoRaiz = raiz.getAsJsonObject();

            if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe un array válido llamado productos.", ColoresConsola.ROJO));
                return productos;
            }

            // Devuelve el array JSON que contiene los productos
            productos = objetoRaiz.getAsJsonArray("productos");
            

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error con reader: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (JsonSyntaxException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error JSON mal formado", ColoresConsola.ROJO));
        }

        return productos;
    }

    private static boolean procesarProducto(JsonElement productoJson, List<Producto> listaProductos, List<String> incidencias, int posicion) {
        try {
            if (!productoJson.isJsonObject()) {
                incidencias.add("Producto " + posicion + ": el elemento no es un objeto JSON.");
                return false;
            }

            // Deserializar el producto
            Producto producto = new Gson().fromJson(productoJson, Producto.class);

            // Validar producto
            List<String> errores = GestorProductos.validarProducto(producto, listaProductos);

            if (errores.isEmpty()) {
                listaProductos.add(producto);
                return true;

            } else {
                // Guardar las incidencias
                for (int j = 0; j < errores.size(); j++) {
                    incidencias.add("Producto " + posicion + ": " + errores.get(j) + ".");
                }

                return false;
            }

        } catch (JsonSyntaxException e) {
            incidencias.add("Producto " + posicion + ": error JSON mal formado.");
            return false;
        }
    }

    public static void exportarJson(List<Producto> productos) {
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
        
        escribirJson(fichero, productos);     
    }

    private static JsonObject crearJson(List<Producto> productos) {
        JsonObject raiz = new JsonObject();
        JsonArray productosJson = new JsonArray();

        // Añadir los productos al array
        for (int i = 0; i < productos.size(); i++) {

            Producto p = productos.get(i);

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
        return raiz;
    }

    private static void escribirJson(File fichero, List<Producto> productos) {
        JsonObject raiz = crearJson(productos);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter escritor = new FileWriter(fichero)) {
            gson.toJson(raiz, escritor);

            System.out.println();
            System.out.println(ColoresConsola.pintar("Exportación JSON finalizada", ColoresConsola.VERDE));
            System.out.println(ColoresConsola.pintar("Ruta: " + fichero.getAbsolutePath(), ColoresConsola.VERDE));
            System.out.println(ColoresConsola.pintar("Formato: JSON", ColoresConsola.VERDE));
            System.out.println(ColoresConsola.pintar("Productos exportados: " + productos.size(), ColoresConsola.VERDE));
            
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se pudo escribir el fichero JSON.", ColoresConsola.ROJO));
        }
    }
}