# Super Inter

Módulos de **gestión de productos** y **gestión de proveedores** del Sistema de Gestión Integral para el supermercado Super Inter, con un front-end en React para el módulo de productos y un **servicio web de registro e inicio de sesión** con token JWT.

**Aprendiz:** Andrés Felipe Zambrano Velasquez
**Programa:** Análisis y Desarrollo de Software (ADSO) – SENA, ficha 3336098

| Evidencia | Contenido | Versión |
|---|---|---|
| GA7-220501096-AA2-EV01 – Codificación de módulos del software | CRUD de productos con JDBC y API REST | `v1.0.0` |
| GA7-220501096-AA2-EV02 – Módulos de software codificados y probados | Módulo web con servlets, formularios HTML y JSP | `v2.0.0` |
| GA7-220501096-AA3-EV01 – Codificación de módulos del software stand-alone, web y móvil | Módulo web de gestión de proveedores | `v3.0.0` |
| GA7-220501096-AA4-EV03 – Componente front-end del proyecto formativo y proyectos de clase | Front-end en React del módulo de productos | `v4.0.0` |
| GA7-220501096-AA5-EV01 – Diseño y desarrollo de servicios web – caso | Servicio web REST de registro e inicio de sesión con JWT | `v5.0.0` |

## Descripción

Aplicación web que implementa el registro, consulta, actualización y eliminación de dos módulos, con acceso a datos por **JDBC**:

| Módulo | Interfaz web (servlets y JSP) | API REST | Front-end en React |
|---|---|---|---|
| Productos | Sí (AA2-EV02) | Sí (AA2-EV01) | Sí (AA4-EV03) |
| Proveedores | Sí (AA3-EV01) | No | No |
| Usuarios (registro e inicio de sesión) | No | Sí (AA5-EV01) | No |

```
Navegador (formularios HTML)        Cliente HTTP (curl, Postman, frontend)
            │                                   │
            │                     Spring Security (valida el token JWT)
            │                                   │
       Servlets + JSP          Controladores REST (productos, autenticación)
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
| Spring Security 7 | Cifrado de contraseñas con BCrypt y validación del token JWT (resource server) |
| Nimbus JOSE + JWT | Firma y verificación de tokens JWT con HS256 (`JwtEncoder` y `JwtDecoder`) |
| Jakarta Servlet (`HttpServlet`) | Controladores del módulo web |
| JSP, Expression Language y JSTL | Vistas del módulo web |
| JDBC (`DataSource`, `PreparedStatement`, `ResultSet`) | Acceso a datos |
| HikariCP | Pool de conexiones JDBC |
| PostgreSQL 16 | Base de datos |
| Maven Wrapper | Construcción del proyecto (empaquetado WAR) |
| JUnit 5 y AssertJ | Pruebas |
| Lombok | Reducción de código repetitivo |
| Git y GitHub | Control de versiones |
| React 19 y React Router | Front-end del módulo de productos (SPA) |
| Vite | Servidor de desarrollo, proxy hacia la API y compilación del front-end |
| ESLint y Prettier | Estándares de codificación y formato del front-end |
| Vitest y Testing Library | Pruebas del front-end |

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

## Front-end en React (AA4-EV03)

Aplicación de una sola página (SPA) en `frontend/` que implementa el módulo de productos consumiendo la API REST de AA2-EV01. El backend no se modificó.

### Arquitectura

```
Navegador (React)  ──fetch /api──▶  Vite (proxy)  ──▶  Spring Boot (API REST)  ──▶  PostgreSQL
 localhost:5173                                         localhost:8080
```

- **SPA + API REST:** React dibuja las pantallas en el navegador y solo intercambia JSON con el backend. Las rutas (`/productos`, `/productos/nuevo`...) las resuelve React Router sin pedir páginas al servidor.
- **Proxy de Vite:** el servidor de desarrollo reenvía `/api` a `http://localhost:8080`. El navegador habla con un solo origen, así que el backend no necesita configuración CORS.
- **Cliente HTTP central** (`src/api/clienteHttp.js`): todas las peticiones pasan por `solicitar()`, que agrega los encabezados JSON y convierte las respuestas de error en `ErrorApi`. Los servicios `productosApi` y `categoriasApi` solo definen rutas y métodos.
- **Organización por funcionalidad:** `features/productos/` agrupa las páginas, los componentes, la validación y el filtrado del módulo. `components/` contiene la estructura (menú, encabezado, pie) y los componentes reutilizables (`Alerta`, `CampoFormulario`, `ModalConfirmacion`).

### Ejecución

Se necesitan dos terminales:

```bash
# Terminal 1, en la raíz: backend
./mvnw spring-boot:run

# Terminal 2: front-end (Node.js 24 LTS)
cd frontend
npm install
npm run dev
```

El front-end queda en http://localhost:5173. El detalle de comandos y estructura está en [`frontend/README.md`](frontend/README.md).

### Pantallas y rutas

| Ruta | Pantalla | Operaciones de la API |
|---|---|---|
| `/` | Redirige a `/productos` | — |
| `/productos` | Listado con búsqueda por nombre, filtro por categoría y eliminación con ventana de confirmación | `GET /api/productos`, `GET /api/categorias`, `DELETE /api/productos/{id}` |
| `/productos/nuevo` | Registro | `GET /api/categorias`, `POST /api/productos` |
| `/productos/:idProducto/editar` | Edición | `GET /api/productos/{id}`, `GET /api/categorias`, `PUT /api/productos/{id}` |
| `*` | Página no encontrada | — |

