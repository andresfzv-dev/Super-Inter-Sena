package com.superinter.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import com.superinter.common.exception.CredencialesInvalidasException;
import com.superinter.common.security.JwtProperties;
import com.superinter.common.security.SeguridadConfig;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.dto.LoginRequest;
import com.superinter.usuario.dto.LoginResponse;
import com.superinter.usuario.dto.RegistroRequest;
import com.superinter.usuario.repository.UsuarioRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class AutenticacionServiceTest {

    private static final String CORREO = "ana.perez@ejemplo.com";
    private static final String CONTRASENA = "Clave-Segura-2026";

    // Costo 4 (el mínimo) en lugar de 10: el algoritmo es el mismo y las pruebas son más rápidas.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private JwtDecoder jwtDecoder;
    private UsuarioService usuarioService;
    private AutenticacionService autenticacionService;
    private int idRegistrado;

    @BeforeEach
    void configurar() {
        // Secreto aleatorio generado para cada prueba; no se usa el de .env.
        byte[] secreto = new byte[32];
        new SecureRandom().nextBytes(secreto);
        JwtProperties propiedades = new JwtProperties(
                Base64.getEncoder().encodeToString(secreto), "super-inter", Duration.ofMinutes(60));
        SeguridadConfig configuracion = new SeguridadConfig();
        jwtDecoder = configuracion.jwtDecoder(propiedades);

        UsuarioRepositoryEnMemoria repositorio = new UsuarioRepositoryEnMemoria();
        usuarioService = new UsuarioService(repositorio, passwordEncoder);
        autenticacionService = new AutenticacionService(repositorio, passwordEncoder,
                new TokenService(configuracion.jwtEncoder(propiedades), propiedades));

        idRegistrado = usuarioService.registrar(new RegistroRequest("Ana Pérez", CORREO, CONTRASENA)).idUsuario();
    }

    @Test
    void credencialesCorrectasDevuelvenMensajeYTokenValido() {
        LoginResponse respuesta = autenticacionService.iniciarSesion(new LoginRequest(CORREO, CONTRASENA));

        assertThat(respuesta.mensaje()).isEqualTo("Autenticación satisfactoria");
        assertThat(respuesta.tipoToken()).isEqualTo("Bearer");
        assertThat(respuesta.expiraEn()).isEqualTo(3600);
        assertThat(respuesta.usuario().idUsuario()).isEqualTo(idRegistrado);
        assertThat(respuesta.usuario().rol()).isEqualTo(Rol.CAJERO);
        assertThat(jwtDecoder.decode(respuesta.token()).getSubject()).isEqualTo(String.valueOf(idRegistrado));
    }

    @Test
    void correoConMayusculasYEspaciosTambienAutentica() {
        LoginResponse respuesta = autenticacionService.iniciarSesion(
                new LoginRequest("  ANA.Perez@Ejemplo.COM ", CONTRASENA));

        assertThat(respuesta.usuario().correo()).isEqualTo(CORREO);
    }

    @Test
    void contrasenaIncorrectaLanzaCredencialesInvalidas() {
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(new LoginRequest(CORREO, "otra-contrasena")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void correoInexistenteYContrasenaIncorrectaDanLaMismaExcepcionYMensaje() {
        Throwable porContrasena = catchThrowable(
                () -> autenticacionService.iniciarSesion(new LoginRequest(CORREO, "otra-contrasena")));
        Throwable porCorreo = catchThrowable(
                () -> autenticacionService.iniciarSesion(new LoginRequest("no.existe@ejemplo.com", CONTRASENA)));

        assertThat(porCorreo)
                .isExactlyInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(porContrasena.getMessage());
        assertThat(porContrasena).isExactlyInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void laContrasenaRegistradaSeguidaDeTextoExtraNoAutentica() {
        // Contraseña registrada de 72 bytes, el máximo. BCrypt ignora lo que venga después
        // del byte 72, así que sin el control de longitud esta variante sería aceptada.
        String contrasenaLimite = "a".repeat(72);
        usuarioService.registrar(new RegistroRequest("Luis Gómez", "luis@ejemplo.com", contrasenaLimite));

        assertThat(autenticacionService.iniciarSesion(new LoginRequest("luis@ejemplo.com", contrasenaLimite)).token())
                .isNotBlank();
        assertThatThrownBy(() -> autenticacionService.iniciarSesion(
                new LoginRequest("luis@ejemplo.com", contrasenaLimite + "texto-extra")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
