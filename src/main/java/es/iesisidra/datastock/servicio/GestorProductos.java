package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoConsultaDTO;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

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

    
    public static void altaProducto() {
        System.out.println("\n--- ALTA DE PRODUCTO EN MYSQL ---");

        String codigo = Util.leerTextoNoVacio("Código: ").trim().toUpperCase(Locale.ROOT);
        String nombre = Util.leerTextoNoVacio("Nombre: ").trim();

        BigDecimal precio;

        try {
            precio = BigDecimal.valueOf(Util.leerDouble("Precio: "));
        } catch (NumberFormatException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] El precio introducido no es válido.", ColoresConsola.ROJO));
            return;
        }

        int stock = Util.leerEntero("Stock inicial: ");
        int categoriaId = Util.leerEntero("ID de categoría: ");
        int proveedorId = Util.leerEntero("ID de proveedor: ");

        // Crear el producto
        Producto producto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);

        // Reutilizar la validación existente
        List<String> errores = validarProducto(producto, new ArrayList<Producto>());

        // Validación adicional de precio para MySQL
        if (precio.scale() > 2) {
            errores.add("El precio no puede tener más de dos decimales.");
        }

        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            errores.add("El precio supera el máximo permitido.");
        }

        if (!errores.isEmpty()) {
            for (String error : errores) {
                System.out.println(ColoresConsola.pintar("[ERROR] " + error, ColoresConsola.ROJO));
            }
            return;
        }

        String sql = "INSERT INTO producto " + "(codigo, nombre, precio, stock, categoria_id, proveedor_id) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setBigDecimal(3, producto.getPrecio());
            sentencia.setInt(4, producto.getStock());
            sentencia.setInt(5, producto.getCategoriaId());
            sentencia.setInt(6, producto.getProveedorId());
            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                System.out.println(ColoresConsola.pintar("[OK] Producto registrado correctamente en MySQL.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[AVISO] No se ha insertado ningún producto.", ColoresConsola.AMARILLO));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido registrar el producto: " + e.getMessage(), ColoresConsola.ROJO));
            

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void consultarProducto() {

        System.out.println("\n--- CONSULTAR PRODUCTO POR CÓDIGO ---");
        System.out.println("----------------------------------------");

        String codigo = Util.leerTextoNoVacio("Código del producto: ").trim().toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código introducido no es válido.", ColoresConsola.ROJO));
            return;
        }

        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, "
                + "p.categoria_id, c.nombre AS categoria, "
                + "p.proveedor_id, pr.nombre AS proveedor "
                + "FROM producto p "
                + "JOIN categoria c ON p.categoria_id = c.id "
                + "JOIN proveedor pr ON p.proveedor_id = pr.id "
                + "WHERE p.codigo = ?";

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, codigo);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    System.out.println("\n--- DATOS DEL PRODUCTO ---");
                    System.out.println("----------------------------------------");

                    System.out.println("Código: " + resultado.getString("codigo"));
                    System.out.println("Nombre: " + resultado.getString("nombre"));
                    System.out.println("Precio: " + resultado.getBigDecimal("precio") + " \\u20AC");
                    System.out.println("Stock: " + resultado.getInt("stock"));

                    System.out.println("Categoría: "+ resultado.getInt("categoria_id") + " - " + resultado.getString("categoria"));

                    System.out.println("Proveedor: " + resultado.getInt("proveedor_id") + " - " + resultado.getString("proveedor"));
                    System.out.println("----------------------------------------");

                } else {

                    System.out.println(ColoresConsola.pintar("[AVISO] No existe ningún producto con ese código.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido consultar el producto.", ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void modificarProducto() {

        System.out.println("\n--- MODIFICAR PRODUCTO MYSQL ---");
        System.out.println("----------------------------------------");

        String codigo = Util.leerTextoNoVacio("Código del producto: ").trim().toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código no es válido.", ColoresConsola.ROJO));
            return;
        }

        String consulta = "SELECT * FROM producto WHERE codigo = ?";

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setString(1, codigo);

            int stock;

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún producto con ese código.", ColoresConsola.ROJO));
                    return;
                }

                stock = resultado.getInt("stock");

                System.out.println("\nProducto encontrado:");
                System.out.println("Nombre actual: " + resultado.getString("nombre"));
                System.out.println("Precio actual: " + resultado.getBigDecimal("precio"));
                System.out.println("Categoría actual: " + resultado.getInt("categoria_id"));
                System.out.println("Proveedor actual: " + resultado.getInt("proveedor_id"));
            }

            System.out.println("\nIntroduce los nuevos datos:");
            String nombre = Util.leerTextoNoVacio("Nuevo nombre: ").trim();

            BigDecimal precio;

            try {
                precio = BigDecimal.valueOf(Util.leerDouble("Precio: "));
            } catch (NumberFormatException e) {
                System.out.println(ColoresConsola.pintar("[ERROR] El precio introducido no es válido.", ColoresConsola.ROJO));
                return;
            }

            int categoriaId = Util.leerEntero("Nueva categoría (ID): ");
            int proveedorId = Util.leerEntero("Nuevo proveedor (ID): ");

            Producto producto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);

            List<String> errores = validarProducto(producto, new ArrayList<Producto>());

            if (precio.scale() > 2) {
                errores.add("El precio no puede tener más de dos decimales.");
            }

            if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
                errores.add("El precio supera el máximo permitido.");
            }

            if (!errores.isEmpty()) {
                for (String error : errores) {
                    System.out.println(ColoresConsola.pintar("[ERROR] " + error, ColoresConsola.ROJO));
                }
                return;
            }

            String sql = "UPDATE producto " + "SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? " + "WHERE codigo = ?";

            try (PreparedStatement actualizar = conexion.prepareStatement(sql)) {

                actualizar.setString(1, nombre);
                actualizar.setBigDecimal(2, precio);
                actualizar.setInt(3, categoriaId);
                actualizar.setInt(4, proveedorId);
                actualizar.setString(5, codigo);

                int filas = actualizar.executeUpdate();

                if (filas > 0) {
                    System.out.println(ColoresConsola.pintar("[OK] Producto modificado correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se ha modificado ningún producto.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido modificar el producto.", ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void eliminarProducto() {

        System.out.println("\n--- ELIMINAR PRODUCTO MYSQL ---");
        System.out.println("----------------------------------------");

        String codigo = Util.leerTextoNoVacio("Código del producto: ").trim().toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código introducido no es válido.", ColoresConsola.ROJO));
            return;
        }

        String consulta = "SELECT nombre FROM producto WHERE codigo = ?";

        try (Connection conexion = ConexionConfig.conectar()) {
            try (PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
                sentencia.setString(1, codigo);

                try (ResultSet resultado = sentencia.executeQuery()) {

                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún producto con ese código.", ColoresConsola.ROJO));
                        return;
                    }

                    System.out.println("Producto encontrado: " + resultado.getString("nombre"));
                }
            }

            if (!Util.confirmar("¿Quieres eliminar este producto?")) {
                System.out.println(ColoresConsola.pintar("[AVISO] Eliminación cancelada.", ColoresConsola.AMARILLO));
                return;
            }

            String sql = "DELETE FROM producto WHERE codigo = ?";

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, codigo);

                int filas = sentencia.executeUpdate();

                if (filas == 1) {
                    System.out.println(ColoresConsola.pintar("[OK] Producto eliminado correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] El producto ya no existe.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido eliminar el producto: " + e.getMessage(), ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static ProductoConsultaDTO crearProductoDTO(ResultSet resultado)
            throws SQLException {

        return new ProductoConsultaDTO(
                resultado.getString("codigo"),
                resultado.getString("nombre"),
                resultado.getBigDecimal("precio"),
                resultado.getInt("stock"),
                resultado.getString("categoria"),
                resultado.getString("proveedor")
        );
    }

    private static void mostrarProductos(String sql, String filtro, Integer valor) {
        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            if (filtro != null) {
                sentencia.setString(1, filtro);
            } else if (valor != null) {
                sentencia.setInt(1, valor);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                boolean encontrado = false;

                while (resultado.next()) {
                    encontrado = true;
                    ProductoConsultaDTO producto = crearProductoDTO(resultado);

                    System.out.println("\n----------------------------------------");
                    System.out.println(producto);
                }

                if (!encontrado) {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se han encontrado productos.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido realizar la consulta.", ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    public static void listarProductosBD() {
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, "
                + "c.nombre AS categoria, pr.nombre AS proveedor "
                + "FROM producto p "
                + "JOIN categoria c ON p.categoria_id = c.id "
                + "JOIN proveedor pr ON p.proveedor_id = pr.id "
                + "ORDER BY p.codigo";

        mostrarProductos(sql, null, null);
    }

    public static void buscarProductoPorNombreBD() {

        System.out.println("\n--- BUSCAR PRODUCTO POR NOMBRE ---");

        String nombre = Util.leerTextoNoVacio("Fragmento del nombre: ").trim();

        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, "
                + "c.nombre AS categoria, pr.nombre AS proveedor "
                + "FROM producto p "
                + "JOIN categoria c ON p.categoria_id = c.id "
                + "JOIN proveedor pr ON p.proveedor_id = pr.id "
                + "WHERE p.nombre LIKE ? "
                + "ORDER BY p.codigo";

        mostrarProductos(sql, "%" + nombre + "%", null);
    }

    public static void productosStockBajoBD() {

        System.out.println("\n--- PRODUCTOS CON STOCK BAJO ---");

        int umbral = Util.leerEntero("Introduce el umbral de stock: ");

        if (umbral < 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El umbral no puede ser negativo.", ColoresConsola.ROJO));
            return;
        }

        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, "
                + "c.nombre AS categoria, pr.nombre AS proveedor "
                + "FROM producto p "
                + "JOIN categoria c ON p.categoria_id = c.id "
                + "JOIN proveedor pr ON p.proveedor_id = pr.id "
                + "WHERE p.stock < ? "
                + "ORDER BY p.stock ASC, p.codigo";

        mostrarProductos(sql, null, umbral);
    }


    public static void importarProductosMemoriaBD(List<Producto> inventario) {
        System.out.println("\n--- IMPORTAR PRODUCTOS DE MEMORIA A MYSQL ---");

        if (inventario == null || inventario.isEmpty()) {
            System.out.println(ColoresConsola.pintar("[AVISO] No hay productos en memoria.", ColoresConsola.AMARILLO));
            return;
        }

        int insertados = 0;
        int omitidos = 0;
        int rechazados = 0;

        String comprobarCodigo = "SELECT codigo FROM producto WHERE codigo = ?";
        String comprobarCategoria = "SELECT id FROM categoria WHERE id = ?";
        String comprobarProveedor = "SELECT id FROM proveedor WHERE id = ?";

        String insertar = "INSERT INTO producto "
                + "(codigo, nombre, precio, stock, categoria_id, proveedor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionConfig.conectar()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sqlCodigo = conexion.prepareStatement(comprobarCodigo);
                PreparedStatement sqlCategoria = conexion.prepareStatement(comprobarCategoria);
                PreparedStatement sqlProveedor = conexion.prepareStatement(comprobarProveedor);
                PreparedStatement sqlInsertar = conexion.prepareStatement(insertar)) {

                List<Producto> procesados = new ArrayList<>();

                for (Producto producto : inventario) {
                    if (producto == null) {
                        rechazados++;
                        System.out.println(ColoresConsola.pintar("[ERROR] Producto nulo en memoria.", ColoresConsola.ROJO));
                        continue;
                    }

                    String codigo = producto.getCodigo();

                    if (codigo != null) {
                        codigo = codigo.trim();
                    }

                    // Validar el producto y los duplicados del lote
                    List<String> errores = validarProducto(producto, procesados);

                    if (producto.getPrecio() != null) {
                        if (producto.getPrecio().scale() > 2) {
                            errores.add("El precio tiene más de dos decimales.");
                        }

                        if (producto.getPrecio().compareTo(new BigDecimal("99999999.99")) > 0) {
                            errores.add("El precio supera el máximo permitido.");
                        }
                    }

                    if (!errores.isEmpty()) {
                        rechazados++;

                        System.out.println(ColoresConsola.pintar("[ERROR] Producto rechazado: " + codigo, ColoresConsola.ROJO));

                        for (String error : errores) {
                            System.out.println("  - " + error);
                        }
                        continue;
                    }

                    // Comprobar código existente en MySQL
                    sqlCodigo.setString(1, codigo);

                    try (ResultSet resultado = sqlCodigo.executeQuery()) {
                        if (resultado.next()) {
                            omitidos++;

                            System.out.println(ColoresConsola.pintar("[AVISO] Producto omitido (ya existe): " + codigo, ColoresConsola.AMARILLO));

                            procesados.add(producto);
                            continue;
                        }
                    }

                    // Comprobar categoría
                    sqlCategoria.setInt(1, producto.getCategoriaId());

                    try (ResultSet resultado = sqlCategoria.executeQuery()) {
                        if (!resultado.next()) {
                            rechazados++;

                            System.out.println(ColoresConsola.pintar("[ERROR] " + codigo + ": categoría inexistente.", ColoresConsola.ROJO));

                            procesados.add(producto);
                            continue;
                        }
                    }

                    // Comprobar proveedor
                    sqlProveedor.setInt(1, producto.getProveedorId());

                    try (ResultSet resultado = sqlProveedor.executeQuery()) {
                        if (!resultado.next()) {
                            rechazados++;

                            System.out.println(ColoresConsola.pintar("[ERROR] " + codigo + ": proveedor inexistente.", ColoresConsola.ROJO));

                            procesados.add(producto);
                            continue;
                        }
                    }

                    // Insertar producto válido
                    sqlInsertar.setString(1, codigo);
                    sqlInsertar.setString(2, producto.getNombre().trim());
                    sqlInsertar.setBigDecimal(3, producto.getPrecio());
                    sqlInsertar.setInt(4, producto.getStock());
                    sqlInsertar.setInt(5, producto.getCategoriaId());
                    sqlInsertar.setInt(6, producto.getProveedorId());

                    int filas = sqlInsertar.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException("No se insertó el producto " + codigo);
                    }

                    insertados++;
                    procesados.add(producto);
                }

                conexion.commit();

                System.out.println(ColoresConsola.pintar("\n[OK] Importación finalizada correctamente.", ColoresConsola.VERDE));

                System.out.println("Insertados: " + insertados);
                System.out.println("Omitidos: " + omitidos);
                System.out.println("Rechazados: " + rechazados);

            } catch (SQLException e) {

                try {
                    conexion.rollback();

                    System.out.println(ColoresConsola.pintar("[ERROR] Fallo SQL. Se han deshecho las inserciones del lote.", ColoresConsola.ROJO));

                } catch (SQLException errorRollback) {
                    System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido completar el rollback: " + errorRollback.getMessage(), ColoresConsola.ROJO));
                }

                System.out.println("Motivo: " + e.getMessage());
                System.out.println("Insertados confirmados: 0");
            }

        } catch (SQLException | IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido realizar la importación: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
}