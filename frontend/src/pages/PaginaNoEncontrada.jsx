import { Link } from 'react-router';
import { useTituloPagina } from '../hooks/useTituloPagina';

export default function PaginaNoEncontrada() {
  useTituloPagina('Página no encontrada');

  return (
    <div className="page-heading">
      <div>
        <h2>Página no encontrada</h2>
        <p>La dirección solicitada no existe.</p>
      </div>
      <Link to="/productos" className="btn btn-secondary">
        &larr; Ir a productos
      </Link>
    </div>
  );
}
