import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
// Mismo orden que fragmentos/head.jsp: cada hoja sobrescribe a las anteriores.
import './styles/variables.css';
import './styles/base.css';
import './styles/layout.css';
import './styles/components.css';
import './styles/responsive.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
