package com.superinter.producto.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepositoryEnMemoria;
import com.superinter.categoria.service.CategoriaService;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.repository.ProductoRepositoryEnMemoria;
import com.superinter.producto.service.ProductoService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProductoFormularioServletTest {

    private static final int ID_ABARROTES = 1;

    private ProductoService productoService;
    private ProductoFormularioServlet servlet;

    @BeforeEach
    void configurar() {
        Categoria abarrotes = Categoria.builder().idCategoria(ID_ABARROTES).nombre("Abarrotes").build();
        CategoriaService categoriaService = new CategoriaService(new CategoriaRepositoryEnMemoria(List.of(abarrotes)));
        productoService = new ProductoService(new ProductoRepositoryEnMemoria(), categoriaService);

        servlet = new ProductoFormularioServlet(
                productoService, categoriaService, Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void getNuevoMuestraElFormularioVacio() throws Exception {
        MockHttpServletRequest request = crearPeticion("GET", ProductoFormularioServlet.RUTA_NUEVO);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getForwardedUrl()).isEqualTo(ProductoFormularioServlet.VISTA);
        assertThat(request.getAttribute("esEdicion")).isEqualTo(false);
        assertThat(request.getAttribute("formulario")).isEqualTo(ProductoFormulario.vacio());
    }

    @Test
    void getEditarCargaLosDatosDelProducto() throws Exception {
        ProductoResponse existente = registrarProducto("770-001");
        MockHttpServletRequest request = crearPeticion("GET", ProductoFormularioServlet.RUTA_EDITAR);
        request.addParameter("id", String.valueOf(existente.idProducto()));

        servlet.service(request, new MockHttpServletResponse());

        ProductoFormulario formulario = (ProductoFormulario) request.getAttribute("formulario");
        assertThat(request.getAttribute("esEdicion")).isEqualTo(true);
        assertThat(formulario.codigo()).isEqualTo("770-001");
        assertThat(formulario.precio()).isEqualTo("4500");
    }

    @Test
    void getEditarProductoInexistenteRespondeNotFound() throws Exception {
        MockHttpServletRequest request = crearPeticion("GET", ProductoFormularioServlet.RUTA_EDITAR);
        request.addParameter("id", "999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void postValidoRegistraElProductoYRedirigeAlListado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(crearPostValido(ProductoFormularioServlet.RUTA_NUEVO, "770-001"), response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/productos?mensaje=creado");
        assertThat(productoService.listarTodos()).hasSize(1);
    }

    @Test
    void postConCamposInvalidosVuelveAlFormularioConLosErroresYLoEscrito() throws Exception {
        MockHttpServletRequest request = crearPostValido(ProductoFormularioServlet.RUTA_NUEVO, "");
        request.setParameter("precio", "0");
        request.setParameter("stock", "-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getForwardedUrl()).isEqualTo(ProductoFormularioServlet.VISTA);
        assertThat(obtenerErrores(request)).containsOnlyKeys("codigo", "precio", "stock");
        assertThat(((ProductoFormulario) request.getAttribute("formulario")).stock()).isEqualTo("-1");
        assertThat(productoService.listarTodos()).isEmpty();
    }

    @Test
    void postConPrecioNoNumericoMuestraErrorDeFormato() throws Exception {
        MockHttpServletRequest request = crearPostValido(ProductoFormularioServlet.RUTA_NUEVO, "770-001");
        request.setParameter("precio", "abc");

        servlet.service(request, new MockHttpServletResponse());

        assertThat(obtenerErrores(request))
                .containsEntry("precio", "El precio debe ser un número válido");
    }

    @Test
    void postConCodigoDuplicadoRespondeConflictoSinRegistrar() throws Exception {
        registrarProducto("770-001");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = crearPostValido(ProductoFormularioServlet.RUTA_NUEVO, "770-001");

        servlet.service(request, response);

        assertThat(response.getStatus()).isEqualTo(409);
        assertThat((String) request.getAttribute("errorGeneral")).contains("Ya existe un producto");
        assertThat(productoService.listarTodos()).hasSize(1);
    }

    @Test
    void postEditarActualizaElProductoYRedirigeAlListado() throws Exception {
        ProductoResponse existente = registrarProducto("770-001");
        MockHttpServletRequest request = crearPostValido(ProductoFormularioServlet.RUTA_EDITAR, "770-001");
        request.addParameter("idProducto", String.valueOf(existente.idProducto()));
        request.setParameter("nombre", "Arroz Diana 5kg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/productos?mensaje=actualizado");
        assertThat(productoService.obtenerPorId(existente.idProducto()).nombre()).isEqualTo("Arroz Diana 5kg");
    }

    private ProductoResponse registrarProducto(String codigo) {
        return productoService.registrar(new ProductoRequest(
                codigo, "Arroz Diana 1kg", new BigDecimal("4500"), 20, null, ID_ABARROTES));
    }

    private MockHttpServletRequest crearPeticion(String metodo, String ruta) {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, ruta);
        request.setServletPath(ruta);
        return request;
    }

    private MockHttpServletRequest crearPostValido(String ruta, String codigo) {
        MockHttpServletRequest request = crearPeticion("POST", ruta);
        request.addParameter("codigo", codigo);
        request.addParameter("nombre", "Arroz Diana 1kg");
        request.addParameter("precio", "4500");
        request.addParameter("stock", "20");
        request.addParameter("fechaVencimiento", "2027-03-15");
        request.addParameter("idCategoria", String.valueOf(ID_ABARROTES));
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> obtenerErrores(MockHttpServletRequest request) {
        return (Map<String, String>) request.getAttribute("errores");
    }
}