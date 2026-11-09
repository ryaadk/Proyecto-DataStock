package es.iesisidra.datastock.servicio;

import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class Consultas {
    private Consultas(){
    }

    public static void listarProductos(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println("No hay productos en el inventario.");
            return;
        }

        List<Producto> productosOrdenados = GestorProductos.ordenarProductos(productos);

        // Mostrar los productos ordenados
        System.out.println("\nLISTADO DE PRODUCTOS");
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

    private static void mostrarProducto(Producto p) {
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
    }

    public static void buscarProductoPorCodigo(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println(ColoresConsola.pintar("No hay productos en el inventario.", ColoresConsola.ROJO));
            return;
        }

        String codigoBuscado = Util.leerTextoNoVacio("Código: ").trim();
        boolean encontrado = false;

        for (int i = 0; i < productos.size() && !encontrado; i++) {
            Producto p = productos.get(i);
            
            if (p.getCodigo() != null && p.getCodigo().trim().equalsIgnoreCase(codigoBuscado)) {
                mostrarProducto(p);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún producto con ese código.", ColoresConsola.ROJO));
        }
    }

    public static void buscarProductoPorNombre(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println(ColoresConsola.pintar("No hay productos en el inventario.", ColoresConsola.ROJO));
            return;
        }

        String nombreBuscado = Util.leerTextoNoVacio("Nombre: ").trim();

        boolean encontrado = false;

        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);

            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(nombreBuscado.toLowerCase())) {
                mostrarProducto(p);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se encontraron productos con ese nombre.", ColoresConsola.ROJO));
        }
    }
}