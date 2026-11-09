package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación
 * y el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;

    /** @param inventario colección común de productos de esta ejecución. */
    public MenuPrincipal(Inventario inventario) { this.inventario = inventario; }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("7. Gestionar base de datos MySQL");
            System.out.println("0. Salir");

            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 7);
            
            switch (opcion) {
                case 1 -> inventario.explorarRuta();
                case 2 -> inventario.importarJson();
                case 3 -> inventario.importarXml();
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 7 -> menuMysql();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void menuConsultas() {
        int opcion;

        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> inventario.listarProductos();
                case 2 -> inventario.buscarProductoPorCodigo();
                case 3 -> inventario.buscarProductoPorNombre();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;

        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> inventario.exportarJson();
                case 2 -> inventario.exportarXml();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;

        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> inventario.guardarInventario();
                case 2 -> inventario.recuperarInventario();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuMysql() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN MYSQL ----------");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultas productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 9);
            switch (opcion) {
                case 1 -> inventario.comprobarConexion();
                case 2 -> menuCategorias();
                case 3 -> menuProveedores();
                case 4 -> menuProductos();
                case 5 -> menuConsultasSQL();
                case 6 -> inventario.registrarMovimiento();
                case 7 -> menuHisorial();
                case 8 -> inventario.importarProductosMemoriaBD();
                case 9 -> inventario.demostrarTransaccionBD();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuCategorias() {
        int opcion;

        do {
            System.out.println("\n---------- GESTION CATEGORÍAS ----------");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> inventario.altaCategoria();
                case 2 -> inventario.listarCategorias();
                case 3 -> inventario.modificarCategoria();
                case 4 -> inventario.eliminarCategoria();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuProveedores() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN PROVEEDORES ----------");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menú MySQL");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> inventario.altaProveedor();
                case 2 -> inventario.listarProveedores();
                case 3 -> inventario.modificarProveedor();
                case 4 -> inventario.eliminarProveedor();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuProductos() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN PRODUCTOS ----------");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> inventario.altaProducto();
                case 2 -> inventario.consultarProducto();
                case 3 -> inventario.modificarProducto();
                case 4 -> inventario.eliminarProducto();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuConsultasSQL() {
        int opcion;

        do {
            System.out.println("\n---------- CONSULTA MYSQL ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock bajo");
            System.out.println("0. Volver al menú principal");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> inventario.listarProductosBD();
                case 2 -> inventario.consultarProducto();
                case 3 -> inventario.buscarProductoPorNombreBD();
                case 4 -> inventario.productosStockBajoBD();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuHisorial() {
        int opcion;

        do {
            System.out.println("\n---------- GESTIÓN HISTORIAL ----------");
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = Util.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> inventario.consultarTodosMovimientos();
                case 2 -> inventario.consultarMovimientosPorProducto();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }
}