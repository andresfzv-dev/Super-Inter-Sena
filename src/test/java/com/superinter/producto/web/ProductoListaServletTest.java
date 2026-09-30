package com.superinter.producto.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepositoryEnMemoria;
import com.superinter.categoria.service.CategoriaService;
import com.superinter.common.web.AtributosVista;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.repository.ProductoRepositoryEnMemoria;
import com.superinter.producto.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProductoListaServletTest {

    private static final int ID_ABARROTES = 1;

    private ProductoListaServlet servlet;

    @BeforeEach
    void configurar() {
        Categoria abarrotes = Categoria.builder().idCategoria(ID_ABARROTES).nombre("Abarrotes").build();
        CategoriaService categoriaService = new CategoriaService(new CategoriaRepositoryEnMemoria(List.of(abarrotes)));
        ProductoService productoService = new ProductoService(new ProductoRepositoryEnMemoria(), categoriaService);

        productoService.registrar(new ProductoRequest(
                "770-001", "Arroz Diana 1kg", new BigDecimal("4500"), 20, null, ID_ABARROTES));

        servlet = new ProductoListaServlet(productoService, categoriaService);
    }

    @Test
    void getEnviaLosProductosALaVistaDelListado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/productos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProductoListaServlet.VISTA);
        assertThat(obtenerProductos(request)).hasSize(1);
        assertThat(request.getAttribute(AtributosVista.TITULO_PAGINA)).isEqualTo("Productos");
    }

    @Test
    void getFiltraPorElNombreRecibidoComoParametro() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/productos");
        request.addParameter("nombre", "leche");

        servlet.service(request, new MockHttpServletResponse());

        assertThat(obtenerProductos(request)).isEmpty();
        assertThat(request.getAttribute("nombreBuscado")).isEqualTo("leche");
    }

    @Test
    void soloMuestraMensajesDeExitoConocidos() throws Exception {
        MockHttpServletRequest conocido = new MockHttpServletRequest("GET", "/productos");
        conocido.addParameter("mensaje", "creado");
        MockHttpServletRequest desconocido = new MockHttpServletRequest("GET", "/productos");
        desconocido.addParameter("mensaje", "<script>alert(1)</script>");

        servlet.service(conocido, new MockHttpServletResponse());
        servlet.service(desconocido, new MockHttpServletResponse());

        assertThat(conocido.getAttribute("mensajeExito")).isEqualTo("Producto registrado correctamente.");
        assertThat(desconocido.getAttribute("mensajeExito")).isNull();
    }

    @Test
    void postNoEstaPermitido() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(new MockHttpServletRequest("POST", "/productos"), response);

        assertThat(response.getStatus()).isEqualTo(405);
    }

    @SuppressWarnings("unchecked")
    private List<ProductoResponse> obtenerProductos(MockHttpServletRequest request) {
        return (List<ProductoResponse>) request.getAttribute("productos");
    }
}