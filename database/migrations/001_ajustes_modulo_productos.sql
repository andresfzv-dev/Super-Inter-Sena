-- =====================================================================
-- Migración 001 - Ajustes para el módulo de gestión de productos
-- Motivo: RF-01, regla de negocio 1 (SKU único) y consistencia de nombres
-- Requisito: tablas vacías (codigo se agrega como NOT NULL sin default)
-- =====================================================================

BEGIN;

-- ---------- categorias ----------
ALTER TABLE categorias RENAME COLUMN id_categorias TO id_categoria;
ALTER SEQUENCE categorias_id_categorias_seq RENAME TO categorias_id_categoria_seq;

ALTER TABLE categorias
    ADD CONSTRAINT uq_categorias_nombre UNIQUE (nombre);

-- ---------- productos ----------
ALTER TABLE productos
    ADD COLUMN codigo VARCHAR(50) NOT NULL;

ALTER TABLE productos
    ADD CONSTRAINT uq_productos_codigo UNIQUE (codigo);

ALTER TABLE productos
    DROP CONSTRAINT productos_precio_check;

ALTER TABLE productos
    ADD CONSTRAINT chk_productos_precio CHECK (precio > 0);

ALTER TABLE productos
    ADD CONSTRAINT chk_productos_nombre CHECK (btrim(nombre) <> '');

CREATE INDEX idx_productos_id_categoria
    ON productos (id_categoria);

-- ---------- consistencia de nombres (otras tablas, vacías) ----------
ALTER TABLE usuarios RENAME COLUMN "contraseña" TO contrasena;
ALTER TABLE usuarios RENAME COLUMN id TO id_usuario;
ALTER SEQUENCE usuarios_id_seq RENAME TO usuarios_id_usuario_seq;

ALTER TABLE compras RENAME COLUMN id_compras TO id_compra;
ALTER SEQUENCE compras_id_compras_seq RENAME TO compras_id_compra_seq;

COMMIT;
