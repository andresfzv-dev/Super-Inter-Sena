package com.superinter.usuario.controller;

import com.superinter.usuario.dto.RegistroRequest;
import com.superinter.usuario.dto.RegistroResponse;
import com.superinter.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicio web de registro e inicio de sesión (evidencia GA7-220501096-AA5-EV01).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacionController {

    private final UsuarioService usuarioService;

    /**
     * Registro público. Responde 201 sin cabecera Location porque aún no existe un endpoint
     * para consultar un usuario por id (la gestión de usuarios es de una evidencia futura).
     */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return RegistroResponse.exitoso(usuarioService.registrar(solicitud));
    }
}
