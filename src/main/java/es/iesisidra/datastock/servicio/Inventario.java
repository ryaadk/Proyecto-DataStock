package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;


public class Inventario {
    private final List<Producto> PRODUCTOS = new ArrayList<>();

    public List<Producto> getProductos() { 
        return PRODUCTOS; 
    }

    public void explorarRuta() {
        InventarioArchivos.explorar();
    }

    public void importarJson() {
        InventarioJSON.importar(PRODUCTOS);
    }

    public void listarProductos() {
        Consultas.listarProductos(PRODUCTOS);
    }

    public void buscarProductoPorCodigo() {
        Consultas.buscarProductoPorCodigo(PRODUCTOS);
    }

    public void buscarProductoPorNombre() {
        Consultas.buscarProductoPorNombre(PRODUCTOS);
    }

    public void importarXml() {
        InventarioXML.importarXml(PRODUCTOS);
    }

    public void exportarJson() {
        InventarioJSON.exportarJson(PRODUCTOS);
    }

    public void exportarXml() {
        InventarioXML.exportarXml(PRODUCTOS);
    }

    public void guardarInventario() {
        InventarioDAT.guardarInventario(PRODUCTOS);
    }

    public void recuperarInventario() {
        InventarioDAT.recuperarInventario(PRODUCTOS);
    }

    public void comprobarConexion() {
        GestorBD.comprobarConexion();
    }

    public void altaCategoria() {
        GestorCategorias.altaCategoria();
    }

    public void listarCategorias() {
        GestorCategorias.listarCategorias();
    }

    public void modificarCategoria() {
        GestorCategorias.modificarCategoria();
    }

    public void eliminarCategoria() {
        GestorCategorias.eliminarCategoria();
    }

    public void altaProveedor() {
        GestorProveedores.altaProveedor();
    }

    public void listarProveedores() {
        GestorProveedores.listarProveedores();
    }

    public void modificarProveedor() {
        GestorProveedores.modificarProveedor();
    }

    public void eliminarProveedor() {
        GestorProveedores.eliminarProveedor();
    }
}