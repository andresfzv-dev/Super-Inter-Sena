import { createContext } from 'react';

// El valor por defecto no hace nada para que una página pueda renderizarse sin AppLayout,
// como ocurre en las pruebas.
export const TituloPaginaContext = createContext(() => {});
