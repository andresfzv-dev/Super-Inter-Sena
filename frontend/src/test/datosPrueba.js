// Datos con la forma de las respuestas de la API (ProductoResponse y categorías), compartidos por
// las pruebas para que todas trabajen sobre el mismo inventario.

export const CATEGORIAS = [
  { idCategoria: 1, nombre: 'Granos' },
  { idCategoria: 2, nombre: 'Lácteos' },
];

export const PRODUCTOS = [
  {
    idProducto: 1,
    codigo: 'TEST-001',
    nombre: 'Arroz Diana x 500g',
    precio: 3500,
    stock: 20,
    fechaVencimiento: '2026-12-31',
    categoria: CATEGORIAS[0],
  },
  {
    idProducto: 2,
    codigo: 'TEST-002',
    nombre: 'Leche Alquería x 1L',
    precio: 4200,
    stock: 0,
    fechaVencimiento: null,
    categoria: CATEGORIAS[1],
  },
  {
    idProducto: 3,
    codigo: 'TEST-003',
    nombre: 'Frijol Cargamanto x 500g',
    precio: 8900.5,
    stock: 7,
    fechaVencimiento: '2027-03-15',
    categoria: CATEGORIAS[0],
  },
];
