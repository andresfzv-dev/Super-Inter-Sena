const MENSAJE_SIN_CONEXION =
  'No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.';

// El backend nunca responde estos estados: los genera un intermediario que no logró comunicarse
// con él. Con el backend apagado, fetch no falla porque el proxy de Vite sí contesta, con un 502.
const ESTADOS_SIN_BACKEND = new Set([502, 503, 504]);

/** Error de una petición a la API, con los datos que las pantallas necesitan para mostrarlo. */
export class ErrorApi extends Error {
  /**
   * @param {number} estado código HTTP de la respuesta, o 0 si no hubo comunicación con el backend
   * @param {string} mensaje texto para mostrar al usuario
   * @param {Record<string, string>} errores mensajes por campo, presentes en las respuestas 400
   */
  constructor(estado, mensaje, errores = {}) {
    super(mensaje);
    this.name = 'ErrorApi';
    this.estado = estado;
    this.errores = errores;
  }
}

// Se interpreta el texto en lugar de confiar en Content-Type porque los errores llegan como
// application/problem+json, y un cuerpo vacío o no JSON no debe ocultar el estado de la respuesta.
function interpretarCuerpo(texto) {
  try {
    return JSON.parse(texto);
  } catch {
    return null;
  }
}

/**
 * Único punto de salida hacia la API: unifica los encabezados y la conversión de errores.
 *
 * @param {string} ruta ruta relativa, por ejemplo /api/productos
 * @param {{ metodo?: string, cuerpo?: unknown }} [opciones]
 * @returns {Promise<any>} el JSON de la respuesta, o null si no trae JSON
 * @throws {ErrorApi} si no hay comunicación con el backend o la respuesta no es 2xx
 */
export async function solicitar(ruta, { metodo = 'GET', cuerpo } = {}) {
  const hayCuerpo = cuerpo !== undefined;
  let respuesta;

  try {
    respuesta = await fetch(ruta, {
      method: metodo,
      headers: {
        Accept: 'application/json',
        ...(hayCuerpo && { 'Content-Type': 'application/json' }),
      },
      ...(hayCuerpo && { body: JSON.stringify(cuerpo) }),
    });
  } catch {
    throw new ErrorApi(0, MENSAJE_SIN_CONEXION);
  }

  if (ESTADOS_SIN_BACKEND.has(respuesta.status)) {
    throw new ErrorApi(0, MENSAJE_SIN_CONEXION);
  }

  const datos = respuesta.status === 204 ? null : interpretarCuerpo(await respuesta.text());

  if (!respuesta.ok) {
    throw new ErrorApi(
      respuesta.status,
      datos?.detail ?? `El servidor respondió con un error (${respuesta.status}).`,
      datos?.errores,
    );
  }

  return datos;
}
