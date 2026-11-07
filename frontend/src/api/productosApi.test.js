import { beforeEach, describe, expect, it, vi } from 'vitest';
import { categoriasApi } from './categoriasApi';
import { solicitar } from './clienteHttp';
import { productosApi } from './productosApi';

vi.mock('./clienteHttp', () => ({ solicitar: vi.fn() }));

const SOLICITUD = { codigo: 'A1' };

beforeEach(() => {
  vi.mocked(solicitar).mockReset();
});

describe('servicios de la API', () => {
  it.each([
    ['listar productos', () => productosApi.listar(), ['/api/productos']],
    ['obtener un producto', () => productosApi.obtener(7), ['/api/productos/7']],
    [
      'registrar un producto',
      () => productosApi.registrar(SOLICITUD),
      ['/api/productos', { metodo: 'POST', cuerpo: SOLICITUD }],
    ],
    [
      'actualizar un producto',
      () => productosApi.actualizar(7, SOLICITUD),
      ['/api/productos/7', { metodo: 'PUT', cuerpo: SOLICITUD }],
    ],
    [
      'eliminar un producto',
      () => productosApi.eliminar(7),
      ['/api/productos/7', { metodo: 'DELETE' }],
    ],
    ['listar categorías', () => categoriasApi.listar(), ['/api/categorias']],
  ])('%s usa la ruta y el método del contrato', (_nombre, llamar, argumentos) => {
    llamar();

    expect(solicitar).toHaveBeenCalledWith(...argumentos);
  });
});
