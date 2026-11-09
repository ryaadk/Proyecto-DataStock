# DataStock – Gestión de inventario (UT1 + UT2)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Tecnologías:** Java 21 · Maven · Gson · JDBC · MySQL · Aplicación de consola

## 1. Descripción

**DataStock** es un proyecto individual de gestión de inventario desarrollado en Java. Integra dos formas de trabajar con productos:

- **UT1 – Ficheros y memoria:** lectura, validación, consulta, exportación y recuperación de productos mediante JSON, XML y ficheros binarios.
- **UT2 – JDBC y MySQL:** acceso a una base de datos relacional para gestionar categorías, proveedores y productos; consultar datos, registrar movimientos de stock y ejecutar transacciones.

La aplicación mantiene separado el **inventario en memoria** de los productos almacenados en **MySQL**. La importación de UT1 a MySQL es una operación explícita: los datos no se sincronizan automáticamente. La UT3, relativa a componentes reutilizables, queda fuera del alcance documentado aquí.

## 2. Requisitos previos

- **JDK 21** instalado.
- **Apache Maven** disponible desde la terminal.
- **MySQL Server** accesible para las funciones de UT2 y permisos para crear el esquema y ejecutar el procedimiento almacenado.
- Un IDE compatible con Maven, como VS Code, IntelliJ IDEA o Eclipse.
- Conexión a Internet la primera vez que Maven descargue las dependencias.

Comprobar el entorno:

```bash
java -version
mvn -version
```

La **UT1 puede usarse sin MySQL**. Para UT2, es necesario disponer del controlador **MySQL Connector/J** como dependencia de Maven en `pom.xml`, además de Gson para las operaciones JSON.

## 3. Instalación y ejecución

Clonar el repositorio y entrar en su directorio:

```bash
git clone https://github.com/ryaadk/Proyecto-DataStock.git
cd Proyecto-DataStock
```

Compilar y ejecutar desde el directorio que contiene `pom.xml`:

```bash
mvn clean compile
mvn exec:java
```

La clase de entrada del proyecto es `es.iesisidra.datastock.App`.

Las actividades complementarias pueden ejecutarse de forma independiente:

**XPath (RF-FIC-07):**

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
```

**Acceso aleatorio (RF-FIC-08):**

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
```

En PowerShell, si se pasan parámetros `-D` de Maven y aparecen problemas de interpretación, pueden escribirse entre comillas, por ejemplo `"-Dexec.mainClass=..."`.

### Codificación de caracteres

El código fuente y los ficheros de configuración deben guardarse como **UTF-8**. Si aparecen símbolos extraños al escribir tildes o `€`, comprobar que **la entrada de la terminal, la salida de Java y la codificación de los ficheros coincidan**. Cambiar únicamente `chcp` puede empeorar la visualización si el resto del entorno utiliza otra codificación.

## 4. Configuración de MySQL

### 4.1. Crear la base de datos

El script suministrado con el proyecto se encuentra en:

```text
material_referencia/DataStock_BD_MySQL.sql
```

Ejecutarlo en MySQL Workbench o desde el cliente de MySQL, por ejemplo:

```bash
mysql -u root -p < material_referencia/DataStock_BD_MySQL.sql
```

El script define la base de datos `datastock`, las tablas `categoria`, `proveedor`, `producto` y `movimiento_stock`, sus restricciones y el procedimiento almacenado `registrar_movimiento` utilizado para modificar el stock. Revisar el contenido del script antes de ejecutarlo sobre una base de datos existente.

### 4.2. Configurar la conexión JDBC

Crear el archivo `src/main/resources/database.properties` en la máquina donde se ejecutará el proyecto:

```properties
db.url=jdbc:mysql://localhost:3306/datastock
db.user=TU_USUARIO
db.password=TU_CONTRASENA
```

La clase `ConexionConfig` carga las propiedades desde el **classpath** y abre la conexión mediante `DriverManager`. Asegurarse de que MySQL esté iniciado y de que el usuario tenga permisos suficientes.

**Seguridad:** no publicar contraseñas reales ni subir `database.properties` con credenciales al repositorio. Mantenerlo fuera del control de versiones y documentar los valores mediante un ejemplo sin secretos, si se necesita.

### 4.3. Dependencia JDBC

El `pom.xml` debe incluir MySQL Connector/J. Si todavía no está configurado:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.5.0</version>
</dependency>
```

Después, volver a compilar con Maven. No es necesario añadir manualmente el JAR al classpath cuando se ejecuta mediante Maven con las dependencias correctamente declaradas.

## 5. Organización del proyecto

Estructura principal:

```text
Proyecto-DataStock/
├── pom.xml
├── README.md
├── .gitignore
├── datos/
│   ├── ejemplos/                 # Ficheros de prueba JSON/XML
│   └── salidas/                  # Resultados exportados
├── material_referencia/
│   ├── DataStock_BD_MySQL.sql    # Esquema y procedimiento almacenado
│   └── DataStock_Diagrama_ER_CrowFoot.png
└── src/main/
    ├── java/es/iesisidra/datastock/
    │   ├── App.java              # Punto de entrada
    │   ├── modelo/               # Producto y clases del modelo
    │   ├── servicio/             # Inventario, gestores y operaciones
    │   ├── repository/           # Acceso a XML
    │   ├── ui/                   # Menús de consola
    │   ├── util/                 # Entrada, validación y colores
    │   ├── config/               # Configuración de XML/JDBC
    │   └── complementarias/      # XPath y RandomAccessFile
    └── resources/
        └── database.properties   # Configuración local (no subir secretos)
```

Entre las clases relevantes se encuentran `App`, `MenuPrincipal`, `Inventario`, `Producto`, `GestorProductos`, `GestorCategorias`, `GestorProveedores`, `GestorBD` y `ConexionConfig`. Las operaciones de movimientos e historial se integran en el módulo de servicio.

## 6. Menús y navegación

### Menú principal

```text
1. Explorar ficheros y directorios
2. Importar productos desde JSON
3. Importar productos desde XML
4. Consultar productos en memoria
5. Exportar productos
6. Guardar y recuperar inventario
7. Gestionar base de datos MySQL
0. Salir
```

### Gestión MySQL

```text
1. Comprobar conexión y mostrar información de la BD
2. Gestionar categorías
3. Gestionar proveedores
4. Gestionar productos (CRUD)
5. Registrar entrada o salida de stock
6. Consultar historial de movimientos
7. Importar productos de memoria a MySQL
8. Demostración de transacción con commit/rollback
0. Volver al menú principal
```

Los submenús de categorías, proveedores y productos permiten realizar operaciones CRUD. Las consultas y el historial pueden filtrarse por producto según el requisito correspondiente. Las operaciones de borrado requieren confirmación y se respeta la integridad referencial.

## 7. UT1 – Requisitos de ficheros

| Requisito | Funcionalidad |
|---|---|
| **RF-FIC-01** | Explorar ficheros y directorios; validar rutas y accesos. |
| **RF-FIC-02** | Importar productos JSON con Gson y registrar incidencias. |
| **RF-FIC-03** | Importar XML mediante DOM y configuración segura. |
| **RF-FIC-04** | Listar y buscar productos por código o fragmento de nombre. |
| **RF-FIC-05** | Exportar inventario a JSON o XML. |
| **RF-FIC-06** | Guardar y recuperar el inventario serializado en `.dat`. |
| **RF-FIC-07** | Consultas XPath sobre catálogo XML. |
| **RF-FIC-08** | Leer y modificar registros de `stock.bin` mediante `RandomAccessFile`. |

**Importación:** los productos válidos se incorporan a la lista en memoria y los inválidos se notifican; si la estructura general del fichero es incorrecta no se debe modificar el inventario. **Recuperación `.dat`:** sustituye el inventario actual, con confirmación cuando procede. **Acceso aleatorio:** modifica su fichero independiente, no la lista de productos en memoria.

## 8. UT2 – Requisitos JDBC y MySQL

| Requisito | Funcionalidad |
|---|---|
| **RF-JDBC-01** | Conectar a MySQL mediante `ConexionConfig` y comprobar metadatos y acceso al esquema. |
| **RF-JDBC-02** | Altas, consultas, modificaciones y bajas de categorías y proveedores con `PreparedStatement`. |
| **RF-JDBC-03** | CRUD de productos en MySQL, validando datos y claves foráneas. |
| **RF-JDBC-04** | Consultas con `ResultSet`, `JOIN`, búsqueda por código, `LIKE` y stock inferior a un umbral. |
| **RF-JDBC-05** | Registrar entradas y salidas con `CallableStatement` y `registrar_movimiento`. |
| **RF-JDBC-06** | Consultar el historial completo o por código, ordenado por fecha e ID. |
| **RF-JDBC-07** | Importar explícitamente productos válidos de UT1 a MySQL dentro de una transacción JDBC. |
| **RF-JDBC-08** | Demostrar `setAutoCommit(false)`, `commit()` y `rollback()` con categorías de prueba. |

### 8.1. Operaciones sobre productos

Los datos de productos se consultan con consultas preparadas; la visualización mediante `JOIN` puede recuperar los nombres de la categoría y el proveedor. La modificación normal del producto permite cambiar nombre, precio, categoría y proveedor, **pero no el stock**. MySQL impide borrar productos que tengan movimientos históricos asociados.

### 8.2. Movimientos de stock

El stock se modifica exclusivamente mediante el procedimiento `registrar_movimiento`, invocado desde Java con:

```java
CallableStatement sentencia = conexion.prepareCall("{CALL registrar_movimiento(?, ?, ?)}");
```

El procedimiento valida el producto, el tipo (`ENTRADA` o `SALIDA`), una cantidad positiva y la disponibilidad de unidades. Usa su **propia transacción** con `SELECT ... FOR UPDATE`, actualización de stock, registro de movimiento y `COMMIT`/`ROLLBACK`. Java no debe abrir una transacción exterior para esta llamada.

Ejemplo:

```text
Producto TEC-001: stock inicial 8
ENTRADA de 5:  8 -> 13
SALIDA de 4:  13 -> 9
SALIDA de 20: error por stock insuficiente; permanece en 9
```

Cada movimiento guarda el **stock anterior y posterior en el momento de la operación**. El historial no se recalcula utilizando el stock actual.

### 8.3. Importación de memoria a MySQL

La importación es voluntaria desde el menú. Se validan previamente formatos, códigos duplicados, categorías y proveedores. El programa informa de **insertados**, **omitidos** por duplicado y **rechazados** con sus motivos. Los rechazados no se insertan y no impiden que se procesen los válidos.

El lote se ejecuta con `setAutoCommit(false)`: ante un fallo SQL inesperado se realiza `rollback` de las inserciones pendientes; en caso contrario se realiza `commit`. **El inventario UT1 no cambia** por importar a MySQL.

### 8.4. Demostración de transacciones JDBC

La demostración realiza dos inserciones de categorías de prueba y las confirma con `commit`. En una segunda transacción intenta insertar un ID duplicado, provocando el error y el `rollback` completo de ese intento. Se restaura el estado original de `autoCommit` al finalizar.

Una propuesta de IDs de prueba es `900001`, `900002` y `900003`, **solo si están libres**. Las pruebas deben realizarse en un entorno de desarrollo, sin alterar categorías vinculadas a productos reales. Para limpiar los datos confirmados, verificar antes su identidad y que no estén referenciados:

```sql
DELETE FROM categoria
WHERE (id = 900001 AND nombre = 'Prueba Commit 1')
   OR (id = 900002 AND nombre = 'Prueba Commit 2');
```

El `rollback` JDBC de este ejercicio es independiente del que ejecuta internamente el procedimiento almacenado de movimientos.

## 9. Modelo de datos y validaciones

### Producto

| Campo | Tipo Java | Regla principal |
|---|---|---|
| `codigo` | `String` | Obligatorio, máximo 12 caracteres, patrón `[A-Z0-9-]+`, único. |
| `nombre` | `String` | Entre 3 y 100 caracteres. |
| `precio` | `BigDecimal` | Mayor que cero; en MySQL `DECIMAL(10,2)`. |
| `stock` | `int` | Mayor o igual que cero. |
| `categoriaId` | `int` | Positivo; debe existir en MySQL para UT2. |
| `proveedorId` | `int` | Positivo; debe existir en MySQL para UT2. |

El método `validarProducto()` permite compartir las reglas generales entre operaciones de memoria y JDBC. La base de datos añade restricciones propias: claves primarias, claves foráneas, campos únicos y comprobaciones `CHECK`.

### Modelo relacional

- **`categoria`**: identificador y nombre único.
- **`proveedor`**: identificador, nombre y correo electrónico opcional/único.
- **`producto`**: código, nombre, precio, stock y referencias a categoría y proveedor.
- **`movimiento_stock`**: ID, producto, fecha, tipo, cantidad, stock anterior y stock posterior.

El esquema y su diagrama están disponibles en `material_referencia/`.

## 10. Gestión de errores y seguridad

- Todas las entradas deben validarse antes de utilizarlas.
- Los parámetros del usuario se transmiten mediante `PreparedStatement` o `CallableStatement`, sin concatenarlos directamente en SQL.
- Las conexiones, sentencias y resultados se cierran con `try-with-resources`.
- Los errores de integridad, como códigos duplicados o referencias inexistentes, deben mostrarse con mensajes legibles.
- Los borrados solicitan confirmación antes de ejecutarse.
- Las lecturas de historial y consultas no alteran datos.
- La lectura XML utiliza configuración segura para evitar entidades externas (XXE).
- Las credenciales de MySQL deben permanecer fuera de Git.

## 11. Pruebas de aceptación

El registro de pruebas debe recoger **datos introducidos, resultado esperado, resultado real, estado e incidencias**. No se consideran aprobadas las pruebas hasta ejecutarlas y documentar su resultado.

### UT2 – Casos de aceptación

| Caso | Comprobación |
|---|---|
| CP-JDBC-01 a 04 | Conexión y gestión de categorías/proveedores según el enunciado. |
| **CP-JDBC-05** | Insertar y consultar un producto correcto. |
| **CP-JDBC-06** | Modificar nombre y precio. |
| **CP-JDBC-07** | Detectar código duplicado y categoría inexistente. |
| **CP-JDBC-08** | Eliminar producto sin historial e impedir eliminación con movimientos. |
| **CP-JDBC-09** | Listado mediante `JOIN`, ordenado por código. |
| **CP-JDBC-10** | Búsqueda parcial y consulta sin resultados. |
| **CP-JDBC-11** | Productos con stock inferior al umbral indicado. |
| **CP-JDBC-12** | Entrada de 5 unidades y comprobación de producto e historial. |
| **CP-JDBC-13** | Salida válida y cálculo correcto del stock. |
| **CP-JDBC-14** | Salida superior al disponible sin modificar ni stock ni historial. |
| **CP-JDBC-15** | Historial completo y filtrado, ordenado por fecha e ID. |
| **CP-JDBC-16** | Producto existente sin movimientos. |
| **CP-JDBC-17** | Importación mixta: válidos, duplicados y referencias inexistentes. |
| **CP-JDBC-18** | Fallo SQL forzado con `rollback` de todo el lote. |
| **CP-JDBC-19** | Dos categorías de prueba insertadas con `commit`. |
| **CP-JDBC-20** | Inserción duplicada y `rollback` sin inserciones parciales. |

Para UT1 deben conservarse también las evidencias de los casos RF-FIC-01 a RF-FIC-08, incluidos los ejemplos de ficheros válidos e inválidos, la serialización y las actividades complementarias.

## 12. Git, entrega y comprobaciones finales

**Repositorio:** https://github.com/ryaadk/Proyecto-DataStock

Antes de entregar, comprobar lo siguiente:

- `mvn clean compile` finaliza sin errores.
- Las dependencias Maven incluyen Gson y MySQL Connector/J.
- El archivo `database.properties` existe localmente y no publica contraseñas.
- El script SQL crea las tablas y el procedimiento almacenado requerido.
- Las operaciones CRUD y los submenús están conectados a sus métodos reales.
- Los filtros funcionan sin modificar registros.
- Los movimientos modifican stock e historial de manera atómica.
- La importación JDBC realiza correctamente `commit` o `rollback`.
- El registro de pruebas refleja ejecuciones reales.
- No se suben archivos generados (`target/`) ni configuraciones privadas del IDE.
- Se crea la etiqueta de entrega que indique el enunciado, por ejemplo `UT2-v1.0`, sobre el commit definitivo.

## 13. Referencias

Los enunciados **DataStock UT1** y **DataStock UT2** facilitados en el aula virtual son la referencia principal para los requisitos y los casos de aceptación. El script y el diagrama relacional se encuentran en `material_referencia/`.
