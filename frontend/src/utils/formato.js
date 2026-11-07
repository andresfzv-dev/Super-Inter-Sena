const FORMATO_PESOS = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

const SIN_VALOR = '—';

export function formatearPrecio(precio) {
  return FORMATO_PESOS.format(precio);
}

/**
 * Convierte aaaa-mm-dd a dd/mm/aaaa manipulando el texto. No se usa new Date(fecha) porque lo
 * interpreta como medianoche UTC y en Colombia (UTC-5) mostraría el día anterior.
 */
export function formatearFecha(fecha) {
  if (!fecha) {
    return SIN_VALOR;
  }
  const [anio, mes, dia] = fecha.split('-');
  return `${dia}/${mes}/${anio}`;
}
