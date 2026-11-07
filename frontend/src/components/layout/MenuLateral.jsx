import { NavLink } from 'react-router';

export default function MenuLateral({ onNavegar }) {
  return (
    <>
      <div className="sidebar__brand">
        <span className="sidebar__brand-logo">SI</span>
        <span className="sidebar__brand-text">Super Inter</span>
      </div>
      <nav className="sidebar__nav" aria-label="Navegación principal">
        <div>
          <p className="sidebar__group-label">Inventario</p>
          <ul>
            <li>
              {/* El menú se cierra en el clic porque el enlace es la única forma de navegar con
                  él abierto: el fondo oscuro cubre el resto de la página. */}
              <NavLink
                to="/productos"
                onClick={onNavegar}
                className={({ isActive }) => `sidebar__link${isActive ? ' is-active' : ''}`}
              >
                <span className="sidebar__link-icon">
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                  >
                    <path d="M21 8 12 3 3 8v8l9 5 9-5V8Zm-9-3.3 6.3 3.5L12 12l-6.3-3.8L12 4.7ZM5 9.6l6 3.4v6.9l-6-3.4V9.6Zm8 10.3v-6.9l6-3.4v6.9l-6 3.4Z" />
                  </svg>
                </span>
                <span>Productos</span>
              </NavLink>
            </li>
          </ul>
        </div>
      </nav>
    </>
  );
}
