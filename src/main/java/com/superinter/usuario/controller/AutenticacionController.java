package com.superinter.usuario.controller;

import com.superinter.usuario.dto.LoginRequest;
import com.superinter.usuario.dto.LoginResponse;
import com.superinter.usuario.dto.RegistroRequest;
import com.superinter.usuario.dto.RegistroResponse;
import com.superinter.usuario.dto.UsuarioResponse;
import com.superinter.usuario.service.AutenticacionService;
import com.superinter.usuario.service.TokenService;
import com.superinter.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicio web de registro e inicio de sesión (evidencia GA7-220501096-AA5-EV01).
 *
 * <p>El registro y el inicio de sesión son públicos. El perfil exige el token, según la
 * configuración de {@code SeguridadConfig}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacionController {

    private final UsuarioService usuarioService;
    private final AutenticacionService autenticacionService;
    private final TokenService tokenService;

    /**
     * Registro público. Responde 201 sin cabecera Location porque aún no existe un endpoint
     * para consultar un usuario por id (la gestión de usuarios es de una evidencia futura).
     */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return RegistroResponse.exitoso(usuarioService.registrar(solicitud));
    }

    /**
     * Inicio de sesión. Responde 200 con "Autenticación satisfactoria" y el token, o 401 con
     * "Error en la autenticación" si el correo o la contraseña no son correctos.
     */
    @PostMapping("/login")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return autenticacionService.iniciarSesion(solicitud);
    }

    /**
     * Datos del usuario dueño del token. Spring Security ya validó la firma, el emisor y la
     * vigencia del token antes de llegar aquí; sin un token válido responde 401.
     */
    @GetMapping("/perfil")
    public UsuarioResponse perfil(@AuthenticationPrincipal Jwt token) {
        return tokenService.leerUsuario(token);
    }
}
