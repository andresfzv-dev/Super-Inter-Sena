import { describe, expect, it } from 'vitest';
import { formatearFecha, formatearPrecio } from './formato';

// Intl separa el símbolo y el valor con un espacio de no separación, no con uno normal.
const normalizarEspacios = (texto) => texto.replace(/\s/g, ' ');

describe('formatearPrecio', () => {
  it('muestra pesos colombianos con separador de miles y sin decimales', () => {
    expect(normalizarEspacios(formatearPrecio(1500000))).toBe('$ 1.500.000');
  });

  it('redondea los decimales porque COP se muestra sin ellos', () => {
    expect(normalizarEspacios(formatearPrecio(2500.5))).toBe('$ 2.501');
  });
});

describe('formatearFecha', () => {
  it('convierte aaaa-mm-dd a dd/mm/aaaa sin desfase por zona horaria', () => {
    expect(formatearFecha('2026-01-01')).toBe('01/01/2026');
  });

  it.each([[null], [undefined], ['']])('muestra una raya si la fecha es %s', (fecha) => {
    expect(formatearFecha(fecha)).toBe('—');
  });
});
