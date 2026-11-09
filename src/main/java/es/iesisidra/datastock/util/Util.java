package es.iesisidra.datastock.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Utilidad compartida para solicitar datos por consola.
 * <p>Utiliza un único {@link Scanner} para toda la aplicación. Se leen líneas
 * completas y se convierten cuando procede, evitando conflictos entre
 * {@code nextInt()} y {@code nextLine()}.</p>
 */
public final class Util {
    private static final Scanner TECLADO = new Scanner(System.in, System.console() != null ? System.console().charset() : Charset.defaultCharset());

    private Util() {
    }

    /** Lee una línea; permite una cadena vacía. */
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!TECLADO.hasNextLine()) {
            // Entrada cerrada (por ejemplo, ejecución no interactiva).
            System.out.println();
            System.exit(0);
        }
        return TECLADO.nextLine();
    }

    public static String leerTextoNoVacio(String mensaje) {
        boolean esValido = false;
        String cadena;

        do {
            cadena = leerTexto(mensaje);

            if (cadena == null || cadena.isEmpty()) {
                System.out.println("Debes introducir un valor.");
            } else {

                esValido = true;
            }

        } while (!esValido);

        return cadena;
    }

    /** Lee un entero; repite la pregunta ante una entrada no válida. */
    public static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try { return Integer.parseInt(texto); }
            catch (NumberFormatException ex) {
                System.out.println(ColoresConsola.pintar("Introduce un número entero válido.", ColoresConsola.AMARILLO));
            }
        }
    }

    /** Lee un double; repite la pregunta ante una entrada no válida. */
    public static double leerDouble(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try { return Double.parseDouble(texto); }
            catch (NumberFormatException ex) {
                System.out.println(ColoresConsola.pintar("Introduce un número decimal válido.", ColoresConsola.AMARILLO));
            }
        }
    }

    /** Lee una opción comprendida entre los límites indicados. */
    public static int leerOpcion(String mensaje, int minimo, int maximo) {
        while (true) {
            int opcion = leerEntero(mensaje);
            if (opcion >= minimo && opcion <= maximo) return opcion;
            System.out.println(ColoresConsola.pintar("Opción fuera de rango.", ColoresConsola.AMARILLO));
        }
    }

    /** Solicita confirmación con S/N y devuelve true únicamente si se elige S. */
    public static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (S/N): ").trim();
            if (respuesta.equalsIgnoreCase("S")) return true;
            if (respuesta.equalsIgnoreCase("N")) return false;
            System.out.println("Responde S o N.");
        }
    }

    public static File pedirArchivo() {
        String entrada = Util.leerTextoNoVacio("Ruta: ");
        System.out.println();

        File fichero = new File(entrada);

        if (!fichero.exists() || !fichero.isFile()) {
            System.out.println("[ERROR] El fichero no existe/no es un fichero.");
            return null;
        }

        return fichero;
    }

    public static boolean validarDestino(File fichero) {

        File directorio = fichero.getAbsoluteFile().getParentFile();

        // Comprobar si existe el directorio
        if (directorio == null || !directorio.exists() || !directorio.isDirectory()) {
            System.out.println("[ERROR] El directorio de destino no existe.");
            return false;
        }

        // Comprobar si la ruta es un directorio
        if (fichero.isDirectory()) {
            System.out.println("[ERROR] La ruta indicada corresponde a un directorio, no a un archivo.");
            return false;
        }

        return true;
    }

    public static boolean confirmarSobrescritura(File fichero) {
        if (fichero.exists()) {
            if (!confirmar("El fichero ya existe. ¿Quieres sobrescribirlo?")) {
                return false;
            }
        }

        return true;
    }

    /** Detiene el menú hasta que el usuario pulse Intro. */
    public static void pausar() { leerTexto("Pulsa Intro para continuar..."); }
}
