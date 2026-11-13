-- =====================================================================
-- Migración 004 - Limpieza de usuarios de prueba
-- Motivo: las pruebas automáticas del módulo de usuarios (evidencia
--         GA7-220501096-AA5-EV01) deben borrar sus datos antes y después
--         de cada prueba, pero la migración 003 no otorga DELETE sobre
--         usuarios a super_inter_app, por mínimo privilegio.
-- Solución: una función que se ejecuta con los permisos de su dueño
--           (SECURITY DEFINER) y que solo puede borrar usuarios cuyo correo
--           empiece por 'test-'. La aplicación recibe EXECUTE sobre la
--           función, pero sigue sin DELETE sobre la tabla.
-- =====================================================================

BEGIN;

CREATE FUNCTION eliminar_usuarios_de_prueba()
    RETURNS integer
    LANGUAGE sql
    SECURITY DEFINER
    -- Una función SECURITY DEFINER debe fijar search_path: si no, quien la llama podría
    -- crear un objeto "usuarios" en otro esquema y hacer que la función, con los permisos
    -- del dueño, actúe sobre él.
    SET search_path = public, pg_temp
AS $$
    WITH eliminados AS (
        -- El filtro está fijo en la función y no se recibe como parámetro, para que no
        -- pueda usarse para borrar usuarios reales.
        DELETE FROM usuarios
        WHERE correo LIKE 'test-%'
        RETURNING 1
    )
    SELECT count(*)::integer FROM eliminados;
$$;

COMMENT ON FUNCTION eliminar_usuarios_de_prueba() IS
    'Borra los usuarios creados por las pruebas automáticas (correo LIKE ''test-%''). '
    'Solo la usan las pruebas; el código de la aplicación no la llama.';

-- PostgreSQL otorga EXECUTE a PUBLIC por defecto en las funciones nuevas; se quita para
-- que solo la aplicación pueda ejecutarla.
REVOKE EXECUTE ON FUNCTION eliminar_usuarios_de_prueba() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION eliminar_usuarios_de_prueba() TO super_inter_app;

COMMIT;
