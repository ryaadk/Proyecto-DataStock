# DataStock – Proyecto UT1

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

## 1. ¿Qué es este proyecto?

DataStock es una práctica individual e integradora para gestionar un inventario de productos mediante Java. Durante el desarrollo se trabajan distintos formatos de almacenamiento e intercambio de datos.

El proyecto se divide en tres unidades de trabajo:

- **UT1 – Ficheros:** importación JSON/XML, consultas, exportación, serialización, XPath y acceso aleatorio.
- **UT2 – JDBC:** conexión con MySQL, operaciones SQL y transacciones.
- **UT3 – Componentes:** organización del acceso a datos mediante componentes reutilizables.

Esta entrega corresponde a **UT1**, por lo que no requiere conexión a una base de datos.

## 2. Requisitos previos

- JDK 21 instalado y configurado.
- Maven instalado o un IDE con integración Maven.
- IntelliJ IDEA, Eclipse, VS Code u otro entorno compatible.
- Conexión a Internet para descargar las dependencias la primera vez.

Se puede comprobar la instalación con:

```bash
java -version
mvn -version
```

## 3. Cómo abrir y ejecutar

1. Clonar el repositorio o descargar el proyecto.
2. Abrir la carpeta que contiene `pom.xml` como proyecto Maven.
3. Esperar a que Maven descargue las dependencias.
4. Ejecutar la aplicación desde el IDE o desde una terminal situada en la raíz del proyecto.

```bash
mvn clean compile
mvn exec:java
```

Para ejecutar las actividades complementarias de forma independiente:

**RF-FIC-07 – XPath**

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
```

**RF-FIC-08 – RandomAccessFile**

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
```

Las rutas relativas se interpretan desde el directorio de ejecución. Si aparecen caracteres de control ANSI en la consola, se puede desactivar el color mediante `-Ddatastock.color=false` o configurar la variable de entorno `NO_COLOR=1`.

## 4. Organización del proyecto

```text
Proyecto-DataStock/
├── pom.xml
├── README.md
├── .gitignore
├── datos/
│   ├── ejemplos/              # Ficheros de prueba JSON y XML
│   └── salidas/               # Ficheros generados
├── material_referencia/       # Material de referencia para UT2
└── src/main/java/es/iesisidra/datastock/
    ├── App.java
    ├── modelo/                # Clases del modelo
    ├── servicio/              # Gestión del inventario
    ├── repository/            # Lectura y Escritura de XML con DOM
    ├── ui/                    # Menú principal
    ├── util/                  # Utilidades de consola
    ├── config/                # Configuración XML segura
    └── complementarias/       # XPath y RandomAccessFile
```

Las clases principales son `App`, como punto de entrada; `MenuPrincipal`, que gestiona la navegación; `Inventario`, que mantiene la colección de productos; y las clases complementarias para las actividades XPath y acceso aleatorio.

## 5. Menú principal

La aplicación permite acceder a las siguientes funcionalidades:

```text
1. Explorar ficheros y directorios     RF-FIC-01
2. Importar productos desde JSON       RF-FIC-02
3. Importar productos desde XML        RF-FIC-03
4. Consultar productos                 RF-FIC-04
5. Exportar productos                  RF-FIC-05
6. Guardar y recuperar inventario      RF-FIC-06
0. Salir
```

El submenú de consultas permite listar productos y buscarlos por código o nombre. El de exportación permite generar ficheros JSON o XML, mientras que el de serialización permite guardar y recuperar el inventario mediante un fichero `.dat`.

El menú presenta las opciones y delega las operaciones en las clases correspondientes, manteniendo separada la interfaz de la lógica de negocio.

## 6. Requisitos funcionales

- **RF-FIC-01 – Explorar ficheros y directorios:** permite consultar rutas, ficheros y directorios, gestionando rutas inexistentes o inaccesibles.
- **RF-FIC-02 – Importar JSON:** lee catálogos JSON mediante Gson, valida los productos e incorpora los registros válidos al inventario.
- **RF-FIC-03 – Importar XML:** procesa catálogos XML y aplica las validaciones correspondientes antes de incorporar los productos.
- **RF-FIC-04 – Consultar productos:** permite listar el inventario y buscar productos por código o nombre.
- **RF-FIC-05 – Exportar productos:** genera catálogos JSON o XML con los productos del inventario e informa del resultado de la operación.
- **RF-FIC-06 – Serializar y recuperar:** guarda el inventario en un fichero binario `.dat` y permite recuperarlo posteriormente.
- **RF-FIC-07 – XPath:** ejecuta consultas independientes sobre un catálogo XML para buscar por código, categoría, precio superior a un valor y obtener el número total de productos.
- **RF-FIC-08 – RandomAccessFile:** consulta y modifica registros de un fichero binario mediante acceso aleatorio, sin actualizar la colección del inventario en memoria.

