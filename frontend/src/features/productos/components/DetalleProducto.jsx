import { formatearFecha, formatearPrecio } from '../../../utils/formato';

/** Datos del producto que se muestran antes de confirmar su eliminación. */
export default function DetalleProducto({ producto }) {
  const datos = [
    ['Código', producto.codigo],
    ['Nombre', producto.nombre],
    ['Categoría', producto.categoria.nombre],
    ['Precio', formatearPrecio(producto.precio)],
    ['Stock', producto.stock],
    ['Vencimiento', formatearFecha(producto.fechaVencimiento)],
  ];

  return (
    <dl className="form-row">
      {datos.map(([etiqueta, valor]) => (
        <div key={etiqueta}>
          <dt className="form-label">{etiqueta}</dt>
          <dd>{valor}</dd>
        </div>
      ))}
    </dl>
  );
}
