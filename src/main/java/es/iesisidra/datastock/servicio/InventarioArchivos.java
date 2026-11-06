package es.iesisidra.datastock.servicio;

import java.io.File;

import es.iesisidra.datastock.util.Util;

public class InventarioArchivos {
    
    private InventarioArchivos() {    
    }

    public static void explorar() {
        // Pido ruta y hago validaciones (Si existe, si esta vacía y si es un directorio)
        String entrada = Util.leerTexto("Ruta: ");

        if (entrada == null || entrada.isBlank()) {
            System.out.println("[ERROR] Debes introducir una ruta.");
            return;
        }

        File ruta = new File(entrada);

        if (!ruta.exists()) {
            System.out.println("[ERROR] La ruta introducida no existe.");
            return;
        }

        if (!ruta.isDirectory()) {
            System.out.println("[ERROR] La ruta no es un directorio.");
            return;
        }

        // 2 contadores, Ficheros - Directorios
        // Paso el contador a la otra funcion pora modificarlas
        // El contador cuenta también los subdirectorios
        int[] contadores = {0, 0}; 
        

        explorarDirectorio(ruta, contadores);

        // Una vez termina la función imprimo por pantalla el total
        System.out.println();
        System.out.println("Total ficheros: " + contadores[0] + " | Total directorios: " + contadores[1]);
    }

    private static void explorarDirectorio(File ruta, int[] contadores) {
        // Hago un bucle, si hay elementos, se ejecutará el bucle, si no hay elementos no ejecutará nada
        File[] elementos = ruta.listFiles();

        if (elementos == null) {
            System.out.println("[ERROR] No se puede acceder a: " + ruta.getAbsolutePath());
            return;
        }

        for (File elemento : elementos) {
            // Comprobar si es archivo o directorio e imprimir datos correspondientes
            if (elemento.isFile()) {
                System.out.println("[FICHERO] " + elemento.getName());
                System.out.println("[RUTA ABSOLUTA] " + elemento.getAbsolutePath());
                System.out.println("[BYTES] " + elemento.length());

                contadores[0]++;

            } else if (elemento.isDirectory()) {
                System.out.println("[DIRECTORIO] " + elemento.getName());

                contadores[1]++;
                // Vuelve a ejecutar esta función para hacer otra vez el bucle y revisar el contenido de este directorio.
                explorarDirectorio(elemento, contadores);
            }
        }
    }
}
