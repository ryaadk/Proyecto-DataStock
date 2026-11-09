package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class GestorCategorias {

    private GestorCategorias () {
    }

    public static void altaCategoria() {
        System.out.println("\n--- ALTA DE CATEGORÍA ---");
        System.out.println("----------------------------------------");

        // Solicitar el identificador
        int id = Util.leerEntero("ID de categoría: ");

        // Comprobar que el identificador sea positivo
        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        // Solicitar el nombre
        String nombre = Util.leerTextoNoVacio("Nombre: ").trim();

        if (nombre.length() > 60) {
            System.out.println(ColoresConsola.pintar("[ERROR] El nombre no puede superar los 60 caracteres.", ColoresConsola.ROJO));
            return;
        }

        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

        try (Connection conexion = ConexionConfig.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            sentencia.setString(2, nombre);

            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                System.out.println(ColoresConsola.pintar("[OK] Categoría registrada correctamente.", ColoresConsola.VERDE));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido registrar la categoría: " + e.getMessage(), ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    // Listar todas las categorías
    public static void listarCategorias() {

        System.out.println("\n--- LISTADO DE CATEGORÍAS ---");
        System.out.println("----------------------------------------");

        String sql = "SELECT id, nombre FROM categoria ORDER BY id";

        try (Connection conexion = ConexionConfig.conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            boolean hayCategorias = false;

            System.out.printf("%-10s %-30s%n", "ID", "NOMBRE");
            System.out.println("----------------------------------------");

            while (resultado.next()) {

                hayCategorias = true;

                int id = resultado.getInt("id");
                String nombre = resultado.getString("nombre");

                System.out.printf("%-10d %-30s%n", id, nombre);
            }

            if (!hayCategorias) {
                System.out.println("[AVISO] No hay categorías registradas.");
            }

            System.out.println("----------------------------------------");

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se han podido consultar las categorías.", ColoresConsola.ROJO));

        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    public static void modificarCategoria() {

        System.out.println("\n--- MODIFICAR CATEGORÍA ---");
        System.out.println("----------------------------------------");

        // Solicitar el ID de la categoría
        int id = Util.leerEntero("ID de categoría: ");

        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";

        try (Connection conexion = ConexionConfig.conectar()) {

            // Comprobar que la categoría existe
            String consulta = "SELECT id FROM categoria WHERE id = ?";

            try (PreparedStatement comprobar = conexion.prepareStatement(consulta)) {
                comprobar.setInt(1, id);

                try (ResultSet resultado = comprobar.executeQuery()) {
                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe una categoría con ese ID.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            // Solicitar el nuevo nombre
            String nombre = Util.leerTextoNoVacio("Nuevo nombre: ").trim();

            if (nombre.length() > 60) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre no puede superar los 60 caracteres.", ColoresConsola.ROJO));
                return;
            }

            // Modificar la categoría
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, nombre);
                sentencia.setInt(2, id);
                int filas = sentencia.executeUpdate();

                if (filas == 1) {
                    System.out.println(ColoresConsola.pintar("[OK] Categoría modificada correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se ha modificado ninguna categoría.", ColoresConsola.AMARILLO));
                }
            }
        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido modificar la categoría : " + e.getMessage(), ColoresConsola.ROJO));
        
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void eliminarCategoria() {
        System.out.println("\n--- ELIMINAR CATEGORÍA ---");
        System.out.println("----------------------------------------");

        // Solicitar el ID de la categoría
        int id = Util.leerEntero("ID de categoría: ");

        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        try (Connection conexion = ConexionConfig.conectar()) {
            // Comprobar que la categoría existe
            String consulta = "SELECT id FROM categoria WHERE id = ?";

            try (PreparedStatement comprobar = conexion.prepareStatement(consulta)) {
                comprobar.setInt(1, id);
                try (ResultSet resultado = comprobar.executeQuery()) {

                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe una categoría con ese ID.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            // Solicitar confirmación antes de eliminar
            if (!Util.confirmar("¿Quieres eliminar esta categoría? (S/N): ")) {
                System.out.println(ColoresConsola.pintar("[AVISO] Eliminación cancelada.", ColoresConsola.AMARILLO));
                return;
            }

            // Eliminar la categoría
            String sql = "DELETE FROM categoria WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, id);
                int filas = sentencia.executeUpdate();

                if (filas == 1) {
                    System.out.println(ColoresConsola.pintar("[OK] Categoría eliminada correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se ha eliminado ninguna categoría.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido eliminar la categoría: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
}