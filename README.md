# **DataStock – Gestión de inventario (UT1 + UT2)**

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

## **11. Referencias**

Los enunciados de **DataStock UT1 y UT2** facilitados en el aula virtual
son la referencia principal para los requisitos y casos de aceptación.

- [SQLite: descargas](https://www.sqlite.org/download.html)
- [SQLite: guía de inicio](https://www.sqlite.org/quickstart.html)
- [SQLite JDBC de Xerial](https://github.com/xerial/sqlite-jdbc)