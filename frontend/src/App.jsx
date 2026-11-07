import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import AppLayout from './components/layout/AppLayout';
import ProductoFormularioPage from './features/productos/pages/ProductoFormularioPage';
import ProductoListaPage from './features/productos/pages/ProductoListaPage';
import PaginaNoEncontrada from './pages/PaginaNoEncontrada';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Todas las rutas comparten AppLayout, que conserva el menú y el encabezado. */}
        <Route element={<AppLayout />}>
          {/* Productos es el único módulo de este front-end, por eso es la página de inicio. */}
          <Route index element={<Navigate to="/productos" replace />} />
          <Route path="productos" element={<ProductoListaPage />} />
          <Route path="productos/nuevo" element={<ProductoFormularioPage />} />
          <Route path="productos/:idProducto/editar" element={<ProductoFormularioPage />} />
          <Route path="*" element={<PaginaNoEncontrada />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
