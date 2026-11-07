import { useContext, useEffect } from 'react';
import { TituloPaginaContext } from './tituloPaginaContext';

/**
 * Publica el título de la página actual. Cada página declara su título, pero el <h1> vive en el
 * encabezado de AppLayout, que está por encima de la ruta; por eso se comunica con un contexto.
 */
export function useTituloPagina(titulo) {
  const establecerTitulo = useContext(TituloPaginaContext);

  useEffect(() => {
    establecerTitulo(titulo);
    document.title = `${titulo} | Super Inter`;
  }, [establecerTitulo, titulo]);
}
