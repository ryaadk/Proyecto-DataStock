package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DemoSQLite {

    public static void main(String[] args) {

        String ruta = "datos/sqlite/productos_demo.db";
        String url = "jdbc:sqlite:" + ruta;

        try {
            // Crear directorio si no existe
            Files.createDirectories(Path.of("datos/sqlite"));

            // Abrir conexión SQLite
            try (Connection conexion = DriverManager.getConnection(url)) {

                System.out.println("[OK] Conexión SQLite abierta: " + ruta);

                // Crear tabla
                String sqlTabla = "CREATE TABLE IF NOT EXISTS producto_demo (codigo TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL)";

                try (Statement st = conexion.createStatement()) {
                    st.executeUpdate(sqlTabla);
                    System.out.println("[OK] Tabla producto_demo disponible.");
                }

                // Insertar productos
                String sqlInsertar = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";

                int insertados = 0;

                try (PreparedStatement ps = conexion.prepareStatement(sqlInsertar)) {

                    ps.setString(1, "SQL-001");
                    ps.setString(2, "Producto demo A");
                    insertados += ps.executeUpdate();

                    ps.setString(1, "SQL-002");
                    ps.setString(2, "Producto demo B");
                    insertados += ps.executeUpdate();
                }

                System.out.println("Insertados: " + insertados + " | Ya existentes: " + (2 - insertados));

                // Consultar productos
                String sqlSelect = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo";

                try (Statement st = conexion.createStatement();
                     ResultSet rs = st.executeQuery(sqlSelect)) {

                    while (rs.next()) {
                        System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
                    }
                }
            }

            // Comprobar que el fichero existe
            if (Files.exists(Path.of(ruta))) {
                System.out.println("[OK] Fichero SQLite persistente: " + ruta);
            }

        } catch (IOException | SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
