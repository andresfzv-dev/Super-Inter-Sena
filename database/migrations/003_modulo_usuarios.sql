-- =====================================================================
-- Migración 003 - Módulo de usuarios (registro e inicio de sesión)
-- Motivo: RF-17 (creación de usuarios con roles), RNF-01 (autenticación
--         con usuario y contraseña), RNF-02 (permisos basados en roles)
--         y decisiones de la evidencia GA7-220501096-AA5-EV01:
--           * nombre que no sea solo espacios
--           * rol limitado a los roles del documento de requerimientos
--           * correo guardado en minúsculas
--           * permisos de la aplicación sobre la tabla
-- Requisito: tabla usuarios vacía (verificado antes de aplicar: 0 filas)
-- =====================================================================

BEGIN;

-- NOT NULL no impide un nombre formado solo por espacios.
ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_nombre CHECK (btrim(nombre) <> '');

-- Se guarda el código del enum Rol de la aplicación. La restricción evita
-- que un INSERT manual o un error de la aplicación deje un rol que el
-- sistema no reconoce y que, por tanto, no daría permisos coherentes.
ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_rol CHECK (
        rol IN ('ADMINISTRADOR', 'GERENTE', 'AUXILIAR_INVENTARIOS', 'CAJERO', 'COMPRAS')
    );

-- La aplicación normaliza el correo a minúsculas antes de guardarlo y de
-- buscarlo. Al exigirlo también aquí, la restricción única que ya existe
-- (usuarios_correo_key) basta para impedir "Ana@x.co" y "ana@x.co" como
-- usuarios distintos, sin crear un índice adicional sobre lower(correo).
ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_correo_minusculas CHECK (correo = lower(correo));

-- Permisos de la aplicación: solo lo que usan el registro (INSERT y la
-- secuencia del id) y el inicio de sesión (SELECT). Sin UPDATE ni DELETE
-- por mínimo privilegio; la gestión de usuarios por el administrador (HU4)
-- se agregará en otra evidencia con su propia migración.
GRANT SELECT, INSERT ON usuarios TO super_inter_app;
GRANT USAGE ON SEQUENCE usuarios_id_usuario_seq TO super_inter_app;

COMMIT;
