export default function Encabezado({ titulo, onAbrirMenu }) {
  return (
    <>
      <button
        type="button"
        className="btn btn-icon btn-secondary app-header__menu-toggle"
        aria-label="Abrir menú"
        onClick={onAbrirMenu}
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          aria-hidden="true"
        >
          <path d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <div className="app-header__title-group">
        <h1>{titulo}</h1>
      </div>
    </>
  );
}
