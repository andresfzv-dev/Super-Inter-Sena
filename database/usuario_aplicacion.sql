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


-- Módulo de proveedores: CRUD completo (migración 002)
GRANT SELECT, INSERT, UPDATE, DELETE ON proveedores TO super_inter_app;
GRANT USAGE ON SEQUENCE proveedores_id_proveedor_seq TO super_inter_app;

-- Módulo de usuarios: registro e inicio de sesión (migración 003).
-- Sin UPDATE ni DELETE por mínimo privilegio.
GRANT SELECT, INSERT ON usuarios TO super_inter_app;
GRANT USAGE ON SEQUENCE usuarios_id_usuario_seq TO super_inter_app;

-- Limpieza de usuarios de prueba (migración 004). schema.sql se exporta sin privilegios,
-- así que la función llega con EXECUTE para PUBLIC y aquí se restringe a la aplicación.
REVOKE EXECUTE ON FUNCTION eliminar_usuarios_de_prueba() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION eliminar_usuarios_de_prueba() TO super_inter_app;
