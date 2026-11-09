package es.iesisidra.datastock.servicio;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class GestorMovimientos {
    private GestorMovimientos() {

    }

    public static void registrarMovimiento() {

        System.out.println("\n--- REGISTRAR MOVIMIENTO DE STOCK ---");
        System.out.println("----------------------------------------");

        String codigo = Util.leerTextoNoVacio("Código del producto: ").trim().toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código no es válido.", ColoresConsola.ROJO));
            return;
        }

        String tipo = Util.leerTextoNoVacio("Tipo (ENTRADA/SALIDA): ").trim().toUpperCase(Locale.ROOT);

        if (!tipo.equals("ENTRADA") && !tipo.equals("SALIDA")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El tipo debe ser ENTRADA o SALIDA.", ColoresConsola.ROJO));
            return;
        }

        int cantidad = Util.leerEntero("Cantidad: ");

        if (cantidad <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] La cantidad debe ser mayor que cero.", ColoresConsola.ROJO));
            return;
        }

        String sql = "{CALL registrar_movimiento(?, ?, ?)}";

        try (Connection conexion = ConexionConfig.conectar();
            CallableStatement sentencia = conexion.prepareCall(sql)) {

            sentencia.setString(1, codigo);
            sentencia.setString(2, tipo);
            sentencia.setInt(3, cantidad);

            sentencia.execute();

            System.out.println(ColoresConsola.pintar("[OK] Movimiento registrado correctamente.", ColoresConsola.VERDE));

            // Consultar el stock actualizado
            String consultaStock = "SELECT stock FROM producto WHERE codigo = ?";

            try (PreparedStatement consulta = conexion.prepareStatement(consultaStock)) {
                consulta.setString(1, codigo);

                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next()) {
                        System.out.println("Stock actual: " + resultado.getInt("stock"));
                    }
                }
            }

            // Consultar el último movimiento
            String consultaMovimiento = "SELECT tipo, cantidad, stock_anterior, "
                    + "stock_posterior, fecha "
                    + "FROM movimiento_stock "
                    + "WHERE producto_codigo = ? "
                    + "ORDER BY id DESC LIMIT 1";

            try (PreparedStatement consulta = conexion.prepareStatement(consultaMovimiento)) {
                consulta.setString(1, codigo);

                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next()) {

                        System.out.println("\n--- ÚLTIMO MOVIMIENTO ---");
                        System.out.println("Tipo: " + resultado.getString("tipo"));
                        System.out.println("Cantidad: " + resultado.getInt("cantidad"));
                        System.out.println("Stock anterior: " + resultado.getInt("stock_anterior"));
                        System.out.println("Stock posterior: " + resultado.getInt("stock_posterior"));
                        System.out.println("Fecha: " + resultado.getTimestamp("fecha"));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido registrar el movimiento: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void consultarTodosMovimientos() {

        System.out.println("\n--- HISTORIAL DE MOVIMIENTOS ---");
        System.out.println("----------------------------------------");

        String sql = "SELECT id, producto_codigo, fecha, tipo, cantidad, "
                + "stock_anterior, stock_posterior "
                + "FROM movimiento_stock "
                + "ORDER BY fecha ASC, id ASC";

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery()) {

            boolean hayMovimientos = false;

            while (resultado.next()) {
                hayMovimientos = true;

                System.out.println("\n----------------------------------------");
                System.out.println("ID: " + resultado.getLong("id"));
                System.out.println("Producto: " + resultado.getString("producto_codigo"));
                System.out.println("Fecha: " + resultado.getTimestamp("fecha"));
                System.out.println("Tipo: " + resultado.getString("tipo"));
                System.out.println("Cantidad: " + resultado.getInt("cantidad"));
                System.out.println("Stock anterior: " + resultado.getInt("stock_anterior"));
                System.out.println("Stock posterior: " + resultado.getInt("stock_posterior"));
            }

            if (!hayMovimientos) {
                System.out.println(ColoresConsola.pintar("[AVISO] No hay movimientos registrados.", ColoresConsola.AMARILLO));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido consultar el historial.", ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    public static void consultarMovimientosPorProducto() {

        System.out.println("\n--- MOVIMIENTOS POR PRODUCTO ---");
        System.out.println("----------------------------------------");

        String codigo = Util.leerTextoNoVacio("Código del producto: ").trim().toUpperCase(Locale.ROOT);

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código introducido no es válido.", ColoresConsola.ROJO));
            return;
        }

        try (Connection conexion = ConexionConfig.conectar()) {
            String sqlProducto = "SELECT codigo FROM producto WHERE codigo = ?";

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlProducto)) {
                sentencia.setString(1, codigo);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún producto con ese código.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            String sql = "SELECT id, producto_codigo, fecha, tipo, cantidad, "
                    + "stock_anterior, stock_posterior "
                    + "FROM movimiento_stock "
                    + "WHERE producto_codigo = ? "
                    + "ORDER BY fecha ASC, id ASC";

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, codigo);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    boolean hayMovimientos = false;

                    while (resultado.next()) {
                        hayMovimientos = true;

                        System.out.println("\n----------------------------------------");
                        System.out.println("ID: " + resultado.getLong("id"));
                        System.out.println("Producto: " + resultado.getString("producto_codigo"));
                        System.out.println("Fecha: " + resultado.getTimestamp("fecha"));
                        System.out.println("Tipo: " + resultado.getString("tipo"));
                        System.out.println("Cantidad: " + resultado.getInt("cantidad"));
                        System.out.println("Stock anterior: " + resultado.getInt("stock_anterior"));
                        System.out.println("Stock posterior: " + resultado.getInt("stock_posterior"));
                    }

                    if (!hayMovimientos) {
                        System.out.println(ColoresConsola.pintar("[AVISO] El producto existe, pero no tiene movimientos registrados.", ColoresConsola.AMARILLO));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se han podido consultar los movimientos.", ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
}