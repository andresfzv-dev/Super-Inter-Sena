// Las reglas y los mensajes replican ProductoRequest (validaciones del backend) y
// ProductoFormulario (conversión de texto a número del módulo JSP), para que el usuario vea en el
// navegador lo mismo que respondería el servidor sin esperar una petición.

const LONGITUD_MAXIMA_CODIGO = 50;
const LONGITUD_MAXIMA_NOMBRE = 150;
const DIGITOS_ENTEROS_PRECIO = 10;
const DIGITOS_DECIMALES_PRECIO = 2;
// Límite de Integer en Java: un stock mayor no se puede convertir en el backend.
const STOCK_MAXIMO = 2147483647;

const PATRON_DECIMAL = /^[+-]?(\d+(\.\d*)?|\.\d+)$/;
const PATRON_ENTERO = /^[+-]?\d+$/;

/** Valores del formulario como texto, porque así los entregan los controles de HTML. */
export const FORMULARIO_VACIO = {
  codigo: '',
  nombre: '',
  precio: '',
  stock: '',
  fechaVencimiento: '',
  idCategoria: '',
};

/** Convierte un ProductoResponse a los valores de texto que muestra el formulario. */
export function desdeProducto(producto) {
  return {
    codigo: producto.codigo,
    nombre: producto.nombre,
    precio: String(producto.precio),
    stock: String(producto.stock),
    fechaVencimiento: producto.fechaVencimiento ?? '',
    idCategoria: String(producto.categoria.idCategoria),
  };
}

// @Digits del backend ignora los ceros a la izquierda de la parte entera y a la derecha de la
// decimal (1.500 tiene un solo decimal significativo), por eso se quitan antes de contar.
function superaDigitosPrecio(precio) {
  const [enteros, decimales = ''] = precio.replace(/^[+-]/, '').split('.');
  return (
    enteros.replace(/^0+/, '').length > DIGITOS_ENTEROS_PRECIO ||
    decimales.replace(/0+$/, '').length > DIGITOS_DECIMALES_PRECIO
  );
}

function validarTextoObligatorio(valor, longitudMaxima, mensajeObligatorio, mensajeLongitud) {
  // El backend recorta los espacios antes de validar, así que aquí se mide el texto recortado.
  const texto = valor.trim();
  if (!texto) {
    return mensajeObligatorio;
  }
  return texto.length > longitudMaxima ? mensajeLongitud : null;
}

function validarPrecio(valor) {
  const precio = valor.trim();
  if (!precio) {
    return 'El precio es obligatorio';
  }
  if (!PATRON_DECIMAL.test(precio)) {
    return 'El precio debe ser un número válido';
  }
  if (Number(precio) <= 0) {
    return 'El precio debe ser mayor que cero';
  }
  return superaDigitosPrecio(precio)
    ? 'El precio admite máximo 10 dígitos enteros y 2 decimales'
    : null;
}

function validarStock(valor) {
  const stock = valor.trim();
  if (!stock) {
    return 'El stock es obligatorio';
  }
  if (!PATRON_ENTERO.test(stock) || Number(stock) > STOCK_MAXIMO) {
    return 'El stock debe ser un número entero';
  }
  return Number(stock) < 0 ? 'El stock no puede ser negativo' : null;
}

/**
 * @returns {Record<string, string>} mensaje por campo; vacío si el formulario es válido
 */
export function validarProducto(valores) {
  const errores = {
    codigo: validarTextoObligatorio(
      valores.codigo,
      LONGITUD_MAXIMA_CODIGO,
      'El código es obligatorio',
      'El código no puede superar 50 caracteres',
    ),
    nombre: validarTextoObligatorio(
      valores.nombre,
      LONGITUD_MAXIMA_NOMBRE,
      'El nombre es obligatorio',
      'El nombre no puede superar 150 caracteres',
    ),
    precio: validarPrecio(valores.precio),
    stock: validarStock(valores.stock),
    idCategoria: valores.idCategoria ? null : 'La categoría es obligatoria',
  };

  return Object.fromEntries(Object.entries(errores).filter(([, mensaje]) => mensaje));
}

/** Convierte valores ya validados al ProductoRequest que espera la API. */
export function aSolicitud(valores) {
  return {
    codigo: valores.codigo.trim(),
    nombre: valores.nombre.trim(),
    precio: Number(valores.precio),
    stock: Number(valores.stock),
    // La API espera null, no texto vacío, cuando el producto no vence.
    fechaVencimiento: valores.fechaVencimiento || null,
    idCategoria: Number(valores.idCategoria),
  };
}