Al guardar o eliminar, el listado muestra los mismos mensajes del módulo JSP ("Producto registrado correctamente.", etc.). El mensaje viaja en el `state` de la navegación y se borra del historial, para que no reaparezca al recargar.

### Manejo de errores

| Situación | Respuesta en pantalla |
|---|---|
| Datos inválidos (validación en el navegador) | Mensaje junto a cada campo, sin enviar la petición. Usa las reglas y los mensajes de `ProductoRequest` y `ProductoFormulario` |
| `400` con `errores` por campo | Mensaje del backend junto a cada campo |
| `409` (código duplicado, producto con ventas o compras) | Alerta general en el formulario o en la ventana de eliminación |
| `404` o id de ruta no válido | Alerta "Producto con id N no existe" sin mostrar el formulario |
| Backend apagado (falla de `fetch` o `502`/`503`/`504` del proxy) | "No se pudo conectar con el servidor. Verifique que el backend esté en ejecución." |

Los campos con error reciben la clase `has-error`, `aria-invalid` y `aria-describedby`, para que el mensaje sea visible y lo anuncien los lectores de pantalla.

### Estándares de codificación

- **ESLint** (configuración plana) con las reglas recomendadas de JavaScript, `react-hooks` y `react-refresh`. `npm run lint` falla también con advertencias.
- **Prettier** con comillas simples, punto y coma, `trailingComma: all` y ancho de 100 columnas. Solo formatea `src/**/*.{js,jsx}`, para no reformatear los CSS del prototipo.
- Componentes en PascalCase (`ProductoListaPage`), funciones y variables en camelCase (`filtrarProductos`), constantes en UPPER_SNAKE_CASE (`FORMULARIO_VACIO`). Conceptos del negocio en español y rol técnico en inglés (`productosApi`, `ProductoFormularioPage`).

### Comentarios en el código

Como en el backend, los comentarios explican **por qué**. Por ejemplo:

- `clienteHttp.js` explica por qué los estados 502, 503 y 504 se tratan como falta de conexión con el backend.
- `formato.js` explica por qué la fecha no se convierte con `new Date('aaaa-mm-dd')`: se interpreta como UTC y en Colombia (UTC-5) mostraría el día anterior.
- `productoFormulario.js` explica por qué los decimales del precio se cuentan sin los ceros finales, igual que `@Digits` en el backend.
- `ModalConfirmacion` explica por qué no se cierra con Escape mientras se procesa la eliminación.

### Pruebas

```bash
cd frontend
npm test
```

67 pruebas automatizadas con Vitest, Testing Library y user-event. No requieren el backend: en las pruebas de páginas los servicios de la API se reemplazan con `vi.mock`.

| Archivo | Pruebas | Qué verifica |
|---|---|---|
| `clienteHttp.test.js` | 9 | Respuesta 2xx, envío de JSON, 204, ProblemDetail 400, error sin JSON, falla de red y 502 del proxy |
| `productosApi.test.js` | 6 | Ruta y método de cada servicio según el contrato de la API |
| `formato.test.js` | 6 | Precio en COP y fecha sin desfase por zona horaria |
| `productoFormulario.test.js` | 21 | Validaciones de obligatorios, longitudes, precio y stock; conversiones `aSolicitud` y `desdeProducto` |
| `filtrarProductos.test.js` | 4 | Nombre sin distinguir mayúsculas, categoría y ambos combinados |
| `ProductoListaPage.test.jsx` | 10 | Listado, filtros en vivo, estado vacío, mensaje de éxito, error de conexión, eliminación confirmada, cancelada y con 409 |
| `ProductoFormularioPage.test.jsx` | 11 | Errores del cliente, registro, 409, errores 400 por campo, edición, 404 e id no válido |

Además se hizo una prueba manual de punta a punta con el backend y PostgreSQL (11 casos, incluida la vista móvil y el backend apagado). Los datos de prueba usaron el prefijo `TEST-` y se eliminaron al terminar.

## Servicio web de autenticación (AA5-EV01)

Servicio web REST de **registro** e **inicio de sesión**. Recibe un usuario (el correo) y una contraseña: si la autenticación es correcta responde **"Autenticación satisfactoria"** con un token JWT; si no, responde **"Error en la autenticación"**.

### Endpoints

| Método | Ruta | Acceso | Respuestas |
|---|---|---|---|
| `POST` | `/api/auth/registro` | Público | `201` registrado, `400` datos inválidos, `409` correo ya registrado |
| `POST` | `/api/auth/login` | Público | `200` autenticación satisfactoria, `400` datos vacíos, `401` error en la autenticación |
| `GET` | `/api/auth/perfil` | Requiere `Authorization: Bearer <token>` | `200` datos del usuario, `401` sin token o con token inválido o vencido |

Los errores se responden en formato `ProblemDetail` (RFC 9457), igual que el resto de la API.

#### Registro

| Campo | Obligatorio | Validación |
|---|---|---|
| `nombre` | Sí | Máximo 100 caracteres; se quitan los espacios de los extremos |
| `correo` | Sí | Formato de correo, máximo 150 caracteres; se quitan los espacios y se convierte a minúsculas |
| `contrasena` | Sí | Mínimo 8 caracteres y máximo **72 bytes en UTF-8** |

El cliente no envía el rol: todo usuario registrado queda como `CAJERO`. Si el JSON trae un campo `rol`, se ignora.

```bash
curl -i -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Pérez","correo":"ana.perez@ejemplo.com","contrasena":"Clave-Segura-2026"}'
```

Respuesta `201 Created` (nunca incluye la contraseña):

```json
{
  "mensaje": "Usuario registrado correctamente",
  "usuario": { "idUsuario": 1, "nombre": "Ana Pérez", "correo": "ana.perez@ejemplo.com", "rol": "CAJERO" }
}
```

Respuesta `409 Conflict` si el correo ya existe, aunque se escriba con otras mayúsculas:

```json
{
  "detail": "Ya existe un usuario registrado con el correo ana.perez@ejemplo.com",
  "instance": "/api/auth/registro",
  "status": 409,
  "title": "Conflicto con el estado actual"
}
```

Respuesta `400 Bad Request` con el error de cada campo:

```json
{
  "detail": "Uno o más campos tienen valores inválidos",
  "instance": "/api/auth/registro",
  "status": 400,
  "title": "Datos inválidos",
  "errores": {
    "contrasena": "La contraseña debe tener al menos 8 caracteres",
    "nombre": "El nombre es obligatorio",
    "correo": "El correo no tiene un formato válido"
  }
}
```

#### Inicio de sesión

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"ana.perez@ejemplo.com","contrasena":"Clave-Segura-2026"}'
```

Respuesta `200 OK`:

```json
{
  "mensaje": "Autenticación satisfactoria",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipoToken": "Bearer",
  "expiraEn": 3600,
  "usuario": { "idUsuario": 1, "nombre": "Ana Pérez", "correo": "ana.perez@ejemplo.com", "rol": "CAJERO" }
}
```

`expiraEn` es la vida del token en segundos. Respuesta `401 Unauthorized`, **idéntica** si el correo no existe o si la contraseña es incorrecta:

```json
{
  "detail": "Correo o contraseña incorrectos",
  "instance": "/api/auth/login",
  "status": 401,
  "title": "Error en la autenticación"
}
```

#### Perfil (protegido)

```bash
curl -i http://localhost:8080/api/auth/perfil -H "Authorization: Bearer <token>"
```

Respuesta `200 OK` con los datos tomados del token: `{ "idUsuario": 1, "nombre": "Ana Pérez", "correo": "ana.perez@ejemplo.com", "rol": "CAJERO" }`.

Sin token, o con un token alterado o vencido, responde `401` con `application/problem+json` y la cabecera `WWW-Authenticate: Bearer`:

```json
{
  "detail": "Se requiere un token de acceso. Inicie sesión y envíelo en la cabecera Authorization: Bearer <token>",
  "instance": "/api/auth/perfil",
  "status": 401,
  "title": "No autenticado"
}
```

### Flujo del token

```
Cliente                                   Backend
  │  POST /api/auth/login (correo, contraseña) │
  │ ─────────────────────────────────────────▶ │  AutenticacionService: busca el usuario y compara con BCrypt
  │ ◀───────────────────────────────────────── │  TokenService: firma el JWT con HS256 (JWT_SECRET)
  │  200 { mensaje, token, ... }               │
  │                                            │
  │  GET /api/auth/perfil                      │
  │  Authorization: Bearer <token>             │
  │ ─────────────────────────────────────────▶ │  Spring Security: verifica firma, emisor y vencimiento
  │ ◀───────────────────────────────────────── │  AutenticacionController: datos del usuario del token
  │  200 { idUsuario, nombre, correo, rol }    │
```

- El token lleva los claims `iss` (`super-inter`), `sub` (id del usuario), `iat`, `exp`, `nombre`, `correo` y `rol`. Nunca lleva la contraseña ni su hash: el contenido de un JWT solo está codificado en Base64, cualquiera con el token puede leerlo.
- El servidor no guarda sesiones (`STATELESS`): cada petición se autentica solo con su token.
- El claim `rol` se convierte en la autoridad `ROLE_<rol>` (por ejemplo `ROLE_CAJERO`), lista para restringir endpoints por rol (RNF-02).

### Configuración del secreto JWT

El token se firma con la clave de la variable `JWT_SECRET`, que va en `.env` (excluido del repositorio):

```bash
openssl rand -base64 32
```

```properties
JWT_SECRET=<salida del comando anterior>
JWT_EXPIRACION=60m
```

- `JWT_SECRET` debe estar en Base64 y tener al menos 32 bytes (256 bits, el mínimo de HS256). Si falta o es más corto, la aplicación **no arranca** y muestra: `JWT_SECRET debe estar en Base64 y tener al menos 32 bytes (genérelo con: openssl rand -base64 32)`.
- `JWT_EXPIRACION` es opcional (por defecto `60m`). Admite valores como `30m` o `2h`.
- `application.properties` lee estas variables en `superinter.jwt.secreto`, `superinter.jwt.emisor` y `superinter.jwt.expiracion`, validadas por `JwtProperties`.

### Decisiones de seguridad

- **Contraseñas con BCrypt (RNF-03):** se guardan cifradas (`$2a$10$...`), con una sal aleatoria por contraseña. Nunca se devuelven, ni se escriben en el log, ni van en el token.
- **Mensaje genérico:** el `401` del login es el mismo si el correo no existe o si la contraseña es incorrecta, para no revelar qué correos están registrados.
- **Comparación contra un hash fijo:** cuando el correo no existe, la contraseña se compara igual contra un hash BCrypt fijo, calculado al arrancar con el mismo costo que los hashes reales. Sin esto, la respuesta sería más rápida para los correos inexistentes y el tiempo revelaría cuáles existen.
- **Límite de 72 bytes:** BCrypt solo procesa los primeros 72 bytes, y Spring Security lanza una excepción si recibe más. El registro lo valida en bytes UTF-8 (las letras con tilde y la ñ ocupan 2), con la anotación `@MaximoBytesUtf8`, así que responde `400` en lugar de `500`. El login rechaza con `401` una contraseña de más de 72 bytes, porque BCrypt la recortaría y aceptaría cualquier texto agregado después de una contraseña de 72 bytes.
- **Rol por defecto:** el registro público asigna siempre `CAJERO`. Por mínimo privilegio y por la regla de negocio 3, el cliente no puede elegir su rol; cambiarlo es tarea del administrador (HU4).
- **Correo normalizado:** se guarda y se busca sin espacios y en minúsculas. La restricción `chk_usuarios_correo_minusculas` lo garantiza en la base de datos, así que la restricción única del correo tampoco distingue mayúsculas.
- **CSRF desactivado:** la autenticación no usa cookies ni sesión; el token viaja en la cabecera `Authorization`, que un sitio externo no puede agregar a una petición del navegador. Además, los formularios JSP envían POST sin token CSRF. Debe activarse si en el futuro se agrega un inicio de sesión por sesión o cookies.
- **Endpoints públicos:** solo `/api/auth/perfil` exige token. Las JSP, la API de productos y el front-end en React siguen funcionando sin iniciar sesión, porque aún no tienen pantalla de inicio de sesión.
- **Sin `formLogin` ni `httpBasic`:** la única forma de autenticarse es el endpoint de login. Spring Security no genera un usuario ni una contraseña por defecto.
- **Registro de actividades (RNF-04):** se registran el registro de un usuario (`info`, con el id), el inicio de sesión exitoso (`info`, con el id) y el fallido (`warn`, con el correo). Nunca se registran contraseñas, hashes ni tokens.

### Cambios en la base de datos

La tabla `usuarios` ya existía en el modelo lógico. No se creó; se ajustó con dos migraciones:

`database/migrations/003_modulo_usuarios.sql`:

- `chk_usuarios_nombre`: rechaza nombres formados solo por espacios.
- `chk_usuarios_rol`: el rol solo puede ser `ADMINISTRADOR`, `GERENTE`, `AUXILIAR_INVENTARIOS`, `CAJERO` o `COMPRAS`.
- `chk_usuarios_correo_minusculas`: el correo se guarda en minúsculas.
- Permisos de `super_inter_app`: `SELECT` e `INSERT` sobre `usuarios` y `USAGE` sobre su secuencia. Sin `UPDATE` ni `DELETE`, por mínimo privilegio.

`database/migrations/004_limpieza_usuarios_de_prueba.sql`:

- Función `eliminar_usuarios_de_prueba()`, `SECURITY DEFINER`, que solo borra usuarios con correo `test-%`. Las pruebas automáticas la usan para limpiar sus datos sin que la aplicación tenga permiso `DELETE` sobre la tabla. Solo `super_inter_app` puede ejecutarla.

Los permisos se agregaron también a `database/usuario_aplicacion.sql` para instalaciones nuevas, y `database/schema.sql` se regeneró.

En una base de datos existente, se aplican como `postgres`:

```bash
psql -U postgres -h localhost -p 5434 -d super-inter -v ON_ERROR_STOP=1 \
  -f database/migrations/003_modulo_usuarios.sql
psql -U postgres -h localhost -p 5434 -d super-inter -v ON_ERROR_STOP=1 \
  -f database/migrations/004_limpieza_usuarios_de_prueba.sql
