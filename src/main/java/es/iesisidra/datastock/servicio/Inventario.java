package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.modelo.Producto;
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

    private List<String> validarProducto(Producto p) {
        List<String> errores = new ArrayList<String>();

        // Validar codigo
        if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
            errores.add("El código no puede estar vacío");

        } else if (p.getCodigo().trim().length() > 12) {
            errores.add("El código no debe superar los 12 caracteres");

        } else if (!p.getCodigo().trim().matches("[A-Z0-9-]+")) {
            errores.add("El código no respeta el patrón");
        }

        boolean codigoDuplicado = false;

        for (int i = 0; i < PRODUCTOS.size() && !codigoDuplicado; i++) {
           if (PRODUCTOS.get(i).getCodigo() != null && PRODUCTOS.get(i).getCodigo().trim().equalsIgnoreCase(p.getCodigo().trim())) {
            codigoDuplicado = true;
}
        }

        if (codigoDuplicado) {
            errores.add("El código ya existe en la lista.");
        }

        // Validar nombre
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
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

        if (entrada == null || entrada.isEmpty()) {
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

                    List<String> errores = validarProducto(producto);

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
                    e.printStackTrace();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}