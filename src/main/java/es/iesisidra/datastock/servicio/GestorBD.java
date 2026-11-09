package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido conectar o acceder al esquema de MySQL.", ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(),ColoresConsola.ROJO));
        }
    }
}