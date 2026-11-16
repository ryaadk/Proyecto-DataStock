# **DataStock – Gestión de inventario (UT1 + UT2 + UT3)**

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · 2026–2027  
**Tecnologías:** Java 21 · Maven · Gson · JDBC · MySQL · SQLite (demo)  
**Repositorio:** https://github.com/ryaadk/Proyecto-DataStock

## **1. Descripción**

DataStock es una aplicación de consola para gestionar un inventario de productos.

El proyecto comprende dos unidades de trabajo:

- **UT1 – Ficheros:** gestión del inventario en memoria, JSON, XML, serialización y acceso aleatorio.
- **UT2 – JDBC:** gestión de categorías, proveedores, productos y movimientos de stock en MySQL.

El inventario en memoria y la base de datos son independientes. Los productos se importan
de UT1 a MySQL únicamente cuando el usuario selecciona esa operación.

SQLite se utiliza solo en una actividad complementaria independiente; la aplicación principal sigue utilizando MySQL.

## **2. Requisitos**

- JDK 21.
- Maven.
- Un IDE compatible con Java, como VS Code, IntelliJ IDEA o Eclipse.
- MySQL Server para las funcionalidades de UT2.
- Conexión a Internet para descargar las dependencias de Maven.

Comprobar Java y Maven:

```bash
java -version
mvn -version
```

## **3. Instalación y ejecución**

Clonar el repositorio:

```bash
git clone https://github.com/ryaadk/Proyecto-DataStock.git
cd Proyecto-DataStock
```

Compilar y ejecutar:

```bash
mvn clean compile
mvn exec:java
```

Las actividades complementarias se pueden ejecutar por separado:

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

En PowerShell puede ser necesario poner entre comillas los argumentos `-D`.

## **4. Configuración de MySQL**

Para utilizar UT2:

1. Iniciar MySQL Server.
2. Ejecutar el script `material_referencia/DataStock_BD_MySQL.sql`.
3. Configurar la conexión en `src/main/resources/database.properties`.
4. Comprobar que Maven incluye el controlador MySQL Connector/J.

Ejemplo de `database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/datastock
db.user=TU_USUARIO
db.password=TU_CONTRASENA
```

La clase `ConexionConfig` utiliza estas propiedades para abrir conexiones JDBC.
No se deben subir contraseñas reales a GitHub.

Dependencia JDBC de MySQL en `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.5.0</version>
</dependency>
```

**Nota:** La UT1 y la demostración SQLite pueden ejecutarse sin MySQL.

## **5. Organización del proyecto**

```text
Proyecto-DataStock/
├── pom.xml
├── README.md
├── .gitignore
├── datos/
│   ├── ejemplos/
│   ├── salidas/
│   └── sqlite/                (se crea al ejecutar la demo)
│       └── productos_demo.db
├── material_referencia/
│   └── DataStock_BD_MySQL.sql
└── src/main/
    ├── java/es/iesisidra/datastock/
    │   ├── App.java
    │   ├── componentes/       (UT3: LectorProductosJson y ConsultaProductoJdbc)
    │   ├── modelo/
    │   ├── servicio/
    │   ├── repository/
    │   ├── ui/
    │   ├── util/
    │   ├── config/
    │   └── complementarias/
    │       └── DemoSQLite.java
    └── resources/
        └── database.properties
```

`App` inicia el programa, los menús gestionan la navegación y las clases de servicio
realizan las operaciones sobre ficheros o base de datos.

## **6. Funcionalidades de UT1**

| Requisito | Funcionalidad |
|---|---|
| RF-FIC-01 | Explorar ficheros y directorios. |
| RF-FIC-02 | Importar productos desde JSON con Gson. |
| RF-FIC-03 | Importar productos desde XML. |
| RF-FIC-04 | Listar y buscar productos en memoria. |
| RF-FIC-05 | Exportar productos a JSON y XML. |
| RF-FIC-06 | Guardar y recuperar inventario en un fichero `.dat`. |
| RF-FIC-07 | Realizar consultas XPath sobre XML. |
| RF-FIC-08 | Consultar y modificar registros mediante `RandomAccessFile`. |

Las importaciones validan los productos y notifican los registros rechazados.
La recuperación desde `.dat` sustituye el inventario, previa confirmación cuando corresponde.

## **7. Funcionalidades de UT2**

| Requisito | Funcionalidad |
|---|---|
| RF-JDBC-01 | Conectar a MySQL y consultar información de la conexión. |
| RF-JDBC-02 | CRUD de categorías y proveedores. |
| RF-JDBC-03 | CRUD de productos. |
| RF-JDBC-04 | Consultas con `JOIN`, código, nombre y stock bajo. |
| RF-JDBC-05 | Registrar entradas y salidas de stock. |
| RF-JDBC-06 | Consultar historial completo o por producto. |
| RF-JDBC-07 | Importar productos de memoria a MySQL. |
| RF-JDBC-08 | Demostrar transacciones con `commit` y `rollback`. |

### **Base de datos**

MySQL utiliza cuatro tablas:

| Tabla | Contenido |
|---|---|
| `categoria` | Identificador y nombre de la categoría. |
| `proveedor` | Identificador y datos del proveedor. |
| `producto` | Código, nombre, precio, stock y referencias. |
| `movimiento_stock` | Entradas, salidas e historial de existencias. |

### **Movimientos e integridad**

- Los movimientos se registran mediante el procedimiento `registrar_movimiento`.
- Una entrada aumenta el stock y una salida lo disminuye.
- No se permiten salidas superiores al stock disponible.
- Cada movimiento conserva el stock anterior y posterior.
- El stock no se modifica desde la edición normal de productos.
- **No se puede eliminar un producto con movimientos registrados**, porque la clave
  foránea `fk_mov_producto` protege su historial. MySQL devuelve el error `1451`,
  que la aplicación debe comunicar con un mensaje comprensible.

### **Transacciones**

La importación desde memoria utiliza una transacción JDBC: confirma las inserciones
con `commit()` o las revierte con `rollback()` ante un fallo SQL inesperado.

La demostración de transacciones inserta categorías de prueba y comprueba que una
segunda operación fallida por duplicado no deja inserciones parciales.

## **8. Validaciones y errores**

Los productos deben cumplir estas reglas:

- Código obligatorio, único y de hasta 12 caracteres (`[A-Z0-9-]+`).
- Nombre de 3 a 100 caracteres.
- Precio positivo; en MySQL se almacena como `DECIMAL(10,2)`.
- Stock igual o superior a cero.
- Categoría y proveedor positivos y existentes en MySQL para UT2.

La aplicación valida las entradas, solicita confirmación antes de borrar y muestra
mensajes `[OK]`, `[ERROR]` y `[AVISO]`. Las consultas SQL utilizan sentencias
preparadas y los recursos JDBC se cierran al terminar.

## **9. Comparativa MySQL y SQLite**

| Aspecto | MySQL | SQLite |
|---|---|---|
| ¿Necesita servidor? | Sí, requiere un servidor MySQL. | No, está integrado en la aplicación. |
| ¿Dónde guarda los datos? | En archivos administrados por el servidor. | En un fichero local `.db`. |
| Conexión JDBC | `jdbc:mysql://localhost:3306/datastock` | `jdbc:sqlite:datos/sqlite/productos_demo.db` |
| Dependencia Maven | `com.mysql:mysql-connector-j:9.5.0` | `org.xerial:sqlite-jdbc:3.46.1.0` |
| Ventajas | Multiusuario, escalable y adecuado para servicios en red. | Ligero, sencillo y sin servidor. |
| Inconvenientes | Requiere instalación, configuración y administración. | Tiene limitaciones con muchas escrituras simultáneas. |
| ¿Cuándo elegirlo? | Aplicaciones empresariales, web o multiusuario. | Aplicaciones locales, pequeñas o prototipos. |

### **Demostración SQLite**

`DemoSQLite` es una clase ejecutable independiente de DataStock. Utiliza el controlador
JDBC **`org.xerial:sqlite-jdbc:3.46.1.0`**, añadido al `pom.xml`.

- Crea `datos/sqlite/` y abre `productos_demo.db` mediante `DriverManager`.
- Crea la tabla `producto_demo` con los campos `codigo` y `nombre`.
- Inserta `SQL-001` y `SQL-002` mediante `PreparedStatement` e `INSERT OR IGNORE`.
- Muestra los registros ordenados por código mediante `ResultSet`.
- Usa `try-with-resources` para cerrar los recursos JDBC.
- Si se ejecuta de nuevo, no duplica los registros y el fichero `.db` conserva los datos.

La base de datos de demostración no sustituye las cuatro tablas MySQL ni se integra en los menús.
Se recomienda excluir `datos/sqlite/*.db` en `.gitignore` si contiene resultados personales.

### **Pool de conexiones**

Un **pool de conexiones** mantiene un conjunto de conexiones JDBC disponibles para
reutilizarlas sin abrir una conexión nueva en cada operación.

Resulta útil en aplicaciones con muchos usuarios o consultas frecuentes, como
servidores web. **DataStock es una aplicación de consola**, con operaciones
puntuales y normalmente secuenciales, por lo que puede trabajar sin pool:
abre una conexión cuando la necesita y la cierra correctamente mediante
`try-with-resources` al finalizar.

## **10. Pruebas y entrega**

Las pruebas deben registrar los datos introducidos, el resultado esperado,
el resultado obtenido y las incidencias detectadas.

Antes de entregar, comprobar que:

