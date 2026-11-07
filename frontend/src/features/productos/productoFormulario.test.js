import { describe, expect, it } from 'vitest';
import { PRODUCTOS } from '../../test/datosPrueba';
import { aSolicitud, desdeProducto, FORMULARIO_VACIO, validarProducto } from './productoFormulario';

const VALIDO = {
  codigo: 'TEST-100',
  nombre: 'Panela x 1kg',
  precio: '4500',
  stock: '10',
  fechaVencimiento: '',
  idCategoria: '1',
};

const conCampo = (campo, valor) => validarProducto({ ...VALIDO, [campo]: valor });

describe('validarProducto', () => {
  it('no devuelve errores con un formulario válido', () => {
    expect(validarProducto(VALIDO)).toEqual({});
  });

  it('marca todos los campos obligatorios de un formulario vacío', () => {
    expect(validarProducto(FORMULARIO_VACIO)).toEqual({
      codigo: 'El código es obligatorio',
      nombre: 'El nombre es obligatorio',
      precio: 'El precio es obligatorio',
      stock: 'El stock es obligatorio',
      idCategoria: 'La categoría es obligatoria',
    });
  });

  it('trata un texto de solo espacios como vacío, igual que el backend', () => {
    expect(conCampo('codigo', '   ')).toEqual({ codigo: 'El código es obligatorio' });
  });

  it('rechaza un código de más de 50 caracteres y acepta 50', () => {
    expect(conCampo('codigo', 'A'.repeat(51))).toEqual({
      codigo: 'El código no puede superar 50 caracteres',
    });
    expect(conCampo('codigo', 'A'.repeat(50))).toEqual({});
  });

  it('mide la longitud sin los espacios de los extremos', () => {
    expect(conCampo('codigo', ` ${'A'.repeat(50)} `)).toEqual({});
  });

  it('rechaza un nombre de más de 150 caracteres y acepta 150', () => {
    expect(conCampo('nombre', 'N'.repeat(151))).toEqual({
      nombre: 'El nombre no puede superar 150 caracteres',
    });
    expect(conCampo('nombre', 'N'.repeat(150))).toEqual({});
  });

  it.each(['0', '-5'])('rechaza el precio %s porque debe ser mayor que cero', (precio) => {
    expect(conCampo('precio', precio)).toEqual({ precio: 'El precio debe ser mayor que cero' });
  });

  it.each([
    ['tres decimales', '10.125'],
    ['once dígitos enteros', '12345678901'],
  ])('rechaza un precio con %s', (_caso, precio) => {
    expect(conCampo('precio', precio)).toEqual({
      precio: 'El precio admite máximo 10 dígitos enteros y 2 decimales',
    });
  });

  it.each(['1234567890.99', '1.500', '0.01'])('acepta el precio límite %s', (precio) => {
    expect(conCampo('precio', precio)).toEqual({});
  });

  it('rechaza un precio que no es un número', () => {
    expect(conCampo('precio', 'abc')).toEqual({ precio: 'El precio debe ser un número válido' });
  });

  it('rechaza un stock negativo y acepta cero', () => {
    expect(conCampo('stock', '-1')).toEqual({ stock: 'El stock no puede ser negativo' });
    expect(conCampo('stock', '0')).toEqual({});
  });

  it.each(['2.5', '2147483648'])('rechaza el stock %s porque no es un entero válido', (stock) => {
    expect(conCampo('stock', stock)).toEqual({ stock: 'El stock debe ser un número entero' });
  });
});

describe('aSolicitud', () => {
  it('convierte los textos a los tipos de ProductoRequest y recorta los espacios', () => {
    expect(
      aSolicitud({
        ...VALIDO,
        codigo: ' TEST-100 ',
        nombre: ' Panela x 1kg ',
        precio: '4500.50',
        fechaVencimiento: '2026-06-30',
      }),
    ).toEqual({
      codigo: 'TEST-100',
      nombre: 'Panela x 1kg',
      precio: 4500.5,
      stock: 10,
      fechaVencimiento: '2026-06-30',
      idCategoria: 1,
    });
  });

  it('envía null cuando no hay fecha de vencimiento', () => {
    expect(aSolicitud(VALIDO).fechaVencimiento).toBeNull();
  });
});

describe('desdeProducto', () => {
  it('convierte un ProductoResponse a los textos del formulario', () => {
    expect(desdeProducto(PRODUCTOS[0])).toEqual({
      codigo: 'TEST-001',
      nombre: 'Arroz Diana x 500g',
      precio: '3500',
      stock: '20',
      fechaVencimiento: '2026-12-31',
      idCategoria: '1',
    });
  });

  it('deja la fecha vacía cuando el producto no vence', () => {
    expect(desdeProducto(PRODUCTOS[1]).fechaVencimiento).toBe('');
  });
});
