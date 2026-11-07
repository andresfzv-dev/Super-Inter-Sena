import { solicitar } from './clienteHttp';

export const categoriasApi = {
  listar: () => solicitar('/api/categorias'),
};
