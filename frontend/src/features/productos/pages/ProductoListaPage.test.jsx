import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { categoriasApi } from '../../../api/categoriasApi';
import { ErrorApi } from '../../../api/clienteHttp';
import { productosApi } from '../../../api/productosApi';
import { CATEGORIAS, PRODUCTOS } from '../../../test/datosPrueba';
import ProductoListaPage from './ProductoListaPage';

// Se reemplazan los servicios para probar la pantalla sin el backend.
vi.mock('../../../api/productosApi', () => ({
  productosApi: { listar: vi.fn(), eliminar: vi.fn() },
}));
vi.mock('../../../api/categoriasApi', () => ({
  categoriasApi: { listar: vi.fn() },
}));

const MENSAJE_SIN_CONEXION =
  'No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.';

// Muestra el state de la ubicación actual para comprobar que la página lo borra. Es un <p> y no un
// <output> porque este tiene rol status y se confundiría con la alerta de éxito.
function EstadoUbicacion() {
  const { state } = useLocation();
  return <p data-testid="estado-ubicacion">{JSON.stringify(state)}</p>;
}

function renderizar(entrada = '/productos') {
  return render(
    <MemoryRouter initialEntries={[entrada]}>
      <Routes>
        <Route
          path="/productos"
          element={
            <>
              <ProductoListaPage />
              <EstadoUbicacion />
            </>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

async function abrirEliminacion(usuario, nombre) {
  await usuario.click(await screen.findByRole('button', { name: `Eliminar ${nombre}` }));
  return screen.getByRole('dialog', { name: 'Eliminar producto' });
}

beforeEach(() => {
  vi.mocked(productosApi.listar).mockResolvedValue(PRODUCTOS);
  vi.mocked(categoriasApi.listar).mockResolvedValue(CATEGORIAS);
  vi.mocked(productosApi.eliminar).mockReset();
});

describe('ProductoListaPage', () => {
  it('muestra los productos con su formato, el conteo y las categorías del filtro', async () => {
    renderizar();

    expect(await screen.findByText('Arroz Diana x 500g')).toBeInTheDocument();
    const filaLeche = screen.getByText('Leche Alquería x 1L').closest('tr');
    expect(within(filaLeche).getByText('Agotado')).toBeInTheDocument();
    expect(within(filaLeche).getByText('—')).toBeInTheDocument();
    expect(screen.getByText('31/12/2026')).toBeInTheDocument();
    expect(screen.getByText('3 producto(s) encontrado(s).')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Lácteos' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Editar Arroz Diana x 500g' })).toHaveAttribute(
      'href',
      '/productos/1/editar',
    );
  });

  it('filtra mientras se escribe y vuelve a mostrar todo con Limpiar', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Arroz Diana x 500g');

    await usuario.type(
      screen.getByRole('searchbox', { name: 'Buscar producto por nombre' }),
      'LECHE',
    );

    expect(screen.getByText('Leche Alquería x 1L')).toBeInTheDocument();
    expect(screen.queryByText('Arroz Diana x 500g')).not.toBeInTheDocument();
    expect(screen.getByText('1 producto(s) encontrado(s).')).toBeInTheDocument();

    await usuario.selectOptions(
      screen.getByRole('combobox', { name: 'Filtrar por categoría' }),
      '1',
    );
    expect(screen.getByText('No se encontraron productos.')).toBeInTheDocument();

    await usuario.click(screen.getByRole('button', { name: 'Limpiar' }));
    expect(screen.getByText('3 producto(s) encontrado(s).')).toBeInTheDocument();
    expect(screen.getByRole('searchbox')).toHaveValue('');
  });

  it('muestra el estado vacío cuando no hay productos', async () => {
    vi.mocked(productosApi.listar).mockResolvedValue([]);
    renderizar();

    expect(await screen.findByText('No se encontraron productos.')).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('muestra el mensaje recibido por state y lo borra del historial', async () => {
    renderizar({
      pathname: '/productos',
      state: { mensaje: 'Producto registrado correctamente.' },
    });

    expect(screen.getByRole('status')).toHaveTextContent('Producto registrado correctamente.');
    expect(await screen.findByTestId('estado-ubicacion')).toHaveTextContent('null');
    expect(screen.getByRole('status')).toHaveTextContent('Producto registrado correctamente.');
  });

  it('muestra el error de conexión si no puede cargar los datos', async () => {
    vi.mocked(productosApi.listar).mockRejectedValue(new ErrorApi(0, MENSAJE_SIN_CONEXION));
    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent(MENSAJE_SIN_CONEXION);
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('elimina el producto después de confirmar', async () => {
    const usuario = userEvent.setup();
    vi.mocked(productosApi.eliminar).mockResolvedValue(null);
    renderizar();

    const ventana = await abrirEliminacion(usuario, 'Arroz Diana x 500g');
    expect(within(ventana).getByText('TEST-001')).toBeInTheDocument();
    expect(within(ventana).getByRole('button', { name: 'Cancelar' })).toHaveFocus();

    await usuario.click(within(ventana).getByRole('button', { name: 'Sí, eliminar producto' }));

    expect(productosApi.eliminar).toHaveBeenCalledWith(1);
    expect(await screen.findByRole('status')).toHaveTextContent(
      'Producto eliminado correctamente.',
    );
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.queryByText('Arroz Diana x 500g')).not.toBeInTheDocument();
    expect(screen.getByText('2 producto(s) encontrado(s).')).toBeInTheDocument();
  });

  it('deshabilita los botones mientras se elimina', async () => {
    const usuario = userEvent.setup();
    let terminarEliminacion;
    vi.mocked(productosApi.eliminar).mockReturnValue(
      new Promise((resolver) => {
        terminarEliminacion = resolver;
      }),
    );
    renderizar();

    const ventana = await abrirEliminacion(usuario, 'Arroz Diana x 500g');
    await usuario.click(within(ventana).getByRole('button', { name: 'Sí, eliminar producto' }));

    expect(within(ventana).getByRole('button', { name: 'Cancelar' })).toBeDisabled();
    expect(within(ventana).getByRole('button', { name: 'Sí, eliminar producto' })).toBeDisabled();

    terminarEliminacion(null);
    expect(await screen.findByText('Producto eliminado correctamente.')).toBeInTheDocument();
  });

  it.each([
    [
      'el botón Cancelar',
      (usuario, ventana) => usuario.click(within(ventana).getByText('Cancelar')),
    ],
    ['la tecla Escape', (usuario) => usuario.keyboard('{Escape}')],
  ])('cancela sin eliminar con %s', async (_forma, cancelar) => {
    const usuario = userEvent.setup();
    renderizar();

    const ventana = await abrirEliminacion(usuario, 'Arroz Diana x 500g');
    await cancelar(usuario, ventana);

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(productosApi.eliminar).not.toHaveBeenCalled();
    expect(screen.getByText('Arroz Diana x 500g')).toBeInTheDocument();
  });

  it('muestra el 409 en la ventana y oculta el botón de confirmar', async () => {
    const usuario = userEvent.setup();
    const detalle = 'No se puede eliminar el producto porque tiene ventas registradas';
    vi.mocked(productosApi.eliminar).mockRejectedValue(new ErrorApi(409, detalle));
    renderizar();

    const ventana = await abrirEliminacion(usuario, 'Arroz Diana x 500g');
    await usuario.click(within(ventana).getByRole('button', { name: 'Sí, eliminar producto' }));

    // Se busca por texto porque, antes del error, la ventana ya tiene la alerta de advertencia.
    expect(await within(ventana).findByText(detalle)).toHaveAttribute('role', 'alert');
    expect(
      within(ventana).queryByText('Esta acción no se puede deshacer.'),
    ).not.toBeInTheDocument();
    expect(
      within(ventana).queryByRole('button', { name: 'Sí, eliminar producto' }),
    ).not.toBeInTheDocument();
    expect(within(ventana).getByRole('button', { name: 'Cancelar' })).toHaveFocus();
    expect(screen.getByText('3 producto(s) encontrado(s).')).toBeInTheDocument();
  });
});
