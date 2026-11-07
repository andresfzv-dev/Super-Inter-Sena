import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { categoriasApi } from '../../../api/categoriasApi';
import { productosApi } from '../../../api/productosApi';
import Alerta from '../../../components/ui/Alerta';
import ModalConfirmacion from '../../../components/ui/ModalConfirmacion';
import { useTituloPagina } from '../../../hooks/useTituloPagina';
import DetalleProducto from '../components/DetalleProducto';
import FiltrosProductos from '../components/FiltrosProductos';
import TablaProductos from '../components/TablaProductos';
import { filtrarProductos } from '../filtrarProductos';

const FILTROS_VACIOS = { nombre: '', idCategoria: '' };

export default function ProductoListaPage() {
  useTituloPagina('Productos');
  const { pathname, state } = useLocation();
  const navigate = useNavigate();

  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [filtros, setFiltros] = useState(FILTROS_VACIOS);
  // El mensaje llega del formulario por el state de la navegación; se copia al estado local
  // porque el efecto de abajo borra ese state.
  const [mensaje, setMensaje] = useState(state?.mensaje ?? null);
  const [productoAEliminar, setProductoAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);
  const [errorEliminar, setErrorEliminar] = useState(null);

  useEffect(() => {
    // El navegador conserva el state de la entrada del historial al recargar; sin borrarlo,
    // el mensaje de éxito volvería a aparecer.
    if (state?.mensaje) {
      navigate(pathname, { replace: true, state: null });
    }
  }, [navigate, pathname, state]);

  useEffect(() => {
    // Evita actualizar el estado si el usuario salió de la página antes de recibir la respuesta.
    let activo = true;

    Promise.all([productosApi.listar(), categoriasApi.listar()])
      .then(([listaProductos, listaCategorias]) => {
        if (activo) {
          setProductos(listaProductos);
          setCategorias(listaCategorias);
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
  }, []);

  // useCallback porque ModalConfirmacion usa esta función en las dependencias de un efecto.
  const cerrarModal = useCallback(() => {
    setProductoAEliminar(null);
    setErrorEliminar(null);
  }, []);

  async function confirmarEliminacion() {
    setEliminando(true);
    setMensaje(null);
    try {
      await productosApi.eliminar(productoAEliminar.idProducto);
      setProductos((anteriores) =>
        anteriores.filter((producto) => producto.idProducto !== productoAEliminar.idProducto),
      );
      setMensaje('Producto eliminado correctamente.');
      cerrarModal();
    } catch (error) {
      setErrorEliminar(error.message);
    } finally {
      setEliminando(false);
    }
  }

  const productosFiltrados = filtrarProductos(productos, filtros);

  return (
    <>
      <div className="page-heading">
        <div>
          <h2>Gestión de productos</h2>
          <p>Consulta, busca y administra el catálogo de productos.</p>
        </div>
        <Link to="/productos/nuevo" className="btn btn-primary">
          + Registrar producto
        </Link>
      </div>

      {mensaje && <Alerta tipo="success">{mensaje}</Alerta>}

      <div className="card">
        <div className="card__body">
          {errorCarga ? (
            <Alerta tipo="danger">{errorCarga}</Alerta>
          ) : (
            <>
              <FiltrosProductos
                nombre={filtros.nombre}
                idCategoria={filtros.idCategoria}
                categorias={categorias}
                onCambiarNombre={(nombre) => setFiltros((actuales) => ({ ...actuales, nombre }))}
                onCambiarCategoria={(idCategoria) =>
                  setFiltros((actuales) => ({ ...actuales, idCategoria }))
                }
                onLimpiar={() => setFiltros(FILTROS_VACIOS)}
              />
              {cargando ? (
                <p className="form-hint">Cargando productos...</p>
              ) : (
                <TablaProductos productos={productosFiltrados} onEliminar={setProductoAEliminar} />
              )}
            </>
          )}
        </div>
      </div>

      {productoAEliminar && (
        <ModalConfirmacion
          titulo="Eliminar producto"
          textoConfirmar="Sí, eliminar producto"
          onConfirmar={confirmarEliminacion}
          onCancelar={cerrarModal}
          procesando={eliminando}
          error={errorEliminar}
        >
          {!errorEliminar && <Alerta tipo="warning">Esta acción no se puede deshacer.</Alerta>}
          <DetalleProducto producto={productoAEliminar} />
        </ModalConfirmacion>
      )}
    </>
  );
}
