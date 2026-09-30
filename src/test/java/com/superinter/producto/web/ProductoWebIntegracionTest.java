package com.superinter.producto.web;

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

import com.superinter.categoria.repository.CategoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del módulo web: levanta la aplicación completa con Tomcat
 * y verifica servlets, JSP y base de datos mediante peticiones HTTP reales.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductoWebIntegracionTest {

    private static final String CODIGO_PRUEBA = "TEST-WEB-001";
    private static final String NOMBRE_PRUEBA = "Café Águila Roja 500g";
    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "DELETE FROM productos WHERE codigo LIKE 'TEST-%'";

    private final HttpClient cliente = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final String urlBase;
    private final DataSource dataSource;
    private final int idCategoria;

    @Autowired
    ProductoWebIntegracionTest(@Value("${local.server.port}") int puerto,
                               DataSource dataSource,
                               CategoriaRepository categoriaRepository) {
        this.urlBase = "http://localhost:" + puerto;
        this.dataSource = dataSource;
        this.idCategoria = categoriaRepository.listarTodas().getFirst().getIdCategoria();
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
    void listadoGeneraLaPaginaJspConLosFragmentosYLosEstilos() throws Exception {
        HttpResponse<String> respuesta = get("/productos");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.headers().firstValue("Content-Type"))
                .hasValueSatisfying(tipo -> assertThat(tipo).startsWith("text/html"));
        assertThat(respuesta.body())
                .contains("Gestión de productos")
                .contains("sidebar__brand")
                .contains("/css/variables.css")
                .doesNotContain("${");
    }

    @Test
    void formularioDeRegistroGeneraLosCamposYLasCategorias() throws Exception {
        HttpResponse<String> respuesta = get("/productos/nuevo");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body())
                .contains("method=\"post\"")
                .contains("name=\"codigo\"")
                .contains("name=\"idCategoria\"")
                .contains("value=\"" + idCategoria + "\"");
    }

    @Test
    void registroDesdeElFormularioGuardaEnLaBaseDeDatosYRedirige() throws Exception {
        String formulario = codificarFormulario(Map.of(
                "codigo", CODIGO_PRUEBA,
                "nombre", NOMBRE_PRUEBA,
                "precio", "12500",
                "stock", "8",
                "fechaVencimiento", "",
                "idCategoria", String.valueOf(idCategoria)));

        HttpResponse<String> respuesta = post("/productos/nuevo", formulario);

        assertThat(respuesta.statusCode()).isEqualTo(302);
        assertThat(respuesta.headers().firstValue("Location"))
                .hasValueSatisfying(destino -> assertThat(destino).endsWith("/productos?mensaje=creado"));

        HttpResponse<String> listado = get("/productos?nombre=" + URLEncoder.encode("Café Águila", UTF_8));
        assertThat(listado.body())
                .contains(CODIGO_PRUEBA)
                .contains(NOMBRE_PRUEBA);
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