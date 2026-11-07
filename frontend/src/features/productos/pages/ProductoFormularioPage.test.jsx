import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { categoriasApi } from '../../../api/categoriasApi';
import { ErrorApi } from '../../../api/clienteHttp';
import { productosApi } from '../../../api/productosApi';
import { CATEGORIAS, PRODUCTOS } from '../../../test/datosPrueba';
import ProductoFormularioPage from './ProductoFormularioPage';

// Se reemplazan los servicios para probar la pantalla sin el backend.
vi.mock('../../../api/productosApi', () => ({
  productosApi: { obtener: vi.fn(), registrar: vi.fn(), actualizar: vi.fn() },
}));
vi.mock('../../../api/categoriasApi', () => ({
  categoriasApi: { listar: vi.fn() },
}));

// Sustituye al listado para comprobar a dónde navega el formulario y con qué mensaje.
function ListadoSimulado() {
  const { state } = useLocation();
  return <p>Listado: {state?.mensaje}</p>;
}

function renderizar(entrada) {
  return render(
    <MemoryRouter initialEntries={[entrada]}>
      <Routes>
        <Route path="/productos" element={<ListadoSimulado />} />
        <Route path="/productos/nuevo" element={<ProductoFormularioPage />} />
        <Route path="/productos/:idProducto/editar" element={<ProductoFormularioPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

async function llenarFormularioValido(usuario) {
  await usuario.type(await screen.findByLabelText(/Código/), 'TEST-100');
  await usuario.type(screen.getByLabelText(/Nombre del producto/), 'Panela x 1kg');
  await usuario.selectOptions(screen.getByLabelText(/Categoría/), '1');
  await usuario.type(screen.getByLabelText(/Precio de venta/), '4500');
  await usuario.type(screen.getByLabelText(/Stock/), '10');
}

const guardar = (usuario) =>
  usuario.click(screen.getByRole('button', { name: 'Guardar producto' }));

beforeEach(() => {
  vi.mocked(categoriasApi.listar).mockResolvedValue(CATEGORIAS);
  vi.mocked(productosApi.registrar).mockReset();
  vi.mocked(productosApi.actualizar).mockReset();
  vi.mocked(productosApi.obtener).mockReset();
});

describe('ProductoFormularioPage en registro', () => {
  it('muestra los errores del cliente junto a cada campo sin llamar a la API', async () => {
    const usuario = userEvent.setup();
    renderizar('/productos/nuevo');
    await screen.findByLabelText(/Código/);

    await guardar(usuario);

    const codigo = screen.getByLabelText(/Código/);
    expect(codigo).toHaveAttribute('aria-invalid', 'true');
    expect(codigo).toHaveAccessibleDescription('El código es obligatorio');
    expect(codigo.closest('.form-group')).toHaveClass('has-error');
    expect(screen.getByText('El precio es obligatorio')).toBeInTheDocument();
    expect(screen.getByText('La categoría es obligatoria')).toBeInTheDocument();
    expect(screen.getByText('Revisa los campos marcados.')).toBeInTheDocument();
    expect(productosApi.registrar).not.toHaveBeenCalled();
  });

  it('borra el error de un campo cuando el usuario lo corrige', async () => {
    const usuario = userEvent.setup();
    renderizar('/productos/nuevo');
    await screen.findByLabelText(/Código/);
    await guardar(usuario);

    await usuario.type(screen.getByLabelText(/Código/), 'T');

    expect(screen.queryByText('El código es obligatorio')).not.toBeInTheDocument();
    expect(screen.getByLabelText(/Código/)).not.toHaveAttribute('aria-invalid');
    expect(screen.getByText('El nombre es obligatorio')).toBeInTheDocument();
  });

  it('registra el producto y navega al listado con el mensaje de éxito', async () => {
    const usuario = userEvent.setup();
    vi.mocked(productosApi.registrar).mockResolvedValue({ idProducto: 10 });
    renderizar('/productos/nuevo');

    await llenarFormularioValido(usuario);
    await guardar(usuario);

    expect(productosApi.registrar).toHaveBeenCalledWith({
      codigo: 'TEST-100',
      nombre: 'Panela x 1kg',
      precio: 4500,
      stock: 10,
      fechaVencimiento: null,
      idCategoria: 1,
    });
    expect(
      await screen.findByText('Listado: Producto registrado correctamente.'),
    ).toBeInTheDocument();
  });

  it('deshabilita el botón mientras se envía', async () => {
    const usuario = userEvent.setup();
    vi.mocked(productosApi.registrar).mockReturnValue(new Promise(() => {}));
    renderizar('/productos/nuevo');

    await llenarFormularioValido(usuario);
    await guardar(usuario);

    expect(screen.getByRole('button', { name: 'Guardar producto' })).toBeDisabled();
  });

  it('muestra el 409 de código duplicado como alerta general', async () => {
    const usuario = userEvent.setup();
    const detalle = 'Ya existe un producto con el código TEST-100';
    vi.mocked(productosApi.registrar).mockRejectedValue(new ErrorApi(409, detalle));
    renderizar('/productos/nuevo');

    await llenarFormularioValido(usuario);
    await guardar(usuario);

    expect(await screen.findByRole('alert')).toHaveTextContent(detalle);
    expect(screen.getByRole('button', { name: 'Guardar producto' })).toBeEnabled();
  });

  it('muestra los errores 400 del backend junto a cada campo', async () => {
    const usuario = userEvent.setup();
    vi.mocked(productosApi.registrar).mockRejectedValue(
      new ErrorApi(400, 'Uno o más campos tienen valores inválidos', {
        nombre: 'El nombre no puede superar 150 caracteres',
      }),
    );
    renderizar('/productos/nuevo');

    await llenarFormularioValido(usuario);
    await guardar(usuario);

    expect(await screen.findByLabelText(/Nombre del producto/)).toHaveAccessibleDescription(
      'El nombre no puede superar 150 caracteres',
    );
    expect(screen.queryByText('Uno o más campos tienen valores inválidos')).not.toBeInTheDocument();
  });
});

describe('ProductoFormularioPage en edición', () => {
  it('carga el producto y lo actualiza', async () => {
    const usuario = userEvent.setup();
    vi.mocked(productosApi.obtener).mockResolvedValue(PRODUCTOS[0]);
    vi.mocked(productosApi.actualizar).mockResolvedValue(PRODUCTOS[0]);
    renderizar('/productos/1/editar');

    const stock = await screen.findByLabelText(/Stock/);
    expect(productosApi.obtener).toHaveBeenCalledWith('1');
    expect(screen.getByLabelText(/Código/)).toHaveValue('TEST-001');
    expect(screen.getByLabelText(/Fecha de vencimiento/)).toHaveValue('2026-12-31');
    expect(screen.getByLabelText(/Categoría/)).toHaveValue('1');

    await usuario.clear(stock);
    await usuario.type(stock, '35');
    await usuario.click(screen.getByRole('button', { name: 'Guardar cambios' }));

    expect(productosApi.actualizar).toHaveBeenCalledWith('1', {
      codigo: 'TEST-001',
      nombre: 'Arroz Diana x 500g',
      precio: 3500,
      stock: 35,
      fechaVencimiento: '2026-12-31',
      idCategoria: 1,
    });
    expect(
      await screen.findByText('Listado: Producto actualizado correctamente.'),
    ).toBeInTheDocument();
  });

  it('muestra el 404 y no muestra el formulario si el producto no existe', async () => {
    const detalle = 'No se encontró el producto con id 99';
    vi.mocked(productosApi.obtener).mockRejectedValue(new ErrorApi(404, detalle));
    renderizar('/productos/99/editar');

    expect(await screen.findByRole('alert')).toHaveTextContent(detalle);
    expect(screen.queryByRole('button', { name: 'Guardar cambios' })).not.toBeInTheDocument();
  });
});
