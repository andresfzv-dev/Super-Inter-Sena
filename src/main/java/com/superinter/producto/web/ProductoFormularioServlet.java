package com.superinter.producto.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.superinter.categoria.service.CategoriaService;
import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.common.web.AtributosVista;
import com.superinter.common.web.ParametrosWeb;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.service.ProductoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

/**
 * Formulario de registro (/productos/nuevo) y edición (/productos/editar) de productos.
 * GET muestra el formulario; POST valida, guarda y redirige al listado.
 */
public class ProductoFormularioServlet extends HttpServlet {

    static final String VISTA = "/WEB-INF/jsp/productos/formulario.jsp";
    static final String RUTA_NUEVO = "/productos/nuevo";
    static final String RUTA_EDITAR = "/productos/editar";

    private final transient ProductoService productoService;
    private final transient CategoriaService categoriaService;
    private final transient Validator validador;

    public ProductoFormularioServlet(ProductoService productoService,
                                     CategoriaService categoriaService,
                                     Validator validador) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.validador = validador;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esEdicion(request)) {
            mostrarFormulario(request, response, ProductoFormulario.vacio(), Map.of(), null);
            return;
        }

        Integer idProducto = ParametrosWeb.enteroOpcional(request.getParameter("id"));
        if (idProducto == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
            return;
        }
        try {
            ProductoFormulario formulario = ProductoFormulario.desde(productoService.obtenerPorId(idProducto));
            mostrarFormulario(request, response, formulario, Map.of(), null);
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ProductoFormulario formulario = ProductoFormulario.desde(request);

        if (esEdicion(request) && formulario.idProducto() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el identificador del producto");
            return;
        }

        Map<String, String> errores = new LinkedHashMap<>();
        ProductoRequest solicitud = formulario.aSolicitud(errores);
        validador.validate(solicitud).forEach(violacion ->
                errores.putIfAbsent(violacion.getPropertyPath().toString(), violacion.getMessage()));

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, formulario, errores, null);
            return;
        }

        try {
            String codigoMensaje = guardar(request, formulario, solicitud);
            response.sendRedirect(request.getContextPath() + "/productos?mensaje=" + codigoMensaje);
        } catch (ConflictoException excepcion) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mostrarFormulario(request, response, formulario, Map.of(), excepcion.getMessage());
        } catch (RecursoNoEncontradoException excepcion) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mostrarFormulario(request, response, formulario, Map.of(), excepcion.getMessage());
        }
    }

    private String guardar(HttpServletRequest request, ProductoFormulario formulario, ProductoRequest solicitud) {
        if (esEdicion(request)) {
            productoService.actualizar(formulario.idProducto(), solicitud);
            return "actualizado";
        }
        productoService.registrar(solicitud);
        return "creado";
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   ProductoFormulario formulario, Map<String, String> errores,
                                   String errorGeneral) throws ServletException, IOException {
        boolean edicion = esEdicion(request);

        request.setAttribute(AtributosVista.TITULO_PAGINA, edicion ? "Editar producto" : "Registrar producto");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "productos");
        request.setAttribute("formulario", formulario);
        request.setAttribute("errores", errores);
        request.setAttribute("errorGeneral", errorGeneral);
        request.setAttribute("categorias", categoriaService.listarTodas());
        request.setAttribute("esEdicion", edicion);
        request.setAttribute("accionFormulario", request.getContextPath() + (edicion ? RUTA_EDITAR : RUTA_NUEVO));

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private boolean esEdicion(HttpServletRequest request) {
        return RUTA_EDITAR.equals(request.getServletPath());
    }
}