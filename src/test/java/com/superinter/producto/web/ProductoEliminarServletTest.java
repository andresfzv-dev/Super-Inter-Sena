package com.superinter.producto.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepositoryEnMemoria;
import com.superinter.categoria.service.CategoriaService;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.repository.ProductoRepositoryEnMemoria;
import com.superinter.producto.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProductoEliminarServletTest {

    private static final int ID_ABARROTES = 1;

    private ProductoService productoService;
    private ProductoEliminarServlet servlet;
    private ProductoResponse existente;

    @BeforeEach
    void configurar() {
        Categoria abarrotes = Categoria.builder().idCategoria(ID_ABARROTES).nombre("Abarrotes").build();
        CategoriaService categoriaService = new CategoriaService(new CategoriaRepositoryEnMemoria(List.of(abarrotes)));
        productoService = new ProductoService(new ProductoRepositoryEnMemoria(), categoriaService);

        existente = productoService.registrar(new ProductoRequest(
                "770-001", "Arroz Diana 1kg", new BigDecimal("4500"), 20, null, ID_ABARROTES));

        servlet = new ProductoEliminarServlet(productoService);
    }

    @Test
    void getMuestraLaConfirmacionSinEliminar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProductoEliminarServlet.RUTA);
        request.addParameter("id", String.valueOf(existente.idProducto()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProductoEliminarServlet.VISTA);
        assertThat(request.getAttribute("producto")).isEqualTo(existente);
        assertThat(productoService.listarTodos()).hasSize(1);
    }

    @Test
    void getProductoInexistenteRespondeNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ProductoEliminarServlet.RUTA);
        request.addParameter("id", "999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void postEliminaElProductoYRedirigeAlListado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ProductoEliminarServlet.RUTA);
        request.addParameter("idProducto", String.valueOf(existente.idProducto()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/productos?mensaje=eliminado");
        assertThat(productoService.listarTodos()).isEmpty();
    }

    @Test
    void postSinIdentificadorRespondeBadRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(new MockHttpServletRequest("POST", ProductoEliminarServlet.RUTA), response);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(productoService.listarTodos()).hasSize(1);
    }
}