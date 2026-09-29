# Super Inter – Backend

Módulo de **gestión de productos** del Sistema de Gestión Integral para el supermercado Super Inter.

**Evidencia:** GA7-220501096-AA2-EV01 – Codificación de módulos del software
**Aprendiz:** Andrés Felipe Zambrano Velasquez
**Programa:** Análisis y Desarrollo de Software (ADSO) – SENA, ficha 3336098

## Descripción

API REST que implementa la inserción, consulta, actualización y eliminación de productos del inventario, con conexión a PostgreSQL mediante **JDBC**. Incluye la consulta de categorías, necesaria para clasificar los productos.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 | Configuración, inyección de dependencias y API REST |
| JDBC (`DataSource`, `PreparedStatement`, `ResultSet`) | Acceso a datos |
| HikariCP | Pool de conexiones JDBC |
| PostgreSQL 16 | Base de datos |
| Maven Wrapper | Construcción del proyecto |
| JUnit 5 y AssertJ | Pruebas |
| Lombok | Reducción de código repetitivo |
| Git y GitHub | Control de versiones |

## Uso de JDBC

El proyecto **no usa JPA ni Hibernate**. Todas las operaciones SQL están escritas a mano con la API estándar de JDBC en las clases `JdbcProductoRepository` y `JdbcCategoriaRepository`. Spring Boot solo administra el pool de conexiones (`DataSource`).

| Operación | Método | Sentencia | API JDBC |
|---|---|---|---|
| Inserción | `guardar` | `INSERT` | `executeUpdate()` y `getGeneratedKeys()` |
| Consulta | `listarTodos`, `buscarPorId` | `SELECT ... JOIN` | `executeQuery()` y `ResultSet` |
| Actualización | `actualizar` | `UPDATE` | `executeUpdate()` |
| Eliminación | `eliminarPorId` | `DELETE` | `executeUpdate()` |

Prácticas aplicadas:

- `PreparedStatement` con parámetros `?` en todas las consultas, lo que previene la inyección SQL.
- `try-with-resources` para cerrar `Connection`, `PreparedStatement` y `ResultSet`, incluso ante errores.
- Traducción de `SQLException` a excepciones de la aplicación. Los códigos SQLSTATE `23505` (valor duplicado) y `23503` (llave foránea) se convierten en respuestas HTTP 409.

## Trazabilidad con los artefactos del proyecto

| Artefacto | Aplicación en el código |
|---|---|
| RF-01 (registro de productos con código, nombre, categoría, fecha de vencimiento y cantidad) | Campos de `Producto` y `ProductoRequest` |
| RF-04 (consultar el inventario disponible) | `GET /api/productos` |
| Regla de negocio 1 (el SKU no puede repetirse) | Validación en `ProductoService` y restricción `uq_productos_codigo` |
| Modelo lógico (GA4-220501095-AA1-EV01) | Tablas `productos` y `categorias` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Entidades `Producto` y `Categoria` y su asociación |
| Base de datos relacional SQL (documento de requerimientos) | PostgreSQL |
| Interfaces de comunicación: API REST (documento de requerimientos) | Controladores REST |

Ajustes justificados respecto a los artefactos:

- **Columna `codigo`:** RF-01 y la regla de negocio 1 exigen un código único, pero el modelo lógico no lo incluía. Se agregó con restricción `UNIQUE` (`database/migrations/001_ajustes_modulo_productos.sql`).
- **Precio como `NUMERIC(12,2)` / `BigDecimal`:** el diagrama de clases usa `double`, que produce errores de redondeo con valores monetarios.
- **Nombres de columnas unificados** con el patrón `id_<tabla>` (`id_categoria`, `id_usuario`, `id_compra`) y `contrasena` sin ñ.

## Estructura del proyecto

```
src/main/java/com/superinter/
├── SuperInterApplication.java
├── common/exception/        Excepciones de la aplicación y manejador global (ProblemDetail)
├── categoria/               Módulo de categorías (solo lectura)
│   ├── domain/  repository/  dto/  service/  controller/
└── producto/                Módulo de productos (CRUD)
    ├── domain/  repository/  dto/  service/  controller/

database/
├── schema.sql               Estructura completa de la base de datos
├── data.sql                 Categorías iniciales
├── usuario_aplicacion.sql   Usuario de la aplicación con mínimo privilegio
└── migrations/              Historial de cambios sobre la base de datos
```

Cada módulo separa sus responsabilidades en capas: dominio, acceso a datos (JDBC), DTO de la API, reglas de negocio y controlador REST.

## Convenciones de codificación

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases | PascalCase | `ProductoService` |
| Métodos y variables | camelCase | `buscarPorId`, `idProducto` |
| Constantes | UPPER_SNAKE_CASE | `SQL_BUSCAR_POR_ID` |
| Paquetes | minúsculas, organizados por módulo y capa | `com.superinter.producto.repository` |

Los conceptos del negocio se nombran en español (`Producto`, `Categoria`) y el rol técnico en inglés como sufijo (`Repository`, `Service`, `Controller`, `Request`, `Response`, `Exception`).

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

Copie la plantilla y complete los valores:

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

La API queda disponible en `http://localhost:8080`.

## API

| Método | Ruta | Operación | Respuesta |
|---|---|---|---|
| `POST` | `/api/productos` | Insertar producto | `201 Created` |
| `GET` | `/api/productos` | Consultar todos | `200 OK` |
| `GET` | `/api/productos/{id}` | Consultar por id | `200 OK` / `404` |
| `PUT` | `/api/productos/{id}` | Actualizar | `200 OK` / `404` / `409` |
| `DELETE` | `/api/productos/{id}` | Eliminar | `204 No Content` / `404` / `409` |
| `GET` | `/api/categorias` | Consultar categorías | `200 OK` |

Los errores se responden en formato `ProblemDetail` (RFC 9457): `400` para datos inválidos, `404` para recursos inexistentes y `409` para código duplicado o producto con ventas o compras registradas.

### Ejemplos

**Inserción**

```bash
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"codigo":"7701234567890","nombre":"Arroz Diana 1kg","precio":4500.00,"stock":20,"fechaVencimiento":"2027-03-15","idCategoria":1}'
```

```
HTTP/1.1 201
Location: http://localhost:8080/api/productos/15

{"idProducto":15,"codigo":"7701234567890","nombre":"Arroz Diana 1kg","precio":4500.00,"stock":20,"fechaVencimiento":"2027-03-15","categoria":{"idCategoria":1,"nombre":"Abarrotes"}}
```

**Consulta**

```bash
curl -i http://localhost:8080/api/productos
curl -i http://localhost:8080/api/productos/15
```

**Actualización**

```bash
curl -i -X PUT http://localhost:8080/api/productos/15 \
  -H "Content-Type: application/json" \
  -d '{"codigo":"7701234567890","nombre":"Arroz Diana 5kg","precio":21000.00,"stock":5,"idCategoria":1}'
```

```
HTTP/1.1 200

{"idProducto":15,"codigo":"7701234567890","nombre":"Arroz Diana 5kg","precio":21000.00,"stock":5,"fechaVencimiento":null,"categoria":{"idCategoria":1,"nombre":"Abarrotes"}}
```

**Eliminación**

```bash
curl -i -X DELETE http://localhost:8080/api/productos/15
```

```
HTTP/1.1 204
```

**Datos inválidos**

```bash
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"codigo":"","nombre":"Prueba","precio":0,"stock":-1,"idCategoria":1}'
```

```
HTTP/1.1 400

{"detail":"Uno o más campos tienen valores inválidos","status":400,"title":"Datos inválidos","errores":{"stock":"El stock no puede ser negativo","codigo":"El código es obligatorio","precio":"El precio debe ser mayor que cero"}}
```

## Pruebas

```bash
./mvnw test
```

35 pruebas automatizadas:

- **Integración** (requieren la base de datos): conexión JDBC y operaciones de los repositorios contra PostgreSQL. Los datos de prueba usan el prefijo `TEST-` y se eliminan al terminar.
- **Unitarias** (sin base de datos): reglas de negocio de los servicios, validaciones de la solicitud y manejo de errores.

## Repositorio

https://github.com/andresfzv-dev/Super-Inter-Sena