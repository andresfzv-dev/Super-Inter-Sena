package com.superinter.proveedor.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.repository.ProveedorRepositoryEnMemoria;
import com.superinter.proveedor.service.ProveedorService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProveedorFormularioServletTest {

    private ProveedorService proveedorService;
    private ProveedorFormularioServlet servlet;

    @BeforeEach
    void configurar() {
        proveedorService = new ProveedorService(new ProveedorRepositoryEnMemoria());
        servlet = new ProveedorFormularioServlet(
                proveedorService, Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void getNuevoMuestraElFormularioVacioEnModoRegistro() throws Exception {
        MockHttpServletRequest request = crearPeticion("GET", ProveedorFormularioServlet.RUTA_NUEVO);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProveedorFormularioServlet.VISTA);
        assertThat(request.getAttribute("esEdicion")).isEqualTo(false);
        assertThat(obtenerFormulario(request).nombre()).isEmpty();
    }

    @Test
    void getEditarCargaLosDatosDelProveedor() throws Exception {
        ProveedorResponse existente = registrar("Postobón");
        MockHttpServletRequest request = crearPeticion("GET", ProveedorFormularioServlet.RUTA_EDITAR);
        request.addParameter("id", String.valueOf(existente.idProveedor()));

        servlet.service(request, new MockHttpServletResponse());

        assertThat(request.getAttribute("esEdicion")).isEqualTo(true);
        assertThat(request.getAttribute("idProveedor")).isEqualTo(existente.idProveedor());
        assertThat(obtenerFormulario(request).correo()).isEqualTo("ventas@ejemplo.com");
    }

    @Test
    void getEditarProveedorInexistenteRespondeNotFound() throws Exception {
        MockHttpServletRequest request = crearPeticion("GET", ProveedorFormularioServlet.RUTA_EDITAR);
        request.addParameter("id", "999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void postValidoRegistraYRedirigeAlListado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(crearPost(ProveedorFormularioServlet.RUTA_NUEVO, "Postobón"), response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/proveedores?mensaje=creado");
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    @Test
    void postConCamposInvalidosVuelveAlFormularioConErroresYLoEscrito() throws Exception {
        MockHttpServletRequest request = crearPost(ProveedorFormularioServlet.RUTA_NUEVO, "  ");
        request.setParameter("telefono", "abc");
        request.setParameter("correo", "ventas@postobon");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getForwardedUrl()).isEqualTo(ProveedorFormularioServlet.VISTA);
        assertThat(obtenerErrores(request)).containsOnlyKeys("nombre", "telefono", "correo");
        assertThat(obtenerFormulario(request).correo()).isEqualTo("ventas@postobon");
        assertThat(proveedorService.buscar(null)).isEmpty();
    }

    @Test
    void postConNombreDuplicadoRespondeConflictoSinRegistrar() throws Exception {
        registrar("Postobón");
        MockHttpServletRequest request = crearPost(ProveedorFormularioServlet.RUTA_NUEVO, "POSTOBÓN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(409);
        assertThat((String) request.getAttribute("errorGeneral")).contains("Ya existe un proveedor");
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    @Test
    void postEditarActualizaYRedirigeAlListado() throws Exception {
        ProveedorResponse existente = registrar("Postobón");
        MockHttpServletRequest request = crearPost(ProveedorFormularioServlet.RUTA_EDITAR, "Postobón");
        request.addParameter("idProveedor", String.valueOf(existente.idProveedor()));
        request.setParameter("contacto", "Carlos Ruiz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/proveedores?mensaje=actualizado");
        assertThat(proveedorService.obtenerPorId(existente.idProveedor()).contacto()).isEqualTo("Carlos Ruiz");
    }

    @Test
    void postEditarSinIdentificadorRespondeBadRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(crearPost(ProveedorFormularioServlet.RUTA_EDITAR, "Postobón"), response);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    private ProveedorResponse registrar(String nombre) {
        return proveedorService.registrar(
                new ProveedorRequest(nombre, "Laura Gómez", "3001234567", "ventas@ejemplo.com"));
    }

    /**
     * Tomcat calcula el servletPath a partir del registro del servlet; en una petición
     * simulada hay que asignarlo, porque el servlet lo usa para distinguir registro y edición.
     */
    private MockHttpServletRequest crearPeticion(String metodo, String ruta) {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, ruta);
        request.setServletPath(ruta);
        return request;
    }

    private MockHttpServletRequest crearPost(String ruta, String nombre) {
        MockHttpServletRequest request = crearPeticion("POST", ruta);
        request.addParameter("nombre", nombre);
        request.addParameter("contacto", "Laura Gómez");
        request.addParameter("telefono", "3001234567");
        request.addParameter("correo", "ventas@ejemplo.com");
        return request;
    }

    private ProveedorRequest obtenerFormulario(MockHttpServletRequest request) {
        return (ProveedorRequest) request.getAttribute("formulario");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> obtenerErrores(MockHttpServletRequest request) {
        return (Map<String, String>) request.getAttribute("errores");
    }
}