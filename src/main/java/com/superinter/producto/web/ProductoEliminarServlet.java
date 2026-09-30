package com.superinter.producto.web;

import java.io.IOException;

import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.common.web.AtributosVista;
import com.superinter.common.web.ParametrosWeb;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.service.ProductoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Eliminación de productos (/productos/eliminar).
 * GET muestra la confirmación; POST elimina y redirige al listado.
 */
public class ProductoEliminarServlet extends HttpServlet {

    static final String VISTA = "/WEB-INF/jsp/productos/confirmar-eliminacion.jsp";
    static final String RUTA = "/productos/eliminar";

    private final transient ProductoService productoService;

    public ProductoEliminarServlet(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProducto = ParametrosWeb.enteroOpcional(request.getParameter("id"));
        if (idProducto == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
            return;
        }
        try {
            mostrarConfirmacion(request, response, productoService.obtenerPorId(idProducto), null);
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProducto = ParametrosWeb.enteroOpcional(request.getParameter("idProducto"));
        if (idProducto == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el identificador del producto");
            return;
        }
        try {
            productoService.eliminar(idProducto);
            response.sendRedirect(request.getContextPath() + "/productos?mensaje=eliminado");
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        } catch (ConflictoException excepcion) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mostrarConfirmacion(request, response, productoService.obtenerPorId(idProducto), excepcion.getMessage());
        }
    }

    private void mostrarConfirmacion(HttpServletRequest request, HttpServletResponse response,
                                     ProductoResponse producto, String errorGeneral)
            throws ServletException, IOException {

        request.setAttribute(AtributosVista.TITULO_PAGINA, "Eliminar producto");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "productos");
        request.setAttribute("producto", producto);
        request.setAttribute("errorGeneral", errorGeneral);

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}