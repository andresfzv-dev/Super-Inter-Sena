import { Link } from 'react-router';
import Alerta from '../../../components/ui/Alerta';
import CampoFormulario from '../../../components/ui/CampoFormulario';

/**
 * Formulario controlado: no guarda estado propio para que la página decida cuándo validar,
 * enviar y mostrar los errores, tanto en registro como en edición.
 *
 * noValidate desactiva la validación nativa del navegador, que muestra mensajes distintos a los
 * del backend y en el idioma del navegador.
 */
export default function FormularioProducto({
  valores,
  errores,
  errorGeneral,
  categorias,
  enviando,
  textoBoton,
  onCambiar,
  onEnviar,
}) {
  const alCambiar = (evento) => onCambiar(evento.target.name, evento.target.value);
  const hayErroresCampos = Object.keys(errores).length > 0;

  return (
    <form className="card" onSubmit={onEnviar} noValidate>
      <div className="card__body">
        {errorGeneral && <Alerta tipo="danger">{errorGeneral}</Alerta>}
        {hayErroresCampos && <Alerta tipo="warning">Revisa los campos marcados.</Alerta>}

        <div className="form-row">
          <CampoFormulario
            id="codigo"
            name="codigo"
            etiqueta="Código"
            requerido
            maxLength={50}
            value={valores.codigo}
            onChange={alCambiar}
            placeholder="Ej: 7701234567890"
            error={errores.codigo}
          />
          <CampoFormulario
            id="nombre"
            name="nombre"
            etiqueta="Nombre del producto"
            requerido
            maxLength={150}
            value={valores.nombre}
            onChange={alCambiar}
            placeholder="Ej: Arroz Diana x 500g"
            error={errores.nombre}
          />
        </div>

        <div className="form-row">
          <CampoFormulario
            id="idCategoria"
            name="idCategoria"
            etiqueta="Categoría"
            requerido
            control="select"
            value={valores.idCategoria}
            onChange={alCambiar}
            error={errores.idCategoria}
          >
            <option value="">Seleccione una categoría</option>
            {categorias.map((categoria) => (
              <option key={categoria.idCategoria} value={categoria.idCategoria}>
                {categoria.nombre}
              </option>
            ))}
          </CampoFormulario>
          <CampoFormulario
            id="fechaVencimiento"
            name="fechaVencimiento"
            etiqueta="Fecha de vencimiento"
            type="date"
            value={valores.fechaVencimiento}
            onChange={alCambiar}
            error={errores.fechaVencimiento}
            ayuda="Déjala vacía si el producto no vence."
          />
        </div>

        <div className="form-row">
          <CampoFormulario
            id="precio"
            name="precio"
            etiqueta="Precio de venta (COP)"
            requerido
            type="number"
            min="0.01"
            step="0.01"
            value={valores.precio}
            onChange={alCambiar}
            placeholder="0"
            error={errores.precio}
          />
          <CampoFormulario
            id="stock"
            name="stock"
            etiqueta="Stock"
            requerido
            type="number"
            min="0"
            step="1"
            value={valores.stock}
            onChange={alCambiar}
            placeholder="0"
            error={errores.stock}
          />
        </div>
      </div>
      <div className="card__footer">
        <Link to="/productos" className="btn btn-secondary">
          Cancelar
        </Link>
        <button type="submit" className="btn btn-primary" disabled={enviando}>
          {textoBoton}
        </button>
      </div>
    </form>
  );
}
