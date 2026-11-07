import { solicitar } from './clienteHttp';

const RUTA_PRODUCTOS = '/api/productos';

export const productosApi = {
  listar: () => solicitar(RUTA_PRODUCTOS),
  obtener: (idProducto) => solicitar(`${RUTA_PRODUCTOS}/${idProducto}`),
  registrar: (solicitud) => solicitar(RUTA_PRODUCTOS, { metodo: 'POST', cuerpo: solicitud }),
  actualizar: (idProducto, solicitud) =>
    solicitar(`${RUTA_PRODUCTOS}/${idProducto}`, { metodo: 'PUT', cuerpo: solicitud }),
  eliminar: (idProducto) => solicitar(`${RUTA_PRODUCTOS}/${idProducto}`, { metodo: 'DELETE' }),
};
