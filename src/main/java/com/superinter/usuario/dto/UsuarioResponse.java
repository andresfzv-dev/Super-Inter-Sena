package com.superinter.usuario.dto;

import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;

/**
 * Datos públicos de un usuario. No incluye la contraseña ni su hash (RNF-03): esta es la
 * única representación de un usuario que sale por la API.
 */
public record UsuarioResponse(
        Integer idUsuario,
        String nombre,
        String correo,
        Rol rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol());
    }
}
