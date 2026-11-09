package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.util.ColoresConsola;

public class GestorBD {
    private GestorBD() {
    }

    public static void comprobarConexion() {
        try (Connection conexion = ConexionConfig.conectar()) {
            // Obtener información del gestor de bases de datos
            DatabaseMetaData metadata = conexion.getMetaData();

            // Consultar el número de productos
            String sql = "SELECT COUNT(*) FROM producto";

            try (Statement sentencia = conexion.createStatement()) {
                sentencia.setQueryTimeout(5);

                try (ResultSet resultado = sentencia.executeQuery(sql)) {
                    if (resultado.next()) {

                        System.out.println("\n--- INFORMACIÓN DE MYSQL ---");
                        System.out.println(ColoresConsola.pintar("[OK] Conexión establecida correctamente.", ColoresConsola.VERDE));
                        System.out.println("Base de datos: " + conexion.getCatalog());
                        System.out.println("Gestor: " + metadata.getDatabaseProductName());
                        System.out.println("Versión: " + metadata.getDatabaseProductVersion());
                        System.out.println("Número de productos: " + resultado.getInt(1));
                        System.out.println("-----------------------------");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido conectar o acceder al esquema de MySQL: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(),ColoresConsola.ROJO));
        }
    }

    public static void demostrarTransaccionBD() {

        System.out.println("\n--- DEMOSTRACIÓN DE TRANSACCIONES JDBC ---");

        int id1 = 900001;
        int id2 = 900002;
        int id3 = 900003;

        String comprobar = "SELECT id FROM categoria WHERE id IN (?, ?, ?)";
        String insertar = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

        try (Connection conexion = ConexionConfig.conectar()) {
            // Comprobar que los IDs de prueba están libres
            try (PreparedStatement sentencia = conexion.prepareStatement(comprobar)) {

                sentencia.setInt(1, id1);
                sentencia.setInt(2, id2);
                sentencia.setInt(3, id3);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] Alguno de los IDs de prueba ya existe.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            boolean autoCommitOriginal = conexion.getAutoCommit();

            try (PreparedStatement sentencia = conexion.prepareStatement(insertar)) {
                // PRUEBA 1: COMMIT
                System.out.println("\n--- PRUEBA 1: COMMIT ---");

                conexion.setAutoCommit(false);

                sentencia.setInt(1, id1);
                sentencia.setString(2, "Prueba Commit 1");
                sentencia.executeUpdate();

                sentencia.setInt(1, id2);
                sentencia.setString(2, "Prueba Commit 2");
                sentencia.executeUpdate();

                conexion.commit();

                System.out.println(ColoresConsola.pintar("[OK] Las dos categorías se han guardado con COMMIT.", ColoresConsola.VERDE));

                // PRUEBA 2: ROLLBACK
                System.out.println("\n--- PRUEBA 2: ROLLBACK ---");

                try {

                    sentencia.setInt(1, id3);
                    sentencia.setString(2, "Prueba Rollback");
                    sentencia.executeUpdate();

                    // Provocar un error: ID duplicado
                    sentencia.setInt(1, id3);
                    sentencia.setString(2, "Prueba Duplicada");
                    sentencia.executeUpdate();

                    conexion.commit();

                } catch (SQLException e) {
                    conexion.rollback();
                    System.out.println(ColoresConsola.pintar("[OK] Error provocado: " + e.getMessage(), ColoresConsola.VERDE));

                    System.out.println(ColoresConsola.pintar("[OK] ROLLBACK ejecutado. La primera inserción " + "de esta segunda prueba se ha deshecho.", ColoresConsola.VERDE));
                }
            } catch (SQLException e) {
                conexion.rollback();
                System.out.println(ColoresConsola.pintar("[ERROR] Error durante la transacción: " + e.getMessage(), ColoresConsola.ROJO));

            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }

        } catch (SQLException | IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido realizar la demostración: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
}