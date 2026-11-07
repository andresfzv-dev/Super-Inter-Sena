// Los errores y advertencias interrumpen al lector de pantalla; las confirmaciones esperan su turno.
const ROL_POR_TIPO = {
  success: 'status',
  info: 'status',
  warning: 'alert',
  danger: 'alert',
};

/** @param {{ tipo: 'success' | 'info' | 'warning' | 'danger', children: React.ReactNode }} props */
export default function Alerta({ tipo, children }) {
  return (
    <div className={`alert alert-${tipo}`} role={ROL_POR_TIPO[tipo]}>
      {children}
    </div>
  );
}