- `mvn clean compile` termina correctamente.
- Las importaciones, consultas y exportaciones funcionan.
- MySQL contiene las tablas y el procedimiento almacenado.
- Las operaciones CRUD respetan las claves foráneas.
- Los movimientos actualizan el stock y conservan el historial.
- `commit()` y `rollback()` funcionan según los casos de prueba.
- `DemoSQLite` funciona sin MySQL, crea el fichero `.db` y evita duplicados al repetirse.
- No se suben `target/`, credenciales ni configuraciones privadas.
- Se incluye el registro de pruebas y la etiqueta Git de entrega.

## **11. UT3: componentes reutilizables**

En la UT3 se han sacado dos operaciones a componentes reutilizables, dentro del
paquete `es.iesisidra.datastock.componentes`. El resto de la aplicación no cambia.

| Componente | Entrada | Salida | Errores |
|---|---|---|---|
| `LectorProductosJson` | Ruta del fichero (`Path`) y productos ya existentes (solo para detectar duplicados). | `List<Producto>` con los productos válidos. Las incidencias se consultan con `getIncidencias()`. | Lanza `IOException` si el fichero no existe, el JSON está mal formado o no tiene el array `productos`. |
| `ConsultaProductoJdbc` | Código del producto. | `ProductoConsultaDTO` con los datos del producto, o `null` si no existe. | Lanza `SQLException` si falla MySQL o `IllegalStateException` si falta la configuración. |

Ninguno de los dos componentes escribe por consola ni usa `Scanner`. Los menús piden
los datos, llaman al componente y muestran el resultado: `[OK]` en verde, `[ERROR]` en
rojo y `[AVISO]` en amarillo.

**¿Qué ventaja tiene separar la lectura JSON del menú? ¿Qué inconveniente tiene?**
La lectura del fichero ya no depende de la consola, así que se puede reutilizar desde
otra aplicación, desde otro menú o desde una prueba sin tener que escribir nada por
teclado. Además, cada clase tiene una sola responsabilidad y es más fácil encontrar
un fallo. El inconveniente es que hay más clases y más código para pasar los datos de
un sitio a otro (por ejemplo, las incidencias), y para un proyecto pequeño puede
parecer más complicado que tenerlo todo junto.

**¿Por qué conviene separar una consulta JDBC de la presentación por consola?**
Porque así la consulta solo se encarga de abrir la conexión, ejecutar el
`PreparedStatement`, leer el `ResultSet` y cerrar los recursos. Si mañana los datos
se muestran en una ventana o en una web, la consulta sirve igual. También se puede
probar el componente comprobando el objeto devuelto, sin mirar lo que sale por pantalla.

**¿Qué herramientas he utilizado?**

- **IDE (VS Code):** escribir, compilar y depurar el código.
- **Maven:** gestionar las dependencias (Gson y MySQL Connector/J), compilar y ejecutar el proyecto.
- **Javadoc:** documentar las clases y los métodos públicos de los componentes
  (finalidad, parámetros, resultado y errores). Se puede generar con `mvn javadoc:javadoc`.
- **JUnit:** no se ha usado en esta entrega; las pruebas se han hecho de forma manual desde el menú.

**¿Qué dos opciones de DataStock se han modificado?**

- **Menú principal > 2. Importar productos desde JSON:** ahora llama a
  `LectorProductosJson` y añade al inventario los productos que devuelve.
- **7. Gestionar base de datos MySQL > 5. Consultas productos > 2. Buscar producto por código:**
  ahora llama a `ConsultaProductoJdbc` y muestra la ficha o un aviso.

### **Registro de pruebas UT3**

| Caso | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| CP-COMP-01 | Opción 2 con `datos/ejemplos/catalogo_valido.json`. | Se cargan los productos válidos y se muestra `[OK]`. | Encontrados 2, importados 2, sin incidencias. `[OK] Importación finalizada.` en verde. | Superada |
| CP-COMP-02 | Opción 2 con `datos/ejemplos/catalogo_malformado.json` y con `datos/ejemplos/no_existe.json`. | Error controlado en rojo; DataStock sigue funcionando. | `[ERROR] JSON mal formado.` y `[ERROR] El fichero no existe o no es un fichero` en rojo. Vuelve al menú principal. | Superada |
| CP-COMP-03 | Opción 7 > 5 > 2 con `TEC-001` (existe) y `NOEXISTE-99` (no existe). | Se muestra la ficha correcta o un aviso; sin excepciones. | Ficha de `TEC-001` con `[OK] Producto encontrado.`; para `NOEXISTE-99`, `[AVISO] No existe ningún producto con ese código.` | Superada |

Prueba adicional: con `datos/ejemplos/catalogo_con_errores.json` se importa 1 producto y se
rechazan 2 (precio negativo y código duplicado), igual que antes de la refactorización.

## **12. Referencias**

Los enunciados de **DataStock UT1 y UT2** facilitados en el aula virtual
son la referencia principal para los requisitos y casos de aceptación.

- [SQLite: descargas](https://www.sqlite.org/download.html)
- [SQLite: guía de inicio](https://www.sqlite.org/quickstart.html)
- [SQLite JDBC de Xerial](https://github.com/xerial/sqlite-jdbc)