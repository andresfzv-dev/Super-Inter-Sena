package com.superinter.usuario.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.superinter.common.exception.ConflictoException;
import com.superinter.usuario.domain.Usuario;

/**
 * Implementación en memoria para pruebas unitarias, sin base de datos.
 * Reproduce la restricción única usuarios_correo_key: rechaza un correo repetido con el
 * mismo mensaje que el repositorio JDBC.
 */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<String, Usuario> usuariosPorCorreo = new LinkedHashMap<>();
    private int siguienteId = 1;

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuariosPorCorreo.containsKey(usuario.getCorreo())) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo " + usuario.getCorreo());
        }
        Usuario guardado = usuario.toBuilder().idUsuario(siguienteId++).build();
        usuariosPorCorreo.put(guardado.getCorreo(), guardado);
        return guardado;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return Optional.ofNullable(usuariosPorCorreo.get(correo));
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return usuariosPorCorreo.containsKey(correo);
    }
}
