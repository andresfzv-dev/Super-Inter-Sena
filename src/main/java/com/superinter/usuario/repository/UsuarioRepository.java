package com.superinter.usuario.repository;

import java.util.Optional;

import com.superinter.usuario.domain.Usuario;

/**
 * Operaciones de acceso a datos de los usuarios que necesitan el registro y el inicio de
 * sesión. No hay actualización ni eliminación: la aplicación solo tiene permisos SELECT e
 * INSERT sobre la tabla (migración 003).
 *
 * <p>Los correos se reciben ya normalizados (sin espacios y en minúsculas); la
 * normalización es responsabilidad de los DTO y servicios.
 */
public interface UsuarioRepository {

    /**
     * Inserta el usuario y devuelve una nueva instancia con el identificador generado.
     *
     * @throws com.superinter.common.exception.ConflictoException si el correo ya está registrado
     */
    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);
}
