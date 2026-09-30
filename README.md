# Super Inter – Backend

Módulo de **gestión de productos** del Sistema de Gestión Integral para el supermercado Super Inter.

**Aprendiz:** Andrés Felipe Zambrano Velasquez
**Programa:** Análisis y Desarrollo de Software (ADSO) – SENA, ficha 3336098

| Evidencia | Contenido | Versión |
|---|---|---|
| GA7-220501096-AA2-EV01 – Codificación de módulos del software | CRUD de productos con JDBC y API REST | `v1.0.0` |
| GA7-220501096-AA2-EV02 – Módulos de software codificados y probados | Módulo web con servlets, formularios HTML y JSP | `v2.0.0` |

## Descripción

Aplicación que implementa la inserción, consulta, actualización y eliminación de productos del inventario, con dos interfaces que comparten la misma lógica de negocio y el mismo acceso a datos por **JDBC**:

- **Interfaz web** con servlets, formularios HTML y JSP (EV02).
- **API REST** en formato JSON (EV01).

```
Navegador (formularios HTML)        Cliente HTTP (curl, frontend)
            │                                   │
   Servlets + JSP (EV02)            Controladores REST (EV01)
            └────────────────┬──────────────────┘
                     Servicios (reglas de negocio)
                             │
                 Repositorios JDBC (PreparedStatement)
                             │
                        PostgreSQL
```

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

## Módulo web (EV02)

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

## Uso de JDBC

El proyecto **no usa JPA ni Hibernate**. Todas las operaciones SQL están escritas a mano con la API estándar de JDBC en `JdbcProductoRepository` y `JdbcCategoriaRepository`. Spring Boot solo administra el pool de conexiones (`DataSource`).

| Operación | Método | Sentencia | API JDBC |
|---|---|---|---|
| Inserción | `guardar` | `INSERT` | `executeUpdate()` y `getGeneratedKeys()` |
| Consulta | `listarTodos`, `buscar`, `buscarPorId` | `SELECT ... JOIN` | `executeQuery()` y `ResultSet` |
| Actualización | `actualizar` | `UPDATE` | `executeUpdate()` |
| Eliminación | `eliminarPorId` | `DELETE` | `executeUpdate()` |

- `PreparedStatement` con parámetros `?` en todas las consultas, incluida la búsqueda con filtros opcionales, lo que previene la inyección SQL.
- `try-with-resources` para cerrar `Connection`, `PreparedStatement` y `ResultSet`.
- Los códigos SQLSTATE `23505` (valor duplicado) y `23503` (llave foránea) se traducen a errores de conflicto.

## Trazabilidad con los artefactos del proyecto

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

### Ajustes justificados

- **Columna `codigo`:** RF-01 y la regla de negocio 1 exigen un código único que el modelo lógico no incluía. Se agregó con restricción `UNIQUE` (`database/migrations/001_ajustes_modulo_productos.sql`).
- **Precio como `NUMERIC(12,2)` / `BigDecimal`:** el diagrama de clases usa `double`, que produce errores de redondeo con valores monetarios.
- **Campos del prototipo de front-end:** el prototipo incluía descripción, proveedor, unidad, costo, stock mínimo y estado, que no existen en el modelo lógico. Las pantallas web conservan el diseño del prototipo con los campos del modelo de datos, y agregan código y fecha de vencimiento (RF-01).
- **Menú, encabezado y pie:** en el prototipo se generaban con JavaScript. En el módulo web se generan en el servidor con `<jsp:include>`. El menú muestra solo los módulos implementados.

## Estructura del proyecto

```
src/main/java/com/superinter/
├── SuperInterApplication.java
├── common/
│   ├── exception/           Excepciones de la aplicación y manejador de la API REST
│   └── web/                 Utilidades comunes de los servlets
├── categoria/               Módulo de categorías (solo lectura)
│   ├── domain/  repository/  dto/  service/  controller/
└── producto/                Módulo de productos
    ├── domain/  repository/  dto/  service/
    ├── controller/          API REST (EV01)
    └── web/                 Servlets y modelo del formulario (EV02)

src/main/webapp/WEB-INF/jsp/
├── fragmentos/              head, sidebar, header y footer compartidos
└── productos/               lista, formulario y confirmar-eliminacion

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

Los conceptos del negocio se nombran en español (`Producto`, `Categoria`) y el rol técnico en inglés como sufijo (`Repository`, `Service`, `Controller`, `Servlet`, `Request`, `Response`, `Exception`).

Los commits siguen **Conventional Commits** y cada funcionalidad se integró a `main` mediante una rama y un Pull Request.

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

- **Interfaz web:** http://localhost:8080/productos
- **API REST:** http://localhost:8080/api/productos

## API REST (EV01)

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

57 pruebas automatizadas:

| Tipo | Qué verifica | Requiere BD |
|---|---|---|
| Unitarias de servlets | GET y POST de los tres servlets con `MockHttpServletRequest`: vistas, redirecciones, códigos HTTP, errores de validación y de negocio | No |
| Integración web | La aplicación completa con Tomcat: generación de las JSP y registro desde el formulario hasta PostgreSQL | Sí |
| Unitarias de servicios y validaciones | Reglas de negocio, validaciones de la solicitud y manejo de errores de la API | No |
| Integración de repositorios | Operaciones JDBC contra PostgreSQL | Sí |

Los datos de prueba usan el prefijo `TEST-` y se eliminan al terminar.

## Repositorio

https://github.com/andresfzv-dev/Super-Inter-Sena