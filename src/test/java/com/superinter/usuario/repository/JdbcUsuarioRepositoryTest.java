package com.superinter.usuario.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.superinter.common.exception.ConflictoException;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del repositorio de usuarios contra la base de datos real.
 *
 * <p>Los usuarios de prueba usan correos con el prefijo test- y se eliminan antes y después
 * de cada prueba con la función eliminar_usuarios_de_prueba() (migración 004), porque la
 * aplicación no tiene permiso DELETE sobre la tabla usuarios.
 */
@SpringBootTest
class JdbcUsuarioRepositoryTest {

    private static final String CORREO = "test-ana.perez@ejemplo.com";
    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "SELECT eliminar_usuarios_de_prueba()";

    // El repositorio guarda lo que recibe; el valor solo necesita parecer un hash BCrypt.
    private static final String HASH_DE_PRUEBA = "$2a$10$abcdefghijklmnopqrstuuJ0b1c2d3e4f5g6h7i8j9k0l1m2n3o4p";

    private final UsuarioRepository usuarioRepository;
    private final DataSource dataSource;

    @Autowired
    JdbcUsuarioRepositoryTest(UsuarioRepository usuarioRepository, DataSource dataSource) {
        this.usuarioRepository = usuarioRepository;
        this.dataSource = dataSource;
    }

    @BeforeEach
    void limpiarAntes() throws SQLException {
        eliminarDatosDePrueba();
    }

    @AfterEach
    void limpiarDespues() throws SQLException {
        eliminarDatosDePrueba();
    }

    @Test
    void guardarAsignaElIdGeneradoYConservaTodosLosCampos() {
        Usuario guardado = usuarioRepository.guardar(crearUsuario(CORREO));

        assertThat(guardado.getIdUsuario()).isNotNull();

        Usuario consultado = usuarioRepository.buscarPorCorreo(CORREO).orElseThrow();
        assertThat(consultado.getIdUsuario()).isEqualTo(guardado.getIdUsuario());
        assertThat(consultado.getNombre()).isEqualTo("TEST-Ana Pérez");
        assertThat(consultado.getCorreo()).isEqualTo(CORREO);
        assertThat(consultado.getContrasenaCifrada()).isEqualTo(HASH_DE_PRUEBA);
        assertThat(consultado.getRol()).isEqualTo(Rol.CAJERO);
    }

    @Test
    void buscarPorCorreoDevuelveVacioCuandoNoExiste() {
        assertThat(usuarioRepository.buscarPorCorreo("test-no.existe@ejemplo.com")).isEmpty();
    }

    @Test
    void existePorCorreoIndicaSiElCorreoEstaRegistrado() {
        usuarioRepository.guardar(crearUsuario(CORREO));

        assertThat(usuarioRepository.existePorCorreo(CORREO)).isTrue();
        assertThat(usuarioRepository.existePorCorreo("test-otro@ejemplo.com")).isFalse();
    }

    @Test
    void guardarConCorreoDuplicadoTraduceElErrorDeUnicidadAConflicto() {
        usuarioRepository.guardar(crearUsuario(CORREO));

        // Se llama al repositorio directamente, sin la verificación previa del servicio,
        // para comprobar que el SQLSTATE 23505 de usuarios_correo_key se traduce.
        assertThatThrownBy(() -> usuarioRepository.guardar(crearUsuario(CORREO)))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un usuario registrado con el correo " + CORREO);
    }

    private Usuario crearUsuario(String correo) {
        return Usuario.builder()
                .nombre("TEST-Ana Pérez")
                .correo(correo)
                .contrasenaCifrada(HASH_DE_PRUEBA)
                .rol(Rol.CAJERO)
                .build();
    }

    private void eliminarDatosDePrueba() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LIMPIAR_DATOS_DE_PRUEBA)) {
            statement.execute();
        }
    }
}
