import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { categoriasApi } from '../../../api/categoriasApi';
import { productosApi } from '../../../api/productosApi';
import Alerta from '../../../components/ui/Alerta';
import { useTituloPagina } from '../../../hooks/useTituloPagina';
import FormularioProducto from '../components/FormularioProducto';
import {
  aSolicitud,
  desdeProducto,
  FORMULARIO_VACIO,
  validarProducto,
} from '../productoFormulario';

// Límite de Integer en Java, el tipo del id en el backend.
const ID_MAXIMO = 2147483647;

// El backend responde un 400 con un mensaje técnico en inglés cuando el id de la ruta no se puede
// convertir a Integer, así que ese caso se resuelve aquí como un producto inexistente.
function esIdValido(idProducto) {
  return /^\d+$/.test(idProducto) && Number(idProducto) >= 1 && Number(idProducto) <= ID_MAXIMO;
}

/** Registro (/productos/nuevo) y edición (/productos/:idProducto/editar) con la misma pantalla. */
export default function ProductoFormularioPage() {
  const { idProducto } = useParams();
  const esEdicion = idProducto !== undefined;
  const idInvalido = esEdicion && !esIdValido(idProducto);
  const titulo = esEdicion ? 'Editar producto' : 'Registrar producto';
  useTituloPagina(titulo);
  const navigate = useNavigate();

  const [valores, setValores] = useState(FORMULARIO_VACIO);
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (idInvalido) {
      return undefined;
    }
    // Evita actualizar el estado si el usuario salió de la página antes de recibir la respuesta.
    let activo = true;

    Promise.all([
      categoriasApi.listar(),
      esEdicion ? productosApi.obtener(idProducto) : Promise.resolve(null),
    ])
      .then(([listaCategorias, producto]) => {
        if (activo) {
          setCategorias(listaCategorias);
          if (producto) {
            setValores(desdeProducto(producto));
          }
        }
      })
      .catch((error) => {
        if (activo) {
          setErrorCarga(error.message);
        }
      })
      .finally(() => {
        if (activo) {
          setCargando(false);
        }
      });

    return () => {
      activo = false;
    };
  }, [esEdicion, idInvalido, idProducto]);

  function cambiarCampo(campo, valor) {
    setValores((actuales) => ({ ...actuales, [campo]: valor }));
    // El error se quita al modificar el campo porque ya no describe el valor que se ve.
    setErrores(({ [campo]: _quitado, ...restantes }) => restantes);
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErrorGeneral(null);

    const erroresCliente = validarProducto(valores);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      return;
    }

    setEnviando(true);
    try {
      const solicitud = aSolicitud(valores);
      if (esEdicion) {
        await productosApi.actualizar(idProducto, solicitud);
      } else {
        await productosApi.registrar(solicitud);
      }
      // Mismos textos que ProductoListaServlet en el módulo JSP.
      const mensaje = esEdicion
        ? 'Producto actualizado correctamente.'
        : 'Producto registrado correctamente.';
      navigate('/productos', { state: { mensaje } });
    } catch (error) {
      // Un 400 con errores por campo se muestra junto a cada control; cualquier otro error
      // (409 de código duplicado, 404, falta de conexión) no pertenece a un campo.
      const erroresServidor = error.errores ?? {};
      if (error.estado === 400 && Object.keys(erroresServidor).length > 0) {
        setErrores(erroresServidor);
      } else {
        setErrorGeneral(error.message);
      }
      setEnviando(false);
    }
  }

  // Mismo formato que el 404 del backend, para que el usuario vea un único tipo de mensaje.
  const mensajeErrorCarga = idInvalido ? `Producto con id ${idProducto} no existe` : errorCarga;

  return (
    <>
      <div className="page-heading">
        <div>
          <h2>{titulo}</h2>
          <p>
            {esEdicion
              ? 'Modifica la información del producto.'
              : 'Completa la información del producto para agregarlo al catálogo.'}
          </p>
        </div>
        <Link to="/productos" className="btn btn-secondary">
          &larr; Volver al listado
        </Link>
      </div>

      {cargando && !mensajeErrorCarga && <p className="form-hint">Cargando...</p>}
      {mensajeErrorCarga && <Alerta tipo="danger">{mensajeErrorCarga}</Alerta>}
      {!cargando && !mensajeErrorCarga && (
        <FormularioProducto
          valores={valores}
          errores={errores}
          errorGeneral={errorGeneral}
          categorias={categorias}
          enviando={enviando}
          textoBoton={esEdicion ? 'Guardar cambios' : 'Guardar producto'}
          onCambiar={cambiarCampo}
          onEnviar={enviar}
        />
      )}
    </>
  );
}
