import { describe, expect, it } from 'vitest';
import { PRODUCTOS } from '../../test/datosPrueba';
import { filtrarProductos } from './filtrarProductos';

const codigos = (productos) => productos.map((producto) => producto.codigo);

describe('filtrarProductos', () => {
  it('devuelve todos los productos sin filtros', () => {
    expect(filtrarProductos(PRODUCTOS, { nombre: '', idCategoria: '' })).toEqual(PRODUCTOS);
  });

  it('busca por nombre sin distinguir mayúsculas e ignora los espacios de los extremos', () => {
    expect(codigos(filtrarProductos(PRODUCTOS, { nombre: '  ARROZ ', idCategoria: '' }))).toEqual([
      'TEST-001',
    ]);
  });

  it('filtra por categoría', () => {
    expect(codigos(filtrarProductos(PRODUCTOS, { nombre: '', idCategoria: '1' }))).toEqual([
      'TEST-001',
      'TEST-003',
    ]);
  });

  it('combina el nombre y la categoría', () => {
    expect(codigos(filtrarProductos(PRODUCTOS, { nombre: 'x 500g', idCategoria: '1' }))).toEqual([
      'TEST-001',
      'TEST-003',
    ]);
    expect(filtrarProductos(PRODUCTOS, { nombre: 'leche', idCategoria: '1' })).toEqual([]);
  });
});
