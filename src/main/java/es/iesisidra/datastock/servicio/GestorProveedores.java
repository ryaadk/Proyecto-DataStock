package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class GestorProveedores {

    // Dar de alta un proveedor
    public static void altaProveedor() {

        System.out.println("\n--- ALTA DE PROVEEDOR ---");
        System.out.println("----------------------------------------");

        // Solicitar identificador
        int id = Util.leerEntero("ID de proveedor: ");

        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        // Solicitar nombre
        String nombre = Util.leerTextoNoVacio("Nombre: ").trim();

        if (nombre.isEmpty()) {
            System.out.println(ColoresConsola.pintar("[ERROR] El nombre no puede estar vacío.", ColoresConsola.ROJO));
            return;
        }

        if (nombre.length() > 100) {
            System.out.println(ColoresConsola.pintar("[ERROR] El nombre no puede superar los 100 caracteres.",ColoresConsola.ROJO));
            return;
        }

        // Solicitar email opcional
        String email = Util.leerTexto("Email (opcional): ").trim();

        if (email.isEmpty()) {
            email = null;

        } else if (email.length() > 120) {
            System.out.println(ColoresConsola.pintar("[ERROR] El email no puede superar los 120 caracteres.",ColoresConsola.ROJO));
            return;
        }

        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            sentencia.setString(2, nombre);

            if (email == null) {
                sentencia.setNull(3, Types.VARCHAR);

            } else {
                sentencia.setString(3, email);
            }

            int filas = sentencia.executeUpdate();

            if (filas == 1) {
                System.out.println(ColoresConsola.pintar("[OK] Proveedor registrado correctamente.", ColoresConsola.VERDE));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido registrar el proveedor: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    // Listar todos los proveedores
    public static void listarProveedores() {
        System.out.println("\n--- LISTADO DE PROVEEDORES ---");
        System.out.println("--------------------------------------------------");

        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";

        try (Connection conexion = ConexionConfig.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet resultado = sentencia.executeQuery(sql)) {

            boolean hayProveedores = false;

            System.out.printf("%-10s %-25s %-30s%n", "ID", "NOMBRE", "EMAIL");
            System.out.println("--------------------------------------------------");

            while (resultado.next()) {
                hayProveedores = true;

                int id = resultado.getInt("id");
                String nombre = resultado.getString("nombre");
                String email = resultado.getString("email");

                if (email == null) {
                    email = "Sin email";
                }

                System.out.printf("%-10d %-25s %-30s%n", id, nombre, email);
            }

            if (!hayProveedores) {
                System.out.println(ColoresConsola.pintar("[AVISO] No hay proveedores registrados.", ColoresConsola.AMARILLO));
            }

            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se han podido consultar los proveedores: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    public static void modificarProveedor() {

        System.out.println("\n--- MODIFICAR PROVEEDOR ---");
        System.out.println("----------------------------------------");

        // Solicitar ID
        int id = Util.leerEntero("ID de proveedor: ");

        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        try (Connection conexion = ConexionConfig.conectar()) {
            // Comprobar que existe el proveedor
            String consulta = "SELECT id FROM proveedor WHERE id = ?";
            try (PreparedStatement comprobar = conexion.prepareStatement(consulta)) {
                comprobar.setInt(1, id);

                try (ResultSet resultado = comprobar.executeQuery()) {
                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe un proveedor con ese ID.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            // Solicitar nuevo nombre
            String nombre = Util.leerTextoNoVacio("Nuevo nombre: ").trim();

            if (nombre.length() > 100) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre no puede superar los 100 caracteres.", ColoresConsola.ROJO));
                return;
            }

            // Solicitar nuevo email
            String email = Util.leerTexto("Nuevo email (opcional, vacío para eliminarlo): ").trim();

            if (email.isEmpty()) {
                email = null;

            } else if (email.length() > 120) {
                System.out.println(ColoresConsola.pintar("[ERROR] El email no puede superar los 120 caracteres.", ColoresConsola.ROJO));
                return;
            }

            // Actualizar proveedor
            String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, nombre);

                if (email == null) {
                    sentencia.setNull(2, Types.VARCHAR);

                } else {
                    sentencia.setString(2, email);
                }

                sentencia.setInt(3, id);
                int filas = sentencia.executeUpdate();

                if (filas == 1) {
                    System.out.println(ColoresConsola.pintar("[OK] Proveedor modificado correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se han realizado cambios en el proveedor.", ColoresConsola.AMARILLO));
                }
            }

        } catch (SQLException e) { 
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido modificar el proveedor: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }


    public static void eliminarProveedor() {
        System.out.println("\n--- ELIMINAR PROVEEDOR ---");
        System.out.println("----------------------------------------");

        // Solicitar ID
        int id = Util.leerEntero("ID de proveedor: ");

        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser positivo.", ColoresConsola.ROJO));
            return;
        }

        try (Connection conexion = ConexionConfig.conectar()) {
            // Comprobar que existe el proveedor
            String consulta = "SELECT id FROM proveedor WHERE id = ?";

            try (PreparedStatement comprobar = conexion.prepareStatement(consulta)) {
                comprobar.setInt(1, id);

                try (ResultSet resultado = comprobar.executeQuery()) {
                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe un proveedor con ese ID.", ColoresConsola.ROJO));
                        return;
                    }
                }
            }

            // Solicitar confirmación
            if (!Util.confirmar("¿Quieres eliminar este proveedor? (S/N): ")) {
                System.out.println(ColoresConsola.pintar("[AVISO] Eliminación cancelada.", ColoresConsola.AMARILLO));
                return;
            }

            // Eliminar proveedor
            String sql = "DELETE FROM proveedor WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, id);
                int filas = sentencia.executeUpdate();

                if (filas == 1) {
                    System.out.println(ColoresConsola.pintar("[OK] Proveedor eliminado correctamente.", ColoresConsola.VERDE));
                } else {
                    System.out.println(ColoresConsola.pintar("[AVISO] No se ha eliminado ningún proveedor.", ColoresConsola.AMARILLO));
                }
            }
        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se ha podido eliminar el proveedor.", ColoresConsola.ROJO));
        } catch (IllegalStateException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
} 