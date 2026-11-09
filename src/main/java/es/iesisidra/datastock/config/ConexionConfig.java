package es.iesisidra.datastock.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionConfig {

    public static Connection conectar() throws SQLException {
        Properties propiedades = new Properties();

        try (InputStream entrada = ConexionConfig.class.getClassLoader().getResourceAsStream("database.properties")) {

            if (entrada == null) {
                throw new IllegalStateException("No se encuentra el archivo database.properties.");
            }

            propiedades.load(entrada);

        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar la configuración de MySQL.");
        }

        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String password = propiedades.getProperty("db.password");

        if (url == null || url.isBlank() || usuario == null || usuario.isBlank() || password == null || password.isBlank()) {

            throw new IllegalStateException("Faltan datos en la configuración de MySQL.");
        }

        return DriverManager.getConnection(url, usuario, password);
    }
}