package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import es.iesisidra.datastock.componentes.LectorProductosJson;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class InventarioJSON {
    private InventarioJSON() {
    }

    public static void importar(List<Producto> listaProductos) {
        String ruta = Util.leerTextoNoVacio("Ruta: ");
        System.out.println();

        LectorProductosJson lector = new LectorProductosJson();
        List<Producto> leidos;

        try {
            leidos = lector.leer(Path.of(ruta), listaProductos);

        } catch (IOException | InvalidPathException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        int encontrados = lector.getEncontrados();
        List<String> incidencias = lector.getIncidencias();

        if (encontrados <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se han encontrado productos en el fichero.json.", ColoresConsola.ROJO));
            return;
        }

        // El componente solo lee: aquí se añaden los productos válidos al inventario
        listaProductos.addAll(leidos);

        System.out.println("===== IMPORTAR JSON =====");
        System.out.println();
        System.out.println("IMPORTACIÓN DE PRODUCTOS");
        System.out.println("--------------------------------------");
        System.out.println("Productos encontrados: " +  encontrados);
        System.out.println("Productos importados: " + leidos.size());
        System.out.println("Productos rechazados: " + (encontrados - leidos.size()));
        System.out.println("Incidencias:");

        if (incidencias.isEmpty()) {
            System.out.println("No hay incidencias.");
            
        } else {
            for (int i = 0; i < incidencias.size(); i++) {
                System.out.println("- " + incidencias.get(i));
            }
        }

        System.out.println(ColoresConsola.pintar("[OK] Importación finalizada.", ColoresConsola.VERDE));
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