package com.superinter.proveedor.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.service.ProveedorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del módulo web de proveedores: levanta la aplicación completa
 * con Tomcat y verifica servlets, JSP y PostgreSQL mediante peticiones HTTP reales.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProveedorWebIntegracionTest {

    private static final String NOMBRE_PRUEBA = "TEST-Distribuidora Café Andino";
    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "DELETE FROM proveedores WHERE nombre LIKE 'TEST-%'";

    private final HttpClient cliente = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final String urlBase;
    private final DataSource dataSource;
    private final ProveedorService proveedorService;

    @Autowired
    ProveedorWebIntegracionTest(@Value("${local.server.port}") int puerto,
                                DataSource dataSource,
                                ProveedorService proveedorService) {
        this.urlBase = "http://localhost:" + puerto;
        this.dataSource = dataSource;
        this.proveedorService = proveedorService;
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
    void listadoGeneraLaPaginaJspConElMenuDeProveedores() throws Exception {
        HttpResponse<String> respuesta = get("/proveedores");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body())
                .contains("Gestión de proveedores")
                .contains("Operaciones")
                .contains("href=\"/proveedores\"")
                .doesNotContain("${");
    }

    @Test
    void registroDesdeElFormularioGuardaEnLaBaseDeDatosYRedirige() throws Exception {
        String formulario = codificarFormulario(Map.of(
                "nombre", NOMBRE_PRUEBA,
                "contacto", "María Peña",
                "telefono", "",
                "correo", "pedidos@andino.com"));

        HttpResponse<String> respuesta = post("/proveedores/nuevo", formulario);

        assertThat(respuesta.statusCode()).isEqualTo(302);
        assertThat(respuesta.headers().firstValue("Location"))
                .hasValueSatisfying(destino -> assertThat(destino).endsWith("/proveedores?mensaje=creado"));

        ProveedorResponse guardado = proveedorService.buscar("Café Andino").getFirst();
        assertThat(guardado.contacto()).isEqualTo("María Peña");
        assertThat(guardado.telefono()).isNull();
    }

    @Test
    void eliminacionDesdeElFormularioBorraDeLaBaseDeDatos() throws Exception {
        ProveedorResponse existente = proveedorService.registrar(
                new ProveedorRequest(NOMBRE_PRUEBA, null, null, null));

        HttpResponse<String> respuesta = post("/proveedores/eliminar",
                "idProveedor=" + existente.idProveedor());

        assertThat(respuesta.statusCode()).isEqualTo(302);
        assertThat(proveedorService.buscar("Café Andino")).isEmpty();
    }

    private HttpResponse<String> get(String ruta) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta)).GET().build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(UTF_8));
    }

    private HttpResponse<String> post(String ruta, String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, UTF_8))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(UTF_8));
    }

    private String codificarFormulario(Map<String, String> campos) {
        return campos.entrySet().stream()
                .map(campo -> URLEncoder.encode(campo.getKey(), UTF_8) + "=" + URLEncoder.encode(campo.getValue(), UTF_8))
                .collect(Collectors.joining("&"));
    }

    private void eliminarDatosDePrueba() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LIMPIAR_DATOS_DE_PRUEBA)) {
            statement.executeUpdate();
        }
    }
}