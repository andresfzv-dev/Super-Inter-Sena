import { useEffect, useId, useRef } from 'react';
import Alerta from './Alerta';

/**
 * Ventana de confirmación. El padre la monta solo cuando debe verse y debe pasar onCancelar en
 * useCallback: si cambiara en cada render, el efecto de Escape se registraría de nuevo cada vez.
 *
 * Si recibe un error oculta el botón de confirmar, porque repetir la acción daría el mismo
 * resultado (por ejemplo, un producto con ventas no se puede eliminar).
 */
export default function ModalConfirmacion({
  titulo,
  textoConfirmar,
  onConfirmar,
  onCancelar,
  procesando = false,
  error = null,
  children,
}) {
  const idTitulo = useId();
  const botonCancelar = useRef(null);

  // Depende de procesando porque el botón pierde el foco al deshabilitarse, y debe recuperarlo
  // cuando la acción falla y la ventana sigue abierta.
  useEffect(() => {
    if (!procesando) {
      botonCancelar.current.focus();
    }
  }, [procesando]);

  useEffect(() => {
    // No se cierra mientras se procesa: el usuario perdería el resultado de la petición en curso.
    function alPresionarTecla(evento) {
      if (evento.key === 'Escape' && !procesando) {
        onCancelar();
      }
    }

    document.addEventListener('keydown', alPresionarTecla);
    return () => document.removeEventListener('keydown', alPresionarTecla);
  }, [onCancelar, procesando]);

  return (
    <div className="modal-backdrop is-open">
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby={idTitulo}>
        <div className="modal__header">
          <h2 className="mb-0" id={idTitulo}>
            {titulo}
          </h2>
        </div>
        <div className="modal__body">
          {error && <Alerta tipo="danger">{error}</Alerta>}
          {children}
        </div>
        <div className="modal__footer">
          <button
            type="button"
            className="btn btn-secondary"
            ref={botonCancelar}
            onClick={onCancelar}
            disabled={procesando}
          >
            Cancelar
          </button>
          {!error && (
            <button
              type="button"
              className="btn btn-danger"
              onClick={onConfirmar}
              disabled={procesando}
            >
              {textoConfirmar}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