## 7. Modelo de datos y validaciones

La clase `Producto` contiene los campos `codigo`, `nombre`, `precio`, `stock`, `categoriaId` y `proveedorId`.

Las principales reglas de validación son:

- El código es obligatorio, tiene una longitud máxima de 12 caracteres y no puede estar duplicado, sin distinguir entre mayúsculas y minúsculas.
- El nombre es obligatorio y debe tener entre 3 y 100 caracteres.
- El precio debe ser decimal y mayor que cero.
- El stock debe ser igual o superior a cero.
- Los identificadores de categoría y proveedor deben ser enteros positivos.

Si el documento JSON o XML tiene una estructura general inválida, la importación no debe modificar el inventario. Si su estructura es válida, pero contiene productos incorrectos, se incorporan únicamente los productos válidos y se informa de los rechazados.

La importación añade productos al inventario, mientras que la recuperación de un fichero `.dat` sustituye el inventario actual, previa confirmación cuando corresponda.

En UT1 no se utiliza MySQL, por lo que los identificadores de categoría y proveedor no se comprueban contra una base de datos.

## 8. Ficheros de prueba y actividades complementarias

El directorio `datos/ejemplos/` contiene los ficheros utilizados para comprobar el funcionamiento de la aplicación. Se deben conservar ejemplos válidos e inválidos para probar tanto las operaciones correctas como la gestión de errores.

Entre los ficheros utilizados durante las pruebas están:

- `5_productos.json`, para probar la serialización y recuperación de cinco productos.
- `4_productos.json`, para probar el acceso aleatorio con cuatro registros.
- El catálogo XML válido del proyecto, para realizar las consultas XPath.

La actividad XPath utiliza un documento XML y permite ejecutar consultas sin necesidad de importar sus productos al inventario. La actividad `RandomAccessFile` trabaja con el fichero independiente `stock.bin`, cuyos registros ocupan 36 bytes cada uno: 24 bytes para el código, 8 para el precio y 4 para el stock.

Los cambios realizados en `stock.bin` no modifican automáticamente los productos del inventario en memoria.

## 9. Pruebas y documentación

Las pruebas deben registrar, para cada caso de aceptación del enunciado, el identificador y requisito funcional, los datos de entrada, el resultado esperado, el resultado obtenido, el estado final y las incidencias detectadas.

El documento de registro de pruebas debe incluirse en la entrega y reflejar los resultados reales de las ejecuciones. Se recomienda adjuntar capturas de consola y los ficheros generados como evidencias.

Antes de entregar, se debe comprobar que `mvn clean compile` funciona correctamente, que el menú gestiona las entradas inválidas y que las actividades complementarias se pueden ejecutar de forma independiente.

## 10. GitHub y entrega

**Repositorio:** https://github.com/ryaadk/Proyecto-DataStock

El proyecto se mantiene bajo control de versiones mediante Git, utilizando commits con mensajes descriptivos. La entrega debe incluir el código fuente, el proyecto Maven, los ficheros de prueba, el README actualizado y el documento de registro de pruebas.

Antes de entregar, comprobar que:

- La compilación finaliza correctamente.
- El repositorio contiene todos los archivos necesarios.
- No se incluyen carpetas generadas como `target/` ni configuraciones personales del IDE.
- Existe la etiqueta Git solicitada por la profesora, por ejemplo `UT1-v1.0`, apuntando al commit final.
- El registro de pruebas está completo y coincide con las pruebas realmente ejecutadas.

## 11. Referencia

El **enunciado DataStock UT1** facilitado en el aula virtual es la referencia principal para determinar el comportamiento exigido y los casos de aceptación.

El contenido de `material_referencia/`, como el modelo relacional y el script SQL, sirve como preparación para UT2. **No es necesario conectar MySQL durante UT1.**
