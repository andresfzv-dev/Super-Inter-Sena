import { useState } from 'react';
import { Outlet } from 'react-router';
import { TituloPaginaContext } from '../../hooks/tituloPaginaContext';
import Encabezado from './Encabezado';
import MenuLateral from './MenuLateral';
import PiePagina from './PiePagina';

export default function AppLayout() {
  const [titulo, setTitulo] = useState('');
  // El estado vive aquí porque lo comparten el botón del encabezado, el menú y el fondo oscuro.
  const [menuAbierto, setMenuAbierto] = useState(false);

  const cerrarMenu = () => setMenuAbierto(false);

  return (
    <>
      <div className={`sidebar-overlay${menuAbierto ? ' is-visible' : ''}`} onClick={cerrarMenu} />
      <div className="app-shell">
        <aside className={`sidebar${menuAbierto ? ' is-open' : ''}`}>
          <MenuLateral onNavegar={cerrarMenu} />
        </aside>
        <header className="app-header">
          <Encabezado titulo={titulo} onAbrirMenu={() => setMenuAbierto(true)} />
        </header>
        <main className="app-main">
          <TituloPaginaContext.Provider value={setTitulo}>
            <Outlet />
          </TituloPaginaContext.Provider>
        </main>
        <footer className="app-footer">
          <PiePagina />
        </footer>
      </div>
    </>
  );
}