```

### Comentarios en el código

Como en los demás módulos, los comentarios explican **por qué**. Por ejemplo:

- `AutenticacionService` explica la comparación contra un hash fijo y el control de los 72 bytes en el login.
- `SeguridadConfig` explica por qué es seguro desactivar CSRF hoy y cuándo habría que activarlo, y por qué solo el perfil exige token.
- `MaximoBytesUtf8` explica por qué `@Size` no basta para la contraseña.
- `RespuestaErrorSeguridad` explica por qué los errores de seguridad no llegan a `@RestControllerAdvice` y cómo se responden en ProblemDetail.
- La migración 004 explica por qué la función fija `search_path` y por qué el filtro `test-%` no se recibe como parámetro.

### Pruebas automáticas

50 pruebas nuevas en el backend:

| Clase | Pruebas | Qué verifica | Requiere BD |
|---|---|---|---|
| `RegistroRequestTest` | 13 | Obligatorios, longitudes, formato del correo, contraseña de 7 y 8 caracteres, límite de 72 bytes con "ñ" y normalización del correo | No |
| `UsuarioServiceTest` | 3 | Rol `CAJERO`, contraseña cifrada verificable con `matches` y correo duplicado sin distinguir mayúsculas | No |
| `AutenticacionServiceTest` | 5 | Credenciales correctas, contraseña incorrecta, correo inexistente (misma excepción y mensaje), correo con mayúsculas y espacios, texto agregado a una contraseña de 72 bytes | No |
| `TokenServiceTest` | 6 | Claims y algoritmo del token, sin contraseña, token de otro secreto rechazado y token vencido rechazado | No |
| `RolTest` | 2 | Conversión del código guardado al rol | No |
| `GlobalExceptionHandlerTest` | 4 | Respuestas `401` de credenciales, sin token y con token inválido, y `403` | No |
| `JdbcUsuarioRepositoryTest` | 4 | Guardar, buscar por correo, verificar si existe y SQLSTATE `23505` con correo duplicado | Sí |
| `AutenticacionIntegracionTest` | 13 | De punta a punta con Tomcat y PostgreSQL: registro, login, perfil y regresión de `/api/productos`, la JSP `/productos` y un POST de formulario JSP sin `403` por CSRF | Sí |

### Pruebas manuales

Con el backend en ejecución (`./mvnw spring-boot:run`), en Git Bash. Los correos de prueba empiezan por `test-` para poder borrarlos al final.

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | Registro correcto | `201`, rol `CAJERO`, sin contraseña en la respuesta |
| 2 | Registro con el mismo correo en mayúsculas | `409` "Ya existe un usuario registrado con el correo..." |
| 3 | Registro con datos inválidos | `400` con errores en `nombre`, `correo` y `contrasena` |
| 4 | Login correcto | `200` "Autenticación satisfactoria" y token |
| 5 | Login con contraseña incorrecta | `401` "Error en la autenticación" |
| 6 | Login con correo inexistente | `401` con el mismo cuerpo de la prueba 5 |
| 7 | Perfil sin token | `401` "No autenticado" en JSON |
| 8 | Perfil con token | `200` con `idUsuario`, `nombre`, `correo` y `rol` |
| 9 | Perfil con token alterado | `401` "El token de acceso no es válido o está vencido..." |
| 10 | JSP de productos y proveedores, y front-end React | Funcionan sin iniciar sesión; un formulario JSP guarda sin `403` |

```bash
# 1. Registro correcto
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Usuario Manual","correo":"test-manual@ejemplo.com","contrasena":"Clave-Segura-2026"}'

# 2. Mismo correo en mayúsculas
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Otro","correo":"TEST-MANUAL@EJEMPLO.COM","contrasena":"Clave-Segura-2026"}'

# 3. Datos inválidos
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"","correo":"sin-arroba","contrasena":"1234567"}'

# 4. Login correcto
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test-manual@ejemplo.com","contrasena":"Clave-Segura-2026"}'

