import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Sin los globales de Vitest, Testing Library no registra su limpieza automática;
// sin esta llamada, el DOM de una prueba se filtraría a la siguiente.
afterEach(() => {
  cleanup();
});
