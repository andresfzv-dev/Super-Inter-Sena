-- =====================================================================
-- Migración 002 - Módulo de gestión de proveedores
-- Motivo: RF-04 (gestionar proveedores), RF-15 (información de contacto)
--         y decisiones de la evidencia GA7-220501096-AA3-EV01:
--           * correo electrónico del proveedor
--           * nombre obligatorio y único
--           * permisos de la aplicación sobre la tabla
-- Requisito: tabla proveedores vacía o sin nombres repetidos/en blanco
-- =====================================================================

BEGIN;

-- Correo opcional. 254 es la longitud máxima de una dirección de correo
-- según RFC 5321; el formato se valida en la aplicación.
ALTER TABLE proveedores
    ADD COLUMN correo VARCHAR(254);

-- NOT NULL no impide un nombre formado solo por espacios.
ALTER TABLE proveedores
    ADD CONSTRAINT chk_proveedores_nombre CHECK (btrim(nombre) <> '');

-- Unicidad sin distinguir mayúsculas: "Postobón" y "POSTOBÓN" son el mismo
-- proveedor. Se usa un índice único sobre una expresión porque una
-- restricción UNIQUE solo admite columnas, no funciones como lower().
CREATE UNIQUE INDEX uq_proveedores_nombre
    ON proveedores (lower(nombre));

-- Permisos de la aplicación (CRUD completo). No se otorgan permisos sobre
-- compras: al eliminar un proveedor, PostgreSQL verifica la llave foránea
-- con los permisos del dueño de la tabla, así que un proveedor con compras
-- produce el error 23503 aunque la aplicación no pueda leer compras.
GRANT SELECT, INSERT, UPDATE, DELETE ON proveedores TO super_inter_app;
GRANT USAGE ON SEQUENCE proveedores_id_proveedor_seq TO super_inter_app;

COMMIT;