package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public final class AccesoAleatorioActividad {

    private static final String RUTA = "datos/salidas/stock.bin";
    private static final int TAMANO_REGISTRO = 36;

    private AccesoAleatorioActividad() {
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();

        System.out.println("===== ACCESO ALEATORIO =====");
        System.out.println("Antes de empezar hay que importar los productos.");
        System.out.println("Importalos usando el catalogo_valido.json");

        // Importar el catálogo JSON indicando la ruta por consola
        inventario.importarJson();
        System.out.println();

        // Generar el fichero binario y trabajar con él
        ejecutar(inventario);
    }

    public static void ejecutar(Inventario inventario) {

        // Generar el fichero con los productos cargados en memoria
        if (generarStockBinario(inventario.getProductos())) {
            mostrarProductos();
            System.out.println();
            gestionarAccesoAleatorioStock();
        }
    }

    private static boolean generarStockBinario(List<Producto> productos) {

        if (productos.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
            return false;
        }

        // Comprobar que los códigos sean válidos antes de ordenar
        for (Producto p : productos) {
            if (p.getCodigo() == null || p.getCodigo().trim().isEmpty() || p.getCodigo().trim().length() > 12) {
                System.out.println("[ERROR] Hay un código vacío o con más de 12 caracteres.");
                return false;
            }
        }
        File fichero = new File(RUTA);

        // Preguntar antes de sobrescribir el fichero existente
        if (fichero.exists() && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?")) {
            System.out.println("Operación cancelada.");
            return false;
        }

        // Crear una copia para no modificar el orden del inventario original
        List<Producto> copia = new ArrayList<>(productos);

        // Ordenar los productos por código Orden Burbuja
        for (int i = 0; i < copia.size() - 1; i++) {
            for (int j = 0; j < copia.size() - 1 - i; j++) {
                if (copia.get(j).getCodigo().compareToIgnoreCase(copia.get(j + 1).getCodigo()) > 0) {
                    Producto auxiliar = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, auxiliar);
                }
            }
        }

        try {
            // Crear la carpeta de salida si no existe
            Path path = Path.of(RUTA);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // Abrir el fichero para escribir (ReadWrite)
            try (RandomAccessFile archivo = new RandomAccessFile(fichero, "rw")) {

                // Borrar el contenido anterior
                archivo.setLength(0);

                // Guardar los datos de cada producto
                for (Producto p : copia) {
                    String codigo = p.getCodigo().trim();

                    // Completar el código con espacios hasta 12 caracteres
                    while (codigo.length() < 12) {
                        codigo += " ";
                    }

                    // Escribir código, precio y stock
                    archivo.writeChars(codigo);
                    archivo.writeDouble(p.getPrecio().doubleValue());
                    archivo.writeInt(p.getStock());
                }

                System.out.println("Fichero generado: " + RUTA);
                System.out.println("Registros: " + copia.size() + " | Tamaño: " + archivo.length() + " bytes");
            }
            return true;

        } catch (IOException e) {
            System.out.println("[ERROR] Error al generar el fichero: " + e.getMessage());
            return false;
        }
    }

    private static void mostrarProductos() {
        // Abrir archivo para leer (Read)
        try (RandomAccessFile archivo = new RandomAccessFile(RUTA, "r")) {
            // Comprobar que el tamaño sea válido
            if (archivo.length() % TAMANO_REGISTRO != 0) {
                System.out.println("[ERROR] El tamaño del fichero no es múltiplo de 36.");
                return;
            }

            // Calcular el número de productos
            int cantidad = (int) (archivo.length() / TAMANO_REGISTRO);
            System.out.println();
            System.out.println("===== TODOS LOS PRODUCTOS =====");

            // Leer y mostrar todos los registros
            for (int i = 0; i < cantidad; i++) {
                StringBuilder codigo = new StringBuilder();

                for (int j = 0; j < 12; j++) {
                    codigo.append(archivo.readChar());
                }

                double precio = archivo.readDouble();
                int stock = archivo.readInt();

                System.out.println(codigo.toString().trim() + " | " + precio + " € | stock " + stock);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Error al leer el fichero: " + e.getMessage());
        }
    }

    private static void gestionarAccesoAleatorioStock() {

        try (RandomAccessFile archivo = new RandomAccessFile(RUTA, "rw")) {

            long tamano = archivo.length();

            // Comprobar que el fichero no esté vacío y tenga un tamaño válido
            if (tamano == 0 || tamano % TAMANO_REGISTRO != 0) {
                System.out.println("[ERROR] El fichero está vacío o su tamaño no es válido.");
                return;
            }

            // Calcular cuántos productos contiene
            int cantidad = (int) (tamano / TAMANO_REGISTRO);

            System.out.println();
            System.out.println("===== ACCESO ALEATORIO =====");
            System.out.println("Fichero: " + RUTA);
            System.out.println("Registros: " + cantidad + " | Tamaño: " + tamano + " bytes");

            // Pedir la posición del producto que se quiere consultar
            int posicion = LectorConsola.leerOpcion("Consultar posición (0-" + (cantidad - 1) + "): ", 0, cantidad - 1);

            // Ir al inicio del registro seleccionado
            archivo.seek((long) posicion * TAMANO_REGISTRO);
            StringBuilder codigo = new StringBuilder();

            // Leer el código del producto
            for (int i = 0; i < 12; i++) {
                codigo.append(archivo.readChar());
            }

            // Leer el precio y el stock actual
            double precio = archivo.readDouble();
            int stockAnterior = archivo.readInt();

            // Mostrar el producto seleccionado
            System.out.println(codigo.toString().trim() + " | " + precio + " € | stock " + stockAnterior);

            // Pedir el nuevo stock
            int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");

            if (nuevoStock < 0) {
                System.out.println("[ERROR] El stock no puede ser negativo.");
                return;
            }

            // Ir al campo stock y modificar únicamente ese valor
            archivo.seek((long) posicion * TAMANO_REGISTRO + 32);
            archivo.writeInt(nuevoStock);

            System.out.println("Actualización correcta: stock "+ stockAnterior + " → " + nuevoStock);

        } catch (IOException e) {
            System.out.println("[ERROR] Error al acceder al fichero: " + e.getMessage());
        }
    }
}