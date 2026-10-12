# Super Inter – Backend

Módulos de **gestión de productos** y **gestión de proveedores** del Sistema de Gestión Integral para el supermercado Super Inter.

**Aprendiz:** Andrés Felipe Zambrano Velasquez
**Programa:** Análisis y Desarrollo de Software (ADSO) – SENA, ficha 3336098

| Evidencia | Contenido | Versión |
|---|---|---|
| GA7-220501096-AA2-EV01 – Codificación de módulos del software | CRUD de productos con JDBC y API REST | `v1.0.0` |
| GA7-220501096-AA2-EV02 – Módulos de software codificados y probados | Módulo web con servlets, formularios HTML y JSP | `v2.0.0` |
| GA7-220501096-AA3-EV01 – Codificación de módulos del software stand-alone, web y móvil | Módulo web de gestión de proveedores | `v3.0.0` |

## Descripción

Aplicación web que implementa el registro, consulta, actualización y eliminación de dos módulos, con acceso a datos por **JDBC**:

| Módulo | Interfaz web (servlets y JSP) | API REST |
|---|---|---|
| Productos | Sí (AA2-EV02) | Sí (AA2-EV01) |
| Proveedores | Sí (AA3-EV01) | No |

```
Navegador (formularios HTML)        Cliente HTTP (curl, frontend)
            │                                   │
       Servlets + JSP               Controladores REST (productos)
            └────────────────┬──────────────────┘
                     Servicios (reglas de negocio)
                             │
                 Repositorios JDBC (PreparedStatement)
                             │
                        PostgreSQL
```

Los servicios no dependen de servlets ni de JSP. La interfaz web actual y una futura API REST o front-end usan la misma lógica de negocio.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 | Configuración, inyección de dependencias y servidor Tomcat embebido |
| Jakarta Servlet (`HttpServlet`) | Controladores del módulo web |
| JSP, Expression Language y JSTL | Vistas del módulo web |
| JDBC (`DataSource`, `PreparedStatement`, `ResultSet`) | Acceso a datos |
| HikariCP | Pool de conexiones JDBC |
| PostgreSQL 16 | Base de datos |
| Maven Wrapper | Construcción del proyecto (empaquetado WAR) |
| JUnit 5 y AssertJ | Pruebas |
| Lombok | Reducción de código repetitivo |
| Git y GitHub | Control de versiones |

## Módulo web de productos (AA2-EV02)

### Pantallas, métodos HTTP y servlets

| Pantalla | Método | URL | Servlet |
|---|---|---|---|
| Listado con búsqueda | GET | `/productos?nombre=...&idCategoria=...` | `ProductoListaServlet` |
| Formulario de registro | GET | `/productos/nuevo` | `ProductoFormularioServlet` |
| Guardar registro | POST | `/productos/nuevo` | `ProductoFormularioServlet` |
| Formulario de edición | GET | `/productos/editar?id=N` | `ProductoFormularioServlet` |
| Guardar edición | POST | `/productos/editar` | `ProductoFormularioServlet` |
| Confirmar eliminación | GET | `/productos/eliminar?id=N` | `ProductoEliminarServlet` |
| Eliminar | POST | `/productos/eliminar` | `ProductoEliminarServlet` |

- **GET** se usa para consultar y mostrar formularios. El formulario de búsqueda usa GET, así que los filtros quedan en la URL.
- **POST** se usa para todo lo que modifica datos: registrar, actualizar y eliminar. Los formularios HTML solo admiten GET y POST, por eso la actualización y la eliminación, que en la API REST usan `PUT` y `DELETE`, aquí van por POST.
- **La eliminación tiene dos pasos**: el GET muestra una confirmación sin borrar nada, y solo el POST elimina. Una petición GET nunca modifica datos.
- **Patrón Post/Redirect/Get**: después de un POST exitoso, el servlet redirige al listado. Recargar la página no reenvía el formulario.

### Servlets

Cada servlet extiende `HttpServlet` e implementa `doGet` y/o `doPost`. Se registran en Tomcat con `ServletRegistrationBean` (`ProductoWebConfig`) en lugar de `@WebServlet`. Con `@WebServlet`, el contenedor crea el servlet con un constructor vacío. Con `ServletRegistrationBean`, Spring lo crea y le inyecta los servicios por constructor, así que los servlets reutilizan la misma lógica de negocio de la API REST.

Los servlets no contienen reglas de negocio: leen los parámetros de la petición, llaman a los servicios, colocan los datos en la petición con `setAttribute` y hacen `forward` a la JSP o `sendRedirect` al listado.

### Elementos de JSP utilizados

| Elemento | Tipo | Ejemplo |
|---|---|---|
| `<%@ page %>` | Directiva | Tipo de contenido y codificación UTF-8 |
| `<%@ taglib %>` | Directiva | Declaración de JSTL (`core`, `fmt`, `functions`) |
| `<jsp:include>` | Acción estándar | Menú lateral, encabezado y pie de página compartidos |
| `<jsp:useBean>` | Acción estándar | Fecha actual para el pie de página |
| `${...}` | Expression Language | `${producto.nombre}`, `${errores.precio}` |
| `<c:forEach>`, `<c:if>`, `<c:choose>`, `<c:out>`, `<c:set>` | JSTL core | Tabla de productos, mensajes, escape de texto |
| `<fmt:formatNumber>`, `<fmt:formatDate>` | JSTL fmt | Precios en pesos colombianos |
| `fn:escapeXml`, `fn:length` | JSTL functions | Escape en atributos y conteo de resultados |

No se usan scriptlets (`<% código Java %>`): la lógica está en los servlets y las JSP solo presentan datos. Todo texto proveniente de datos se escapa (`<c:out>`, `fn:escapeXml`) para prevenir ataques XSS.

Las JSP están en `src/main/webapp/WEB-INF/jsp`, donde no se pueden solicitar directamente desde el navegador: solo se accede a ellas a través de los servlets.

### Validación de formularios

Los datos de un formulario HTML llegan como texto. `ProductoFormulario` los convierte a sus tipos (número, fecha) y registra los errores de formato. Luego se aplican las mismas validaciones de la API REST (`ProductoRequest`) y las reglas de negocio del servicio. Si hay errores, el formulario se muestra de nuevo con lo que escribió el usuario y un mensaje junto a cada campo.

## Módulo de proveedores (AA3-EV01)

Implementa la gestión de proveedores: registro, consulta con búsqueda por nombre, edición y eliminación. Sigue la misma arquitectura y las mismas convenciones del módulo de productos.

### Pantallas, métodos HTTP y servlets

| Pantalla | Método | URL | Servlet |
|---|---|---|---|
| Listado con búsqueda | GET | `/proveedores?nombre=...` | `ProveedorListaServlet` |
| Formulario de registro | GET | `/proveedores/nuevo` | `ProveedorFormularioServlet` |
| Guardar registro | POST | `/proveedores/nuevo` | `ProveedorFormularioServlet` |
| Formulario de edición | GET | `/proveedores/editar?id=N` | `ProveedorFormularioServlet` |
| Guardar edición | POST | `/proveedores/editar` | `ProveedorFormularioServlet` |
| Confirmar eliminación | GET | `/proveedores/eliminar?id=N` | `ProveedorEliminarServlet` |
| Eliminar | POST | `/proveedores/eliminar` | `ProveedorEliminarServlet` |

Se aplican las mismas reglas del módulo web de productos: GET solo consulta, POST modifica, eliminación en dos pasos y patrón Post/Redirect/Get. El mensaje de éxito viaja como un código (`?mensaje=creado`) y el servlet lo traduce al texto, así que no se puede inyectar contenido en la página desde la URL.

### Datos y validaciones

| Campo | Obligatorio | Validación |
|---|---|---|
| Nombre | Sí | Máximo 100 caracteres, no puede estar en blanco, único sin distinguir mayúsculas |
| Contacto | No | Máximo 100 caracteres |
| Teléfono | No | 7 a 20 caracteres: dígitos, espacios, `+`, `-`, `(` y `)` |
| Correo | No | Máximo 254 caracteres, formato de correo |

- `ProveedorRequest` concentra las validaciones (Jakarta Bean Validation). Quita los espacios del nombre y convierte los campos opcionales vacíos en `null`, para que la base de datos no guarde cadenas vacías.
- **Nombre único:** `ProveedorService` lo verifica antes de guardar y la base de datos lo garantiza con el índice `uq_proveedores_nombre` sobre `lower(nombre)`. Si dos registros simultáneos pasan la verificación, el código SQLSTATE `23505` se traduce al mismo mensaje de conflicto.
- **Eliminación de un proveedor con compras:** la llave foránea `fk_compra_proveedor` la impide. El código `23503` se traduce a un mensaje que explica por qué no se puede eliminar.

### Cambios en la base de datos

`database/migrations/002_modulo_proveedores.sql`:

- Agrega la columna `correo VARCHAR(254)`. 254 es la longitud máxima de una dirección de correo (RFC 5321).
- Agrega la restricción `chk_proveedores_nombre`, que rechaza nombres formados solo por espacios.
- Crea el índice único `uq_proveedores_nombre` sobre `lower(nombre)`.
- Otorga a `super_inter_app` los permisos `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre `proveedores` y `USAGE` sobre su secuencia. Los mismos permisos se agregaron a `database/usuario_aplicacion.sql` para instalaciones nuevas.

`database/schema.sql` se regeneró y ya incluye estos cambios.

### Comentarios en el código

Los comentarios explican **por qué** se tomó una decisión, no qué hace cada línea. Por ejemplo:

- La Javadoc de cada clase indica su responsabilidad y el requisito que atiende.
- Los métodos de `ProveedorRepository` documentan su contrato: qué devuelven cuando el registro no existe y qué excepción lanzan.
- En `ProveedorService` se explica el orden de las verificaciones (primero el 404 y luego el nombre duplicado) y el caso de un proveedor eliminado por otro usuario entre la verificación y la actualización.
- La migración 002 explica el uso de un índice sobre `lower(nombre)` en lugar de una restricción `UNIQUE`, y por qué no se otorgan permisos sobre `compras`.

## Uso de JDBC

El proyecto **no usa JPA ni Hibernate**. Todas las operaciones SQL están escritas a mano con la API estándar de JDBC en `JdbcProductoRepository`, `JdbcCategoriaRepository` y `JdbcProveedorRepository`. Spring Boot solo administra el pool de conexiones (`DataSource`).

| Operación | Método | Sentencia | API JDBC |
|---|---|---|---|
| Inserción | `guardar` | `INSERT` | `executeUpdate()` y `getGeneratedKeys()` |
| Consulta | `listarTodos`, `buscar`, `buscarPorId`, `existePorNombre` | `SELECT` | `executeQuery()` y `ResultSet` |
| Actualización | `actualizar` | `UPDATE` | `executeUpdate()` |
| Eliminación | `eliminarPorId` | `DELETE` | `executeUpdate()` |

- `PreparedStatement` con parámetros `?` en todas las consultas, incluida la búsqueda con filtros opcionales, lo que previene la inyección SQL.
- Los campos opcionales se guardan con `setNull(..., Types.VARCHAR)` cuando no tienen valor.
- `try-with-resources` para cerrar `Connection`, `PreparedStatement` y `ResultSet`.
- Los códigos SQLSTATE `23505` (valor duplicado) y `23503` (llave foránea) se traducen a errores de conflicto.

## Trazabilidad con los artefactos del proyecto

### Módulo de productos

| Artefacto | Aplicación |
|---|---|
| RF-01 (registro de productos con código, nombre, categoría, fecha de vencimiento y cantidad) | Formulario web, `Producto` y `ProductoRequest` |
| RF-04 (consultar el inventario disponible) | Listado web y `GET /api/productos` |
| RF-25 (filtrar y buscar información por categoría) | Búsqueda por nombre y filtro por categoría |
| Regla de negocio 1 (el SKU no puede repetirse) | `ProductoService` y restricción `uq_productos_codigo` |
| Modelo lógico (GA4-220501095-AA1-EV01) | Tablas `productos` y `categorias` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Entidades `Producto` y `Categoria` y su asociación |
| Diseño front-end (GA6-220501096-AA4-EV03) | Hojas de estilo, paleta de colores y estructura de las pantallas de productos |
| Entorno operativo: aplicación web cliente-servidor | Servlets y JSP sobre Tomcat |

### Módulo de proveedores

| Artefacto | Aplicación |
|---|---|
| RF-04 (gestionar proveedores: crear, editar y consultar) | Listado, formulario de registro y de edición, `ProveedorService` |
| RF-15 (información de contacto del proveedor) | Campos contacto, teléfono y correo |
| Modelo lógico (GA4-220501095-AA1-EV01) | Tabla `proveedores` y su relación con `compras` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Entidad `Proveedor` |
| Historia de usuario HU2 | Depende de que existan proveedores registrados |
| Diseño front-end (GA6-220501096-AA4-EV03) | Estructura de la pantalla `suppliers/list.html`, estilos y opción del menú |

### Ajustes justificados

- **Columna `codigo`:** RF-01 y la regla de negocio 1 exigen un código único que el modelo lógico no incluía. Se agregó con restricción `UNIQUE` (`database/migrations/001_ajustes_modulo_productos.sql`).
- **Precio como `NUMERIC(12,2)` / `BigDecimal`:** el diagrama de clases usa `double`, que produce errores de redondeo con valores monetarios.
- **Campos del prototipo de front-end:** el prototipo incluía descripción, proveedor, unidad, costo, stock mínimo y estado, que no existen en el modelo lógico. Las pantallas web conservan el diseño del prototipo con los campos del modelo de datos, y agregan código y fecha de vencimiento (RF-01).
- **Menú, encabezado y pie:** en el prototipo se generaban con JavaScript. En el módulo web se generan en el servidor con `<jsp:include>`. El menú muestra solo los módulos implementados.
- **Correo del proveedor:** no estaba en el modelo lógico. Se agregó como campo opcional para completar la información de contacto (RF-15).
- **Nombre de proveedor único:** el modelo lógico no lo exigía. Se agregó para evitar registros duplicados del mismo proveedor, sin distinguir mayúsculas.
- **Eliminación de proveedores:** RF-04 y el diagrama de clases solo indican crear, editar y consultar. Se agregó la eliminación para completar el CRUD; la base de datos impide eliminar un proveedor con compras registradas.
- **NIT y estado del prototipo:** la pantalla de proveedores del prototipo los mostraba, pero no existen en el modelo lógico. No se incluyeron.

## Estructura del proyecto

```
src/main/java/com/superinter/
├── SuperInterApplication.java
├── common/
│   ├── exception/           Excepciones de la aplicación y manejador de la API REST
│   └── web/                 Utilidades comunes de los servlets
├── categoria/               Módulo de categorías (solo lectura)
│   ├── domain/  repository/  dto/  service/  controller/
├── producto/                Módulo de productos
│   ├── domain/  repository/  dto/  service/
│   ├── controller/          API REST (AA2-EV01)
│   └── web/                 Servlets y modelo del formulario (AA2-EV02)
└── proveedor/               Módulo de proveedores (AA3-EV01)
    ├── domain/  repository/  dto/  service/
    └── web/                 Servlets

src/main/webapp/WEB-INF/jsp/
├── fragmentos/              head, sidebar, header y footer compartidos
├── productos/               lista, formulario y confirmar-eliminacion
└── proveedores/             lista, formulario y confirmar-eliminacion

src/main/resources/static/
├── css/                     Estilos del prototipo de front-end
└── js/menu.js               Menú lateral en pantallas pequeñas

database/
├── schema.sql               Estructura completa de la base de datos
├── data.sql                 Categorías iniciales
├── usuario_aplicacion.sql   Usuario de la aplicación con mínimo privilegio
└── migrations/              Historial de cambios sobre la base de datos
```

## Convenciones de codificación

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases | PascalCase | `ProductoFormularioServlet` |
| Métodos y variables | camelCase | `buscarPorId`, `idProducto` |
| Constantes | UPPER_SNAKE_CASE | `RUTA_NUEVO`, `SQL_BUSCAR_POR_ID` |
| Paquetes | minúsculas, por módulo y capa | `com.superinter.producto.web` |
| JSP | minúsculas con guiones | `confirmar-eliminacion.jsp` |

Los conceptos del negocio se nombran en español (`Producto`, `Categoria`, `Proveedor`) y el rol técnico en inglés como sufijo (`Repository`, `Service`, `Controller`, `Servlet`, `Request`, `Response`, `Exception`).

Cada módulo se organiza por capas (`domain`, `repository`, `dto`, `service`, `web`). Un módulo usa el servicio de otro módulo, nunca su repositorio. Las dependencias se inyectan por constructor.

## Control de versiones

- Los commits siguen **Conventional Commits** en español (`feat(proveedor): ...`, `test(proveedor): ...`, `fix(proveedor): ...`).
- Cada funcionalidad se desarrolla en una rama (`feat/modulo-proveedores`) y se integra a `main` mediante un Pull Request con merge commit.
- Cada evidencia entregada se marca con una etiqueta de versión (`v1.0.0`, `v2.0.0`, `v3.0.0`).
- Los cambios en la base de datos se registran como migraciones numeradas. Una migración aplicada no se modifica.
- El archivo `.env` con las credenciales está excluido del repositorio; `.env.example` sirve de plantilla.

## Requisitos

- JDK 21 o superior
- PostgreSQL 16 o superior, con `psql` 16.10 o superior (el `schema.sql` usa `\restrict`)
- Git

No es necesario instalar Maven: el proyecto incluye Maven Wrapper (`mvnw`).

## Instalación

### 1. Base de datos

Como usuario `postgres`:

```bash
psql -U postgres -c "CREATE DATABASE \"super-inter\" ENCODING 'UTF8';"
psql -U postgres -d super-inter -f database/schema.sql
psql -U postgres -d super-inter -f database/data.sql
```

Usuario de la aplicación (la contraseña se ingresa sin mostrarse en pantalla):

```bash
read -s -p "Contraseña para super_inter_app: " APP_PW; echo
psql -U postgres -d super-inter -v app_password="$APP_PW" -f database/usuario_aplicacion.sql
unset APP_PW
```

> Use solo caracteres ASCII en la contraseña.

### 2. Variables de entorno

```bash
cp .env.example .env
```

```properties
DB_URL=jdbc:postgresql://localhost:5432/super-inter
DB_USERNAME=super_inter_app
DB_PASSWORD=la_contrasena_definida
```

El archivo `.env` está excluido del repositorio.

### 3. Ejecución

```bash
./mvnw spring-boot:run
```

- **Productos:** http://localhost:8080/productos
- **Proveedores:** http://localhost:8080/proveedores
- **API REST:** http://localhost:8080/api/productos

## API REST de productos (AA2-EV01)

| Método | Ruta | Operación | Respuesta |
|---|---|---|---|
| `POST` | `/api/productos` | Insertar producto | `201 Created` |
| `GET` | `/api/productos` | Consultar todos | `200 OK` |
| `GET` | `/api/productos/{id}` | Consultar por id | `200 OK` / `404` |
| `PUT` | `/api/productos/{id}` | Actualizar | `200 OK` / `404` / `409` |
| `DELETE` | `/api/productos/{id}` | Eliminar | `204 No Content` / `404` / `409` |
| `GET` | `/api/categorias` | Consultar categorías | `200 OK` |

Los errores se responden en formato `ProblemDetail` (RFC 9457).

```bash
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"codigo":"7701234567890","nombre":"Arroz Diana 1kg","precio":4500.00,"stock":20,"fechaVencimiento":"2027-03-15","idCategoria":1}'
```

## Pruebas

```bash
./mvnw clean test
```

104 pruebas automatizadas, 47 de ellas del módulo de proveedores:

| Tipo | Qué verifica | Requiere BD |
|---|---|---|
| Unitarias de servlets | GET y POST de los servlets de productos y proveedores con `MockHttpServletRequest`: vistas, redirecciones, códigos HTTP, errores de validación y de negocio | No |
| Integración web | La aplicación completa con Tomcat: generación de las JSP y registro desde el formulario hasta PostgreSQL | Sí |
| Unitarias de servicios y validaciones | Reglas de negocio, validaciones de la solicitud y manejo de errores de la API. Los servicios se prueban con repositorios en memoria | No |
| Integración de repositorios | Operaciones JDBC contra PostgreSQL, incluidas las restricciones de nombre único | Sí |

Los datos de prueba usan el prefijo `TEST-` y se eliminan al terminar.

## Repositorio

https://github.com/andresfzv-dev/Super-Inter-Sena