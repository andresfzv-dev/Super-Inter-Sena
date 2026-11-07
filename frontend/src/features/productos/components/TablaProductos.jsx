import { Link } from 'react-router';
import { formatearFecha, formatearPrecio } from '../../../utils/formato';

export default function TablaProductos({ productos, onEliminar }) {
  if (productos.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state__icon" aria-hidden="true">
          &#128230;
        </div>
        <p>No se encontraron productos.</p>
      </div>
    );
  }

  return (
    <>
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th scope="col">Código</th>
              <th scope="col">Producto</th>
              <th scope="col">Categoría</th>
              <th scope="col">Precio</th>
              <th scope="col">Stock</th>
              <th scope="col">Vencimiento</th>
              <th scope="col">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {productos.map((producto) => (
              <tr key={producto.idProducto}>
                <td>{producto.codigo}</td>
                <td>{producto.nombre}</td>
                <td>{producto.categoria.nombre}</td>
                <td>{formatearPrecio(producto.precio)}</td>
                <td>
                  {producto.stock === 0 ? (
                    <span className="badge badge-danger">Agotado</span>
                  ) : (
                    producto.stock
                  )}
                </td>
                <td>{formatearFecha(producto.fechaVencimiento)}</td>
                <td>
                  {/* El aria-label incluye el nombre porque todas las filas tienen botones con
                      el mismo texto y un lector de pantalla no sabría a qué producto aplican. */}
                  <div className="flex flex-gap-2">
                    <Link
                      className="btn btn-secondary btn-sm"
                      to={`/productos/${producto.idProducto}/editar`}
                      aria-label={`Editar ${producto.nombre}`}
                    >
                      Editar
                    </Link>
                    <button
                      type="button"
                      className="btn btn-danger btn-sm"
                      onClick={() => onEliminar(producto)}
                      aria-label={`Eliminar ${producto.nombre}`}
                    >
                      Eliminar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="form-hint">{productos.length} producto(s) encontrado(s).</p>
    </>
  );
}