# Guardar el token en una variable (sin imprimirlo), para las pruebas 8 y 9
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test-manual@ejemplo.com","contrasena":"Clave-Segura-2026"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "Longitud del token: ${#TOKEN}"

# 5. Contraseña incorrecta
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test-manual@ejemplo.com","contrasena":"incorrecta123"}'

# 6. Correo inexistente
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test-no-existe@ejemplo.com","contrasena":"Clave-Segura-2026"}'

# 7. Perfil sin token
curl -s -i http://localhost:8080/api/auth/perfil

# 8. Perfil con token
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/api/auth/perfil \
  -H "Authorization: Bearer $TOKEN"

# 9. Perfil con token alterado (cambia el primer carácter de la firma)
FIRMA=${TOKEN##*.}
if [ "${FIRMA:0:1}" = "A" ]; then C=B; else C=A; fi
ALTERADO="${TOKEN%.*}.${C}${FIRMA:1}"
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/api/auth/perfil \
  -H "Authorization: Bearer $ALTERADO"
```

Para la prueba 10 se abren http://localhost:8080/productos y http://localhost:8080/proveedores, se registra un proveedor `TEST-` desde el formulario, y se ejecuta el front-end en React (`cd frontend && npm run dev`). Todo debe funcionar sin iniciar sesión.

**Resultado:** las 10 pruebas pasaron.

#### Colección de Postman

`docs/postman/Super-Inter-Autenticacion.postman_collection.json` tiene 6 peticiones con pruebas automáticas (`pm.test`), en este orden:

1. Registro correcto (`201`). Genera un correo `test-postman-<marca de tiempo>@ejemplo.com` en cada ejecución, para poder repetir la colección.
2. Registro duplicado con el correo en mayúsculas (`409`).
3. Login correcto (`200`). Guarda el token en la variable `{{token}}`.
4. Login fallido (`401`).
5. Perfil con token (`200`). Envía `Authorization: Bearer {{token}}`.
6. Perfil sin token (`401`).

Se importa en Postman (*Import*) y se ejecuta con el *Collection Runner*. La variable `baseUrl` vale `http://localhost:8080`.

#### Verificación en la base de datos y limpieza

Como `postgres`, para comprobar que la contraseña quedó cifrada (sin mostrar el hash completo):

```bash
psql -U postgres -h localhost -p 5434 -d super-inter -c "SELECT id_usuario, correo, rol, left(contrasena, 7) AS inicio_hash, length(contrasena) AS longitud, contrasena LIKE '\$2a\$%' AS es_bcrypt FROM usuarios WHERE correo LIKE 'test-%' ORDER BY id_usuario;"
```

Resultado esperado: `inicio_hash` = `$2a$10$`, `longitud` = 60, `es_bcrypt` = `t`.

Para borrar los datos de prueba:

```bash
psql -U postgres -h localhost -p 5434 -d super-inter \
  -c "BEGIN;" \
  -c "DELETE FROM usuarios WHERE correo LIKE 'test-%';" \
  -c "DELETE FROM proveedores WHERE nombre LIKE 'TEST-%';" \
  -c "COMMIT;"
```

### Pendientes

- **Proteger los demás módulos con el token:** hoy solo `/api/auth/perfil` lo exige. La API de productos, las JSP y los demás endpoints siguen siendo públicos.
- **Pantalla de inicio de sesión** en el front-end en React y en las JSP.
- **Gestión de usuarios y roles por el administrador (HU4):** consultar usuarios y cambiar su rol. Requerirá una migración con permiso `UPDATE` sobre `usuarios`.
- **Token inválido en rutas públicas:** si una petición a una ruta pública trae `Authorization: Bearer` con un token inválido, Spring Security responde `401`. Es el comportamiento normal del resource server; hay que tenerlo en cuenta cuando el front-end empiece a enviar el token.

## Uso de JDBC

El proyecto **no usa JPA ni Hibernate**. Todas las operaciones SQL están escritas a mano con la API estándar de JDBC en `JdbcProductoRepository`, `JdbcCategoriaRepository`, `JdbcProveedorRepository` y `JdbcUsuarioRepository`. Spring Boot solo administra el pool de conexiones (`DataSource`).

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

Los números de requisito dependen del documento: el documento de especificación de requerimientos (GA1-220501093-AA4-EV02) y el documento de historias de usuario (GA2-220501093-AA1-EV03) numeran sus RF de forma distinta. Por eso RF-04 aparece en productos y en proveedores con significados diferentes; cuando hace falta, la tabla indica de qué documento sale el requisito.

### Módulo de productos

| Artefacto | Aplicación |
|---|---|
| RF-01 (registro de productos con código, nombre, categoría, fecha de vencimiento y cantidad) | Formulario web, `Producto` y `ProductoRequest` |
| RF-04 del documento de especificación de requerimientos, GA1-220501093-AA4-EV02 (consultar el inventario disponible en tiempo real) | Listado web y `GET /api/productos` |
| RF-25 (filtrar y buscar información por categoría) | Búsqueda por nombre y filtro por categoría |
| Regla de negocio 1 (el SKU no puede repetirse) | `ProductoService` y restricción `uq_productos_codigo` |
| Modelo lógico (GA4-220501095-AA1-EV01) | Tablas `productos` y `categorias` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Entidades `Producto` y `Categoria` y su asociación |
| Diseño front-end (GA6-220501096-AA4-EV03) | Hojas de estilo, paleta de colores y estructura de las pantallas de productos |
| Entorno operativo: aplicación web cliente-servidor | Servlets y JSP sobre Tomcat |

### Módulo de proveedores

| Artefacto | Aplicación |
|---|---|
| RF-04 del documento de historias de usuario, GA2-220501093-AA1-EV03 (gestionar proveedores: crear, editar y consultar) | Listado, formulario de registro y de edición, `ProveedorService` |
| RF-15 del documento de especificación de requerimientos, GA1-220501093-AA4-EV02 (información de contacto del proveedor) | Campos contacto, teléfono y correo |
| Modelo lógico (GA4-220501095-AA1-EV01) | Tabla `proveedores` y su relación con `compras` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Entidad `Proveedor` |
| Historia de usuario HU2 | Depende de que existan proveedores registrados |
| Diseño front-end (GA6-220501096-AA4-EV03) | Estructura de la pantalla `suppliers/list.html`, estilos y opción del menú |

### Front-end en React (AA4-EV03)

| Artefacto | Aplicación |
|---|---|
| RF-01 (registro de productos con código, nombre, categoría, fecha de vencimiento y cantidad) | Pantalla `/productos/nuevo`, `FormularioProducto` y validación en `productoFormulario.js` |
| RF-25 (filtrar y buscar información por categoría) | `FiltrosProductos` y `filtrarProductos.js`: búsqueda por nombre y filtro por categoría |
| Regla de negocio 1 (el SKU no puede repetirse) | El `409` de código duplicado se muestra como alerta en el formulario |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Los datos de las pantallas siguen `Producto` y su asociación con `Categoria` |
| Diseño front-end (GA6-220501096-AA4-EV03) | Los cinco CSS del prototipo y la estructura de las pantallas de productos, replicada de las JSP |
| Plan de trabajo del proyecto formativo | Front-end desarrollado con React |

La consulta del inventario disponible se implementa en `/productos` (`ProductoListaPage`).

### Servicio web de autenticación (AA5-EV01)

| Artefacto | Aplicación |
|---|---|
| RF-17 del documento de especificación de requerimientos, GA1-220501093-AA4-EV02 (creación de usuarios con roles y permisos específicos) | `POST /api/auth/registro`, `UsuarioService` y enum `Rol` |
| RNF-01 (autenticación mediante usuario y contraseña) | `POST /api/auth/login` y `AutenticacionService` |
| RNF-02 (permisos basados en roles) | El token lleva el claim `rol`, convertido en la autoridad `ROLE_<rol>` |
| RNF-03 (información sensible almacenada en la base de datos) | Contraseñas cifradas con BCrypt; nunca se devuelven ni se registran |
| RNF-04 (log interno de actividades importantes) | Registro de usuarios e inicios de sesión exitosos y fallidos con `@Slf4j` |
| Regla de negocio 3 (solo el administrador modifica parámetros) | El registro público no deja elegir el rol: siempre asigna `CAJERO` |
| Clases y características de usuarios | Roles `ADMINISTRADOR`, `GERENTE`, `AUXILIAR_INVENTARIOS`, `CAJERO` y `COMPRAS`, con su nombre visible |
| HU4 "Administrar usuarios" (GA2-220501093-AA1-EV03) | Crear usuario y asignar rol. La gestión de roles por el administrador queda para una evidencia futura |
| Modelo conceptual y lógico | Entidad USUARIO: `id_usuario`, `nombre`, `correo`, `contrasena`, `rol` |
| Diagrama de clases (GA4-220501095-AA2-EV04) | Clase `Usuario`; su operación `iniciarSesion()` está en `AutenticacionService.iniciarSesion` |

### Ajustes justificados

- **Columna `codigo`:** RF-01 y la regla de negocio 1 exigen un código único que el modelo lógico no incluía. Se agregó con restricción `UNIQUE` (`database/migrations/001_ajustes_modulo_productos.sql`).
- **Precio como `NUMERIC(12,2)` / `BigDecimal`:** el diagrama de clases usa `double`, que produce errores de redondeo con valores monetarios.
- **Campos del prototipo de front-end:** el prototipo incluía descripción, proveedor, unidad, costo, stock mínimo y estado, que no existen en el modelo lógico. Las pantallas web conservan el diseño del prototipo con los campos del modelo de datos, y agregan código y fecha de vencimiento (RF-01).
- **Menú, encabezado y pie:** en el prototipo se generaban con JavaScript. En el módulo web se generan en el servidor con `<jsp:include>`. El menú muestra solo los módulos implementados.
- **Correo del proveedor:** no estaba en el modelo lógico. Se agregó como campo opcional para completar la información de contacto (RF-15).
- **Nombre de proveedor único:** el modelo lógico no lo exigía. Se agregó para evitar registros duplicados del mismo proveedor, sin distinguir mayúsculas.
- **Eliminación de proveedores:** RF-04 del documento de historias de usuario y el diagrama de clases solo indican crear, editar y consultar. Se agregó la eliminación para completar el CRUD; la base de datos impide eliminar un proveedor con compras registradas.
- **NIT y estado del prototipo:** la pantalla de proveedores del prototipo los mostraba, pero no existen en el modelo lógico. No se incluyeron.
- **Filtrado en el navegador (front-end React):** `GET /api/productos` no recibe parámetros de búsqueda. Para no modificar el contrato de la API, el front-end carga la lista una vez y filtra en el navegador por nombre (sin distinguir mayúsculas) y por categoría, mientras el usuario escribe.
- **Eliminación con ventana modal (front-end React):** el módulo JSP usa una pantalla de confirmación porque los formularios HTML necesitan una petición GET para mostrarla. En la SPA la confirmación se muestra en una ventana modal del prototipo (`.modal`) sobre el listado, sin cambiar de página.
- **Corrección del CSS del botón ☰ (front-end React):** en el prototipo, `.btn { display: inline-flex }` se carga después de `.app-header__menu-toggle { display: none }` y lo deja visible en escritorio. En la copia de `frontend/src/styles/` el selector se cambió a `.app-header .app-header__menu-toggle`, que tiene más especificidad. Los CSS de `src/main/resources/static/` no se modificaron.
- **El correo como nombre de usuario (AA5-EV01):** el modelo lógico no tiene un campo "usuario". Se usa el correo, que ya era único, normalizado sin espacios y en minúsculas.
- **`iniciarSesion()` en un servicio (AA5-EV01):** el diagrama de clases pone la operación en la clase `Usuario`. Se implementó en `AutenticacionService`, porque necesita el repositorio, el cifrado y el token; así `Usuario` sigue siendo un objeto de datos inmutable.
- **Función de limpieza para las pruebas (AA5-EV01):** la aplicación no tiene permiso `DELETE` sobre `usuarios`, por mínimo privilegio. Para que las pruebas limpien sus datos se agregó `eliminar_usuarios_de_prueba()` (migración 004), que solo borra correos `test-%`.
- **Límite de 72 bytes también en el login (AA5-EV01):** no estaba en el diseño inicial. Se agregó porque BCrypt ignora lo que pase de 72 bytes al comparar, y sin el control se aceptaría una contraseña de 72 bytes con cualquier texto agregado.
- **Hash fijo calculado al arrancar (AA5-EV01):** en lugar de escribir un hash en el código, se genera uno al crear `AutenticacionService` con el mismo `PasswordEncoder`, para que tenga siempre el mismo costo que los hashes reales.

## Estructura del proyecto

```
src/main/java/com/superinter/
├── SuperInterApplication.java
├── common/
│   ├── exception/           Excepciones de la aplicación y manejador de la API REST
│   ├── security/            Spring Security, JWT y respuestas 401/403 (AA5-EV01)
│   └── web/                 Utilidades comunes de los servlets
├── categoria/               Módulo de categorías (solo lectura)
│   ├── domain/  repository/  dto/  service/  controller/
├── producto/                Módulo de productos
│   ├── domain/  repository/  dto/  service/
│   ├── controller/          API REST (AA2-EV01)
│   └── web/                 Servlets y modelo del formulario (AA2-EV02)
├── proveedor/               Módulo de proveedores (AA3-EV01)
│   ├── domain/  repository/  dto/  service/
│   └── web/                 Servlets
└── usuario/                 Registro e inicio de sesión (AA5-EV01)
    ├── domain/  repository/  dto/  service/
    └── controller/          API REST /api/auth

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

docs/postman/                Colección de Postman del servicio de autenticación (AA5-EV01)

frontend/                    Front-end en React del módulo de productos (AA4-EV03)
├── package.json             Dependencias y scripts (dev, build, lint, format, test)
├── vite.config.js           Proxy /api hacia el backend y configuración de Vitest
├── eslint.config.js         Reglas de ESLint
├── .prettierrc.json         Reglas de formato
└── src/
    ├── api/                 Cliente HTTP y servicios de la API
    ├── components/          layout/ (menú, encabezado, pie) y ui/ (alerta, campo, modal)
    ├── features/productos/  Páginas, componentes, validación y filtrado de productos
    ├── hooks/               Título de la página
    ├── pages/               Página no encontrada
    ├── styles/              CSS del prototipo
    ├── test/                Configuración y datos de las pruebas
    └── utils/               Formato de precio y fecha
```

## Convenciones de codificación

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases | PascalCase | `ProductoFormularioServlet` |
| Métodos y variables | camelCase | `buscarPorId`, `idProducto` |
| Constantes | UPPER_SNAKE_CASE | `RUTA_NUEVO`, `SQL_BUSCAR_POR_ID` |
| Paquetes | minúsculas, por módulo y capa | `com.superinter.producto.web` |
| JSP | minúsculas con guiones | `confirmar-eliminacion.jsp` |

Los conceptos del negocio se nombran en español (`Producto`, `Categoria`, `Proveedor`) y el rol técnico en inglés como sufijo (`Repository`, `Service`, `Controller`, `Servlet`, `Request`, `Response`, `Exception`). En el front-end se sigue la misma idea: `ProductoListaPage`, `productosApi`, `AppLayout`.

Cada módulo se organiza por capas (`domain`, `repository`, `dto`, `service`, `web`). Un módulo usa el servicio de otro módulo, nunca su repositorio. Las dependencias se inyectan por constructor.

## Control de versiones

- Los commits siguen **Conventional Commits** en español (`feat(proveedor): ...`, `test(proveedor): ...`, `fix(proveedor): ...`).
- Cada funcionalidad se desarrolla en una rama (`feat/modulo-proveedores`) y se integra a `main` mediante un Pull Request con merge commit.
- Cada evidencia entregada se marca con una etiqueta de versión (`v1.0.0`, `v2.0.0`, `v3.0.0`, `v4.0.0`, `v5.0.0`).
- Los cambios en la base de datos se registran como migraciones numeradas. Una migración aplicada no se modifica.
- El archivo `.env` con las credenciales está excluido del repositorio; `.env.example` sirve de plantilla.

## Requisitos

- JDK 21 o superior
- PostgreSQL 16 o superior, con `psql` 16.10 o superior (el `schema.sql` usa `\restrict`)
- Git
- Node.js 24 LTS para el front-end (mínimo `^22.22.2` o `^24.15.0`)

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
JWT_SECRET=salida_de_openssl_rand_base64_32
JWT_EXPIRACION=60m
```

`JWT_SECRET` se genera con `openssl rand -base64 32` (funciona en Git Bash). Sin un `JWT_SECRET` válido la aplicación no arranca; ver [Configuración del secreto JWT](#configuración-del-secreto-jwt).

El archivo `.env` está excluido del repositorio.

### 3. Ejecución

```bash
./mvnw spring-boot:run
```

- **Productos:** http://localhost:8080/productos
- **Proveedores:** http://localhost:8080/proveedores
- **API REST:** http://localhost:8080/api/productos
- **Servicio de autenticación:** http://localhost:8080/api/auth (ver [Servicio web de autenticación](#servicio-web-de-autenticación-aa5-ev01))
- **Front-end en React:** http://localhost:5173, después de `npm install` y `npm run dev` en `frontend/` (ver [Front-end en React](#front-end-en-react-aa4-ev03))

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

154 pruebas automatizadas: 47 del módulo de proveedores y 50 del servicio de autenticación (detalladas en [Pruebas automáticas](#pruebas-automáticas)). Por tipo:

| Tipo | Qué verifica | Requiere BD |
|---|---|---|
| Unitarias de servlets | GET y POST de los servlets de productos y proveedores con `MockHttpServletRequest`: vistas, redirecciones, códigos HTTP, errores de validación y de negocio | No |
| Integración web | La aplicación completa con Tomcat: generación de las JSP y registro desde el formulario hasta PostgreSQL | Sí |
| Unitarias de servicios y validaciones | Reglas de negocio, validaciones de la solicitud y manejo de errores de la API. Los servicios se prueban con repositorios en memoria | No |
| Integración de repositorios | Operaciones JDBC contra PostgreSQL, incluidas las restricciones de nombre único | Sí |
| Seguridad y tokens | Cifrado de contraseñas, emisión y validación del JWT, y respuestas `401` y `403` | No |
| Integración de autenticación | Registro, login y perfil con peticiones HTTP reales, y regresión de la API, las JSP y sus formularios sin token | Sí |

Los datos de prueba usan el prefijo `TEST-` (los correos, `test-`) y se eliminan al terminar. Las pruebas necesitan el archivo `.env` con las variables de la base de datos y `JWT_SECRET`.

El front-end tiene 67 pruebas propias (`cd frontend && npm test`), descritas en [Front-end en React](#front-end-en-react-aa4-ev03).

## Repositorio

https://github.com/andresfzv-dev/Super-Inter-Sena