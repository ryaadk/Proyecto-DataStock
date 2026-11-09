
package es.iesisidra.datastock.modelo;

import java.math.BigDecimal;

public class ProductoConsultaDTO {

    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private String categoria;
    private String proveedor;

    public ProductoConsultaDTO(String codigo, String nombre,
            BigDecimal precio, int stock,
            String categoria, String proveedor) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }

    @Override
    public String toString() {
        return "Código: " + codigo + "\nNombre: " + nombre + "\nPrecio: " + precio + " €" + "\nStock: " + stock + "\nCategoría: " + categoria + "\nProveedor: " + proveedor;
    }
}