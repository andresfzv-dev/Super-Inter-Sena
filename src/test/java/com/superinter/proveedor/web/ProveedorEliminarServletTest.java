package com.superinter.proveedor.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.repository.ProveedorRepositoryEnMemoria;
import com.superinter.proveedor.service.ProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProveedorEliminarServletTest {

    private ProveedorRepositoryEnMemoria repositorio;
    private ProveedorService proveedorService;
    private ProveedorEliminarServlet servlet;
    private ProveedorResponse existente;

    @BeforeEach
    void configurar() {
        repositorio = new ProveedorRepositoryEnMemoria();
        proveedorService = new ProveedorService(repositorio);
        existente = proveedorService.registrar(new ProveedorRequest("Postobón", null, null, null));
        servlet = new ProveedorEliminarServlet(proveedorService);
    }

    @Test
    void getMuestraLaConfirmacionSinEliminar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProveedorEliminarServlet.RUTA);
        request.addParameter("id", String.valueOf(existente.idProveedor()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProveedorEliminarServlet.VISTA);
        assertThat(request.getAttribute("proveedor")).isEqualTo(existente);
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    @Test
    void getProveedorInexistenteRespondeNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProveedorEliminarServlet.RUTA);
        request.addParameter("id", "999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void postEliminaYRedirigeAlListado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(crearPost(existente.idProveedor()), response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/proveedores?mensaje=eliminado");
        assertThat(proveedorService.buscar(null)).isEmpty();
    }

    @Test
    void postProveedorConComprasRespondeConflictoYConservaElProveedor() throws Exception {
        repositorio.registrarCompraDe(existente.idProveedor());
        MockHttpServletRequest request = crearPost(existente.idProveedor());
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getForwardedUrl()).isEqualTo(ProveedorEliminarServlet.VISTA);
        assertThat((String) request.getAttribute("errorGeneral")).contains("tiene compras registradas");
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    @Test
    void postSinIdentificadorRespondeBadRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(new MockHttpServletRequest("POST", ProveedorEliminarServlet.RUTA), response);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    private MockHttpServletRequest crearPost(int idProveedor) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ProveedorEliminarServlet.RUTA);
        request.addParameter("idProveedor", String.valueOf(idProveedor));
        return request;
    }
}