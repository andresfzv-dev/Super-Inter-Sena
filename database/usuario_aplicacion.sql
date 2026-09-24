-- =====================================================================
-- Usuario de la aplicación con mínimo privilegio
-- La contraseña se recibe como variable de psql (app_password);
-- nunca se escribe en este archivo.
-- =====================================================================

\set ON_ERROR_STOP on

CREATE ROLE super_inter_app WITH LOGIN PASSWORD :'app_password';

GRANT CONNECT ON DATABASE "super-inter" TO super_inter_app;
GRANT USAGE ON SCHEMA public TO super_inter_app;

-- Módulo de productos: CRUD completo
GRANT SELECT, INSERT, UPDATE, DELETE ON productos TO super_inter_app;
GRANT USAGE ON SEQUENCE productos_id_producto_seq TO super_inter_app;

-- Categorías: solo lectura
GRANT SELECT ON categorias TO super_inter_app;