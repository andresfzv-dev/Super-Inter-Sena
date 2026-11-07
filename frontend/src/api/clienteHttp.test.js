import { afterEach, describe, expect, it, vi } from 'vitest';
import { ErrorApi, solicitar } from './clienteHttp';

const MENSAJE_SIN_CONEXION =
  'No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.';

function simularFetch(respuesta) {
  const fetchSimulado = vi.fn().mockResolvedValue(respuesta);
  vi.stubGlobal('fetch', fetchSimulado);
  return fetchSimulado;
}

function respuestaJson(cuerpo, status, contentType = 'application/json') {
  return new Response(JSON.stringify(cuerpo), { status, headers: { 'Content-Type': contentType } });
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('solicitar', () => {
  it('devuelve el JSON de una respuesta 2xx y solo envía Accept si no hay cuerpo', async () => {
    const fetchSimulado = simularFetch(respuestaJson([{ idProducto: 1 }], 200));

    await expect(solicitar('/api/productos')).resolves.toEqual([{ idProducto: 1 }]);

    expect(fetchSimulado).toHaveBeenCalledWith('/api/productos', {
      method: 'GET',
      headers: { Accept: 'application/json' },
    });
  });

  it('envía el cuerpo como JSON con su Content-Type', async () => {
    const fetchSimulado = simularFetch(respuestaJson({ idProducto: 1 }, 201));

    await solicitar('/api/productos', { metodo: 'POST', cuerpo: { codigo: 'A1' } });

    expect(fetchSimulado).toHaveBeenCalledWith('/api/productos', {
      method: 'POST',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: '{"codigo":"A1"}',
    });
  });

  it('devuelve null en una respuesta 204', async () => {
    simularFetch(new Response(null, { status: 204 }));

    await expect(solicitar('/api/productos/1', { metodo: 'DELETE' })).resolves.toBeNull();
  });

  it('devuelve null cuando una respuesta 2xx no trae JSON', async () => {
    simularFetch(new Response('<html></html>', { status: 200 }));

    await expect(solicitar('/api/productos')).resolves.toBeNull();
  });

  it('lanza ErrorApi con detail y errores de un ProblemDetail 400', async () => {
    simularFetch(
      respuestaJson(
        {
          status: 400,
          title: 'Datos inválidos',
          detail: 'Uno o más campos tienen valores inválidos',
          errores: { codigo: 'El código es obligatorio' },
        },
        400,
        'application/problem+json',
      ),
    );

    const error = await solicitar('/api/productos', { metodo: 'POST', cuerpo: {} }).catch((e) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.estado).toBe(400);
    expect(error.message).toBe('Uno o más campos tienen valores inválidos');
    expect(error.errores).toEqual({ codigo: 'El código es obligatorio' });
  });

  it('deja errores vacío cuando el ProblemDetail no los trae', async () => {
    simularFetch(
      respuestaJson({ status: 409, detail: 'Código duplicado' }, 409, 'application/problem+json'),
    );

    const error = await solicitar('/api/productos').catch((e) => e);

    expect(error.estado).toBe(409);
    expect(error.errores).toEqual({});
  });

  it('lanza ErrorApi con un mensaje genérico si el error no trae cuerpo JSON', async () => {
    simularFetch(new Response('Error', { status: 500 }));

    const error = await solicitar('/api/productos').catch((e) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.estado).toBe(500);
    expect(error.message).toBe('El servidor respondió con un error (500).');
  });

  it('lanza ErrorApi con estado 0 si fetch falla por red', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    const error = await solicitar('/api/productos').catch((e) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.estado).toBe(0);
    expect(error.message).toBe(MENSAJE_SIN_CONEXION);
  });

  it('trata el 502 del proxy de Vite como falta de conexión con el backend', async () => {
    simularFetch(new Response('', { status: 502 }));

    const error = await solicitar('/api/productos').catch((e) => e);

    expect(error.estado).toBe(0);
    expect(error.message).toBe(MENSAJE_SIN_CONEXION);
  });
});
