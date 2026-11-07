/**
 * Etiqueta, control y mensaje de error de un campo. Las props no declaradas (value, onChange,
 * maxLength...) pasan directo al control, que por defecto es un input.
 */
export default function CampoFormulario({
  id,
  etiqueta,
  error,
  ayuda,
  requerido = false,
  control: Control = 'input',
  children,
  ...propsControl
}) {
  const idError = `${id}-error`;

  return (
    // Sin has-error, el CSS del prototipo oculta .form-error aunque tenga contenido.
    <div className={`form-group${error ? ' has-error' : ''}`}>
      <label className="form-label" htmlFor={id}>
        {etiqueta}
        {requerido && <span className="required">*</span>}
      </label>
      <Control
        id={id}
        className="form-control"
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? idError : undefined}
        {...propsControl}
      >
        {children}
      </Control>
      {error && (
        <p className="form-error" id={idError}>
          {error}
        </p>
      )}
      {ayuda && <p className="form-hint">{ayuda}</p>}
    </div>
  );
}
