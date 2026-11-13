package com.superinter.usuario.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import com.superinter.common.exception.CredencialesInvalidasException;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.dto.LoginRequest;
import com.superinter.usuario.dto.LoginResponse;
import com.superinter.usuario.dto.UsuarioResponse;
import com.superinter.usuario.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Inicio de sesión con correo y contraseña (RNF-01). Implementa la operación
 * {@code iniciarSesion()} de la clase Usuario del diagrama de clases.
 */
@Slf4j
@Service
public class AutenticacionService {

    /** Límite de BCrypt; el registro no permite guardar contraseñas más largas. */
    private static final int MAXIMO_BYTES_BCRYPT = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Hash BCrypt fijo contra el que se compara la contraseña cuando el correo no existe.
     *
     * <p>BCrypt es lento a propósito (decenas de milisegundos). Si con un correo inexistente
     * se respondiera de inmediato, un atacante podría medir el tiempo de respuesta y saber
     * qué correos están registrados aunque el mensaje de error sea el mismo. Comparar
     * siempre contra un hash iguala el tiempo de los dos casos.
     *
     * <p>Se calcula una sola vez al crear el servicio, con el mismo {@link PasswordEncoder}
     * de la aplicación, para que tenga el mismo costo que los hashes reales. La contraseña
     * de origen es aleatoria y se descarta: ninguna contraseña coincide con este hash.
     */
    private final String hashFicticio;

    public AutenticacionService(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * Verifica las credenciales y, si son correctas, emite un token JWT.
     *
     * @throws CredencialesInvalidasException si el correo no existe o la contraseña es
     *                                        incorrecta, con el mismo mensaje en ambos casos
     */
    public LoginResponse iniciarSesion(LoginRequest solicitud) {
        Optional<Usuario> encontrado = usuarioRepository.buscarPorCorreo(solicitud.correo());

        String hashAComparar = encontrado.map(Usuario::getContrasenaCifrada).orElse(hashFicticio);
        // La comparación se hace siempre, exista o no el correo (ver hashFicticio).
        boolean contrasenaCoincide = passwordEncoder.matches(solicitud.contrasena(), hashAComparar);

        // BCrypt ignora lo que pase de 72 bytes al comparar: sin este control, una contraseña
        // registrada de 72 bytes seguida de cualquier texto también sería aceptada.
        boolean longitudValida =
                solicitud.contrasena().getBytes(StandardCharsets.UTF_8).length <= MAXIMO_BYTES_BCRYPT;

        if (encontrado.isEmpty() || !contrasenaCoincide || !longitudValida) {
            // RNF-04. Se registra el correo para detectar intentos repetidos sobre una cuenta,
            // nunca la contraseña. Se quitan los saltos de línea porque el correo viene del
            // cliente y podría usarse para escribir líneas falsas en el log.
            log.warn("Inicio de sesión fallido: correo={}", solicitud.correo().replaceAll("[\\r\\n]", "_"));
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = encontrado.get();
        String token = tokenService.generar(usuario);

        log.info("Inicio de sesión exitoso: idUsuario={}", usuario.getIdUsuario());
        return LoginResponse.exitoso(token, tokenService.segundosDeExpiracion(), UsuarioResponse.desde(usuario));
    }
}
