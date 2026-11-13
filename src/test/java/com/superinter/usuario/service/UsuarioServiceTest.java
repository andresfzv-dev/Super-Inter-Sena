package com.superinter.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.superinter.common.exception.ConflictoException;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.dto.RegistroRequest;
import com.superinter.usuario.dto.UsuarioResponse;
import com.superinter.usuario.repository.UsuarioRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UsuarioServiceTest {

    private static final String CONTRASENA = "Clave-Segura-2026";

    // Costo 4 (el mínimo) en lugar de 10: el algoritmo es el mismo y las pruebas son más rápidas.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UsuarioRepositoryEnMemoria repositorio;
    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        repositorio = new UsuarioRepositoryEnMemoria();
        usuarioService = new UsuarioService(repositorio, passwordEncoder);
    }

    @Test
    void registrarAsignaIdYElRolCajero() {
        UsuarioResponse registrado = usuarioService.registrar(solicitud("ana.perez@ejemplo.com"));

        assertThat(registrado.idUsuario()).isNotNull();
        assertThat(registrado.nombre()).isEqualTo("Ana Pérez");
        assertThat(registrado.correo()).isEqualTo("ana.perez@ejemplo.com");
        assertThat(registrado.rol()).isEqualTo(Rol.CAJERO);
    }

    @Test
    void registrarGuardaLaContrasenaCifradaConBcrypt() {
        usuarioService.registrar(solicitud("ana.perez@ejemplo.com"));

        Usuario guardado = repositorio.buscarPorCorreo("ana.perez@ejemplo.com").orElseThrow();
        assertThat(guardado.getContrasenaCifrada())
                .isNotEqualTo(CONTRASENA)
                .startsWith("$2a$");
        assertThat(passwordEncoder.matches(CONTRASENA, guardado.getContrasenaCifrada())).isTrue();
        assertThat(passwordEncoder.matches("otra-contrasena", guardado.getContrasenaCifrada())).isFalse();
    }

    @Test
    void registrarConCorreoExistenteEnOtrasMayusculasLanzaConflicto() {
        usuarioService.registrar(solicitud("ana.perez@ejemplo.com"));

        // La solicitud normaliza el correo, así que llega al servicio en minúsculas.
        assertThatThrownBy(() -> usuarioService.registrar(solicitud("  ANA.Perez@Ejemplo.com ")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un usuario registrado con el correo ana.perez@ejemplo.com");
    }

    private RegistroRequest solicitud(String correo) {
        return new RegistroRequest("Ana Pérez", correo, CONTRASENA);
    }
}
