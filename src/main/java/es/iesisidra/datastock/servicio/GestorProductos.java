package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

public class GestorProductos {
    private GestorProductos() {
    }

    public static List<String> validarProducto(Producto p, List<Producto> lista) {
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

    public static List<Producto> ordenarProductos(List<Producto> productos) {
        List<Producto> productosOrdenados = new ArrayList<>(productos);

        for (int i = 0; i < productosOrdenados.size() - 1; i++) {
            for (int j = 0; j < productosOrdenados.size() - 1 - i; j++) {

                String codigo1 = productosOrdenados.get(j).getCodigo();
                String codigo2 = productosOrdenados.get(j + 1).getCodigo();

                if (codigo1.compareToIgnoreCase(codigo2) > 0) {
                    Producto auxiliar = productosOrdenados.get(j);
                    productosOrdenados.set(j, productosOrdenados.get(j + 1));
                    productosOrdenados.set(j + 1, auxiliar);
                }
            }
        }
        return productosOrdenados;
    }
}