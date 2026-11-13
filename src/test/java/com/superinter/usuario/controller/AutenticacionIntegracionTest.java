package com.superinter.usuario.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prueba de punta a punta del servicio web de autenticación: levanta la aplicación con
 * Tomcat, Spring Security y PostgreSQL, y envía peticiones HTTP reales.
 *
 * <p>Los usuarios de prueba usan correos con el prefijo test- y se eliminan antes y después
 * de cada prueba con la función eliminar_usuarios_de_prueba() (migración 004).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AutenticacionIntegracionTest {

    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "SELECT eliminar_usuarios_de_prueba()";

    private static final String CORREO = "test-integracion@ejemplo.com";
    private static final String CONTRASENA = "Clave-Segura-2026";

    private final HttpClient cliente = HttpClient.newHttpClient();

    private final String urlBase;
    private final DataSource dataSource;
    private final UsuarioRepository usuarioRepository;
    private final JsonMapper jsonMapper;

    @Autowired
    AutenticacionIntegracionTest(@Value("${local.server.port}") int puerto,
                                 DataSource dataSource,
                                 UsuarioRepository usuarioRepository,
                                 JsonMapper jsonMapper) {
        this.urlBase = "http://localhost:" + puerto;
        this.dataSource = dataSource;
        this.usuarioRepository = usuarioRepository;
        this.jsonMapper = jsonMapper;
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
    void registroCorrectoResponde201SinLaContrasena() throws Exception {
        HttpResponse<String> respuesta = registrar("Ana Pérez", CORREO, CONTRASENA);

        assertThat(respuesta.statusCode()).isEqualTo(201);
        JsonNode cuerpo = jsonMapper.readTree(respuesta.body());
        assertThat(cuerpo.get("mensaje").asString()).isEqualTo("Usuario registrado correctamente");

        JsonNode usuario = cuerpo.get("usuario");
        assertThat(usuario.get("idUsuario").isInt()).isTrue();
        assertThat(usuario.get("nombre").asString()).isEqualTo("Ana Pérez");
        assertThat(usuario.get("correo").asString()).isEqualTo(CORREO);
        assertThat(usuario.get("rol").asString()).isEqualTo("CAJERO");
        assertThat(usuario.has("contrasena")).isFalse();
        assertThat(usuario.has("contrasenaCifrada")).isFalse();
        assertThat(respuesta.body()).doesNotContain(CONTRASENA).doesNotContain("$2a$");
    }

    @Test
    void registroIgnoraElRolEnviadoPorElClienteYAsignaCajero() throws Exception {
        String json = """
                {"nombre": "Ana Pérez", "correo": "%s", "contrasena": "%s", "rol": "ADMINISTRADOR"}
                """.formatted(CORREO, CONTRASENA);

        HttpResponse<String> respuesta = postJson("/api/auth/registro", json);

        assertThat(respuesta.statusCode()).isEqualTo(201);
        assertThat(jsonMapper.readTree(respuesta.body()).get("usuario").get("rol").asString())
                .isEqualTo("CAJERO");
        Usuario guardado = usuarioRepository.buscarPorCorreo(CORREO).orElseThrow();
        assertThat(guardado.getRol()).isEqualTo(Rol.CAJERO);
    }

    @Test
    void registroConDatosInvalidosResponde400ConLosErroresPorCampo() throws Exception {
        HttpResponse<String> respuesta = registrar("", "correo-sin-arroba", "1234567");

        assertThat(respuesta.statusCode()).isEqualTo(400);
        JsonNode cuerpo = jsonMapper.readTree(respuesta.body());
        assertThat(cuerpo.get("title").asString()).isEqualTo("Datos inválidos");
        assertThat(cuerpo.get("errores").propertyNames())
                .containsExactlyInAnyOrder("nombre", "correo", "contrasena");
    }

    @Test
    void registroConCorreoExistenteEnMayusculasResponde409() throws Exception {
        registrar("Ana Pérez", CORREO, CONTRASENA);

        HttpResponse<String> respuesta = registrar("Otra Ana", "  " + CORREO.toUpperCase() + " ", CONTRASENA);

        assertThat(respuesta.statusCode()).isEqualTo(409);
        assertThat(jsonMapper.readTree(respuesta.body()).get("detail").asString())
                .isEqualTo("Ya existe un usuario registrado con el correo " + CORREO);
    }

    private HttpResponse<String> registrar(String nombre, String correo, String contrasena)
            throws IOException, InterruptedException {
        String json = jsonMapper.writeValueAsString(new SolicitudRegistro(nombre, correo, contrasena));
        return postJson("/api/auth/registro", json);
    }

    private HttpResponse<String> postJson(String ruta, String json) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, UTF_8))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(UTF_8));
    }

    private void eliminarDatosDePrueba() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LIMPIAR_DATOS_DE_PRUEBA)) {
            statement.execute();
        }
    }

    /** Cuerpo de la petición tal como lo enviaría un cliente externo. */
    private record SolicitudRegistro(String nombre, String correo, String contrasena) {
    }
}
