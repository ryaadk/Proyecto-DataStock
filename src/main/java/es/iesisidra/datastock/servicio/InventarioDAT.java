package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.Util;

public class InventarioDAT {
    private InventarioDAT() {
    }

    public static void guardarInventario(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println("[ERROR] El inventario está vacío.");
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

        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(fichero))) {
            salida.writeObject(new ArrayList<>(productos));

            System.out.println();
            System.out.println("INVENTARIO GUARDADO CORRECTAMENTE");
            System.out.println("Ruta: " + fichero.getAbsolutePath());
            System.out.println("Productos guardados: " + productos.size());

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo guardar el inventario.");
        }
    }

    public static void recuperarInventario(List<Producto> productos) {
        File fichero = Util.pedirArchivo();

        if (fichero == null) {
            return;
        }

        // Confirmar la sustitución del inventario actual
        if (!productos.isEmpty()) {
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
                    List<String> errores = GestorProductos.validarProducto(p, productosRecuperados);

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
                productos.clear();
                productos.addAll(productosRecuperados);

                System.out.println("Inventario recuperado correctamente.");
                System.out.println("Productos recuperados: " + productos.size());

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