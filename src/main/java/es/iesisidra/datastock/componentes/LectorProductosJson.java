package es.iesisidra.datastock.componentes;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.GestorProductos;

public class LectorProductosJson {
    private final List<String> incidencias = new ArrayList<>();
    private int encontrados;

    public List<Producto> leer(Path ruta, List<Producto> productosExistentes) throws IOException {
        incidencias.clear();
        encontrados = 0;

        if (ruta == null || !Files.isRegularFile(ruta)) {
            throw new IOException("El fichero no existe o no es un fichero: " + ruta);
        }

        JsonArray productosJson = obtenerArrayProductos(ruta);
        encontrados = productosJson.size();

        List<Producto> validos = new ArrayList<>();

        // Lista auxiliar para comprobar duplicados sin tocar el inventario
        List<Producto> procesados = new ArrayList<>(productosExistentes);

        for (int i = 0; i < encontrados; i++) {
            Producto producto = convertirProducto(productosJson.get(i), procesados, i + 1);

            if (producto != null) {
                validos.add(producto);
                procesados.add(producto);
            }
        }

        return validos;
    }

    
    public List<String> getIncidencias() {
        return incidencias;
    }

    
    public int getEncontrados() {
        return encontrados;
    }

    private JsonArray obtenerArrayProductos(Path ruta) throws IOException {
        JsonElement raiz;

        try (Reader reader = Files.newBufferedReader(ruta)) {
            raiz = JsonParser.parseReader(reader);

        } catch (JsonParseException e) {
            throw new IOException("JSON mal formado.");
        }

        // Comprobar si la raiz es un objeto json
        if (!raiz.isJsonObject()) {
            throw new IOException("La raíz del JSON debe ser un objeto.");
        }

        // Obtener la raiz y comprobar si tiene un array de productos
        JsonObject objetoRaiz = raiz.getAsJsonObject();

        if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
            throw new IOException("No existe un array válido llamado productos.");
        }

        return objetoRaiz.getAsJsonArray("productos");
    }

    private Producto convertirProducto(JsonElement productoJson, List<Producto> procesados, int posicion) {
        Producto resultado = null;

        if (!productoJson.isJsonObject()) {
            incidencias.add("Producto " + posicion + ": el elemento no es un objeto JSON.");

        } else {
            try {
                // Deserializar el producto
                Producto producto = new Gson().fromJson(productoJson, Producto.class);

                // Validar producto
                List<String> errores = GestorProductos.validarProducto(producto, procesados);

                if (errores.isEmpty()) {
                    resultado = producto;

                } else {
                    // Guardar las incidencias
                    for (int j = 0; j < errores.size(); j++) {
                        incidencias.add("Producto " + posicion + ": " + errores.get(j) + ".");
                    }
                }

            } catch (JsonParseException e) {
                incidencias.add("Producto " + posicion + ": error JSON mal formado.");
            }
        }

        return resultado;
    }
}