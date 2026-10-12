package com.superinter.proveedor.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.superinter.common.web.AtributosVista;
import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.repository.ProveedorRepositoryEnMemoria;
import com.superinter.proveedor.service.ProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProveedorListaServletTest {

    private ProveedorListaServlet servlet;

    @BeforeEach
    void configurar() {
        ProveedorService proveedorService = new ProveedorService(new ProveedorRepositoryEnMemoria());
        proveedorService.registrar(new ProveedorRequest("Postobón", "Laura Gómez", null, null));
        proveedorService.registrar(new ProveedorRequest("Alpina", null, null, null));

        servlet = new ProveedorListaServlet(proveedorService);
    }

    @Test
    void getEnviaLosProveedoresALaVistaDelListado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProveedorListaServlet.RUTA);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProveedorListaServlet.VISTA);
        assertThat(obtenerProveedores(request)).hasSize(2);
        assertThat(request.getAttribute(AtributosVista.PAGINA_ACTIVA)).isEqualTo("proveedores");
    }

    @Test
    void getFiltraPorElNombreRecibidoYLoConservaParaElFormulario() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProveedorListaServlet.RUTA);
        request.addParameter("nombre", "post");

        servlet.service(request, new MockHttpServletResponse());

        assertThat(obtenerProveedores(request))
                .extracting(ProveedorResponse::nombre)
                .containsExactly("Postobón");
        assertThat(request.getAttribute("nombreBuscado")).isEqualTo("post");
    }

    @Test
    void soloMuestraMensajesDeExitoConocidos() throws Exception {
        MockHttpServletRequest conocido = new MockHttpServletRequest("GET", ProveedorListaServlet.RUTA);
        conocido.addParameter("mensaje", "eliminado");
        MockHttpServletRequest desconocido = new MockHttpServletRequest("GET", ProveedorListaServlet.RUTA);
        desconocido.addParameter("mensaje", "<script>alert(1)</script>");

        servlet.service(conocido, new MockHttpServletResponse());
        servlet.service(desconocido, new MockHttpServletResponse());

        assertThat(conocido.getAttribute("mensajeExito")).isEqualTo("Proveedor eliminado correctamente.");
        assertThat(desconocido.getAttribute("mensajeExito")).isNull();
    }

    @Test
    void postNoEstaPermitido() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(new MockHttpServletRequest("POST", ProveedorListaServlet.RUTA), response);

        assertThat(response.getStatus()).isEqualTo(405);
    }

    @SuppressWarnings("unchecked")
    private List<ProveedorResponse> obtenerProveedores(MockHttpServletRequest request) {
        return (List<ProveedorResponse>) request.getAttribute("proveedores");
    }
}