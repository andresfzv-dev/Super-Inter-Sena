-- =====================================================================
-- Datos iniciales - Categorías de productos
-- Idempotente: se puede ejecutar varias veces sin duplicar registros
-- =====================================================================

INSERT INTO categorias (nombre) VALUES
    ('Abarrotes'),
    ('Lácteos'),
    ('Bebidas'),
    ('Aseo'),
    ('Frutas y verduras')
ON CONFLICT (nombre) DO NOTHING;
