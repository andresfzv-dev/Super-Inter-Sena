/** Controles de búsqueda. No tienen botón de buscar: el listado se filtra con cada cambio. */
export default function FiltrosProductos({
  nombre,
  idCategoria,
  categorias,
  onCambiarNombre,
  onCambiarCategoria,
  onLimpiar,
}) {
  return (
    <div className="table-toolbar" role="search">
      <div className="search-field">
        <span className="search-field__icon" aria-hidden="true">
          &#128269;
        </span>
        <input
          type="search"
          className="form-control"
          value={nombre}
          onChange={(evento) => onCambiarNombre(evento.target.value)}
          placeholder="Buscar producto por nombre..."
          aria-label="Buscar producto por nombre"
        />
      </div>
      <div className="table-toolbar__filters">
        <select
          className="form-control"
          value={idCategoria}
          onChange={(evento) => onCambiarCategoria(evento.target.value)}
          aria-label="Filtrar por categoría"
        >
          <option value="">Todas las categorías</option>
          {categorias.map((categoria) => (
            <option key={categoria.idCategoria} value={categoria.idCategoria}>
              {categoria.nombre}
            </option>
          ))}
        </select>
        <button type="button" className="btn btn-secondary" onClick={onLimpiar}>
          Limpiar
        </button>
      </div>
    </div>
  );
}
