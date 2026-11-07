/**
 * Filtra en el navegador porque GET /api/productos no recibe parámetros de búsqueda.
 *
 * @param {Array<object>} productos lista completa recibida de la API
 * @param {{ nombre: string, idCategoria: string }} filtros valores de los controles; vacío = sin filtro
 */
export function filtrarProductos(productos, { nombre, idCategoria }) {
  const nombreBuscado = nombre.trim().toLowerCase();

  return productos.filter(
    (producto) =>
      producto.nombre.toLowerCase().includes(nombreBuscado) &&
      // El select entrega el id como texto, por eso se compara con String.
      (!idCategoria || String(producto.categoria.idCategoria) === idCategoria),
  );
}
