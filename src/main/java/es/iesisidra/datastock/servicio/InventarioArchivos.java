package es.iesisidra.datastock.servicio;

import java.io.File;

import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.Util;

public class InventarioArchivos {
    
    private InventarioArchivos() {    
    }

    public static void explorar() {
        // Pido ruta y hago validaciones (Si existe, si esta vacía y si es un directorio)
        String entrada = Util.leerTextoNoVacio("Ruta: ");

        File ruta = new File(entrada);

        if (!ruta.exists()) {
            System.out.println(ColoresConsola.pintar("[ERROR] La ruta introducida no existe.", ColoresConsola.ROJO));
            return;
        }

        if (!ruta.isDirectory()) {
            System.out.println(ColoresConsola.pintar("[ERROR] La ruta no es un directorio.", ColoresConsola.ROJO));
            return;
        }

        int[] contadores = {0, 0}; 
        explorarDirectorio(ruta, contadores);

        // Una vez termina la función imprimo por pantalla el total
        System.out.println();
        System.out.println(ColoresConsola.pintar("Total ficheros: " + contadores[0] + " | Total directorios: " + contadores[1], ColoresConsola.VERDE));
    }

    private static void explorarDirectorio(File ruta, int[] contadores) {
        // Hago un bucle, si hay elementos, se ejecutará el bucle, si no hay elementos no ejecutará nada
        File[] elementos = ruta.listFiles();

        if (elementos == null) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se puede acceder a: " + ruta.getAbsolutePath(), ColoresConsola.ROJO));
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
                explorarDirectorio(elemento, contadores);
            }
        }
    }
}
