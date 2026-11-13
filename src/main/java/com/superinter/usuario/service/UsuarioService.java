package com.superinter.usuario.service;

import com.superinter.common.exception.ConflictoException;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.dto.RegistroRequest;
import com.superinter.usuario.dto.UsuarioResponse;
import com.superinter.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de la gestión de usuarios: por ahora, el registro público (RF-17, HU4).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    /**
     * Rol que recibe todo usuario registrado por el endpoint público. Es el de menos
     * permisos sobre la configuración del sistema; cambiarlo es tarea del administrador
     * (regla de negocio 3, HU4), que se implementará en otra evidencia.
     */
    static final Rol ROL_REGISTRO_PUBLICO = Rol.CAJERO;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un usuario con la contraseña cifrada con BCrypt (RNF-03).
     *
     * @throws ConflictoException si el correo ya está registrado
     */
    public UsuarioResponse registrar(RegistroRequest solicitud) {
        // Verificación previa para responder con un mensaje claro en el caso normal. Si dos
        // registros simultáneos la superan, la restricción única de la base de datos rechaza
        // el segundo y el repositorio lanza el mismo conflicto.
        if (usuarioRepository.existePorCorreo(solicitud.correo())) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo " + solicitud.correo());
        }

        Usuario usuario = Usuario.builder()
                .nombre(solicitud.nombre())
                .correo(solicitud.correo())
                .contrasenaCifrada(passwordEncoder.encode(solicitud.contrasena()))
                .rol(ROL_REGISTRO_PUBLICO)
                .build();

        Usuario guardado = usuarioRepository.guardar(usuario);

        // RNF-04: registro de actividad. Solo el id: el log no debe tener datos sensibles.
        log.info("Usuario registrado: idUsuario={}", guardado.getIdUsuario());
        return UsuarioResponse.desde(guardado);
    }
}
