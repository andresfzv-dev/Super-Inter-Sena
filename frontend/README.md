# Super Inter – Front-end en React (módulo de productos)

Aplicación de una sola página (SPA) que consume la API REST de productos del backend Spring Boot
de este mismo repositorio. Evidencia GA7-220501096-AA4-EV03.

## Requisitos

- Node.js `^22.22.2`, `^24.15.0` o `>=26` (recomendado Node 24 LTS). `jsdom`, que usan las
  pruebas, no funciona con versiones anteriores.
- El backend en ejecución en `http://localhost:8080` (ver el README de la raíz).

## Ejecución

```bash
# 1. En la raíz del repositorio, levantar el backend
./mvnw spring-boot:run

# 2. En otra terminal, dentro de frontend/
npm install
npm run dev
```

La aplicación queda en `http://localhost:5173`. El servidor de Vite reenvía las peticiones a
`/api` hacia `http://localhost:8080` (`vite.config.js`), así que el navegador solo habla con un
origen y el backend no necesita configuración CORS.

## Comandos

| Comando | Uso |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática |
| `npm run build` | Compilación de producción en `dist/` |
| `npm run preview` | Sirve la compilación de `dist/` |
| `npm run lint` | ESLint; falla también con advertencias |
| `npm run format` | Formatea `src/**/*.{js,jsx}` con Prettier |
| `npm run format:check` | Verifica el formato sin modificar archivos |
| `npm test` | Ejecuta las pruebas una vez (Vitest) |
| `npm run test:watch` | Ejecuta las pruebas al guardar cambios |

Prettier solo formatea `src/**/*.{js,jsx}` para no reformatear los CSS copiados del prototipo.

## Pantallas y rutas

| Ruta | Pantalla |
|---|---|
| `/` | Redirige a `/productos` |
| `/productos` | Listado con búsqueda por nombre, filtro por categoría y eliminación con ventana de confirmación |
| `/productos/nuevo` | Registro de producto |
| `/productos/:idProducto/editar` | Edición de producto |
| `*` | Página no encontrada |

## Estructura

```
src/
├── main.jsx                 punto de entrada: estilos y <App /> en StrictMode
├── App.jsx                  rutas
├── api/                     cliente HTTP con fetch (clienteHttp) y servicios por recurso
├── components/
│   ├── layout/              AppLayout, MenuLateral, Encabezado y PiePagina
│   └── ui/                  Alerta, CampoFormulario y ModalConfirmacion
├── features/productos/
│   ├── productoFormulario.js    validación y conversión entre el formulario y la API
│   ├── filtrarProductos.js      búsqueda en el navegador
│   ├── components/              TablaProductos, FiltrosProductos, FormularioProducto, DetalleProducto
│   └── pages/                   ProductoListaPage y ProductoFormularioPage
├── hooks/                   título de la página (encabezado y document.title)
├── pages/                   PaginaNoEncontrada
├── styles/                  CSS del prototipo GA6-220501096-AA4-EV03
├── test/                    configuración y datos de las pruebas
└── utils/formato.js         formato de precio (COP) y fecha (dd/mm/aaaa)
```

## Decisiones

- **Filtrado en el navegador:** `GET /api/productos` no recibe parámetros, así que la búsqueda por
  nombre (sin distinguir mayúsculas) y por categoría se hace sobre la lista ya cargada.
- **Validación en el cliente:** replica las reglas y los mensajes de `ProductoRequest`. El backend
  sigue validando; sus errores 400 se muestran junto a cada campo y los 404 y 409 como alerta.
- **Backend no disponible:** si `fetch` falla, o si el proxy de Vite responde 502, 503 o 504
  (lo que ocurre con el backend apagado), se muestra un mensaje de conexión.
- **Botón ☰:** en la copia de `layout.css` y `responsive.css` se aumentó la especificidad del
  selector del botón, porque `.btn` (que se carga después) lo dejaba visible en escritorio.
