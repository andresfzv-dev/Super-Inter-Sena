package com.superinter.proveedor.web;

import java.io.IOException;

import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.common.web.AtributosVista;
import com.superinter.common.web.ParametrosWeb;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.service.ProveedorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Eliminación de proveedores (/proveedores/eliminar).
 * GET muestra la confirmación sin modificar datos; POST elimina y redirige al listado.
 */
public class ProveedorEliminarServlet extends HttpServlet {

    static final String VISTA = "/WEB-INF/jsp/proveedores/confirmar-eliminacion.jsp";
    static final String RUTA = "/proveedores/eliminar";

    private final transient ProveedorService proveedorService;

    public ProveedorEliminarServlet(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProveedor = ParametrosWeb.enteroOpcional(request.getParameter("id"));
        if (idProveedor == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Proveedor no encontrado");
            return;
        }
        try {
            mostrarConfirmacion(request, response, proveedorService.obtenerPorId(idProveedor), null);
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProveedor = ParametrosWeb.enteroOpcional(request.getParameter("idProveedor"));
        if (idProveedor == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el identificador del proveedor");
            return;
        }
        try {
            proveedorService.eliminar(idProveedor);
            response.sendRedirect(request.getContextPath() + ProveedorListaServlet.RUTA + "?mensaje=eliminado");
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        } catch (ConflictoException excepcion) {
            // El proveedor tiene compras: se vuelve a mostrar la confirmación con el motivo.
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mostrarConfirmacion(request, response, proveedorService.obtenerPorId(idProveedor), excepcion.getMessage());
        }
    }

    private void mostrarConfirmacion(HttpServletRequest request, HttpServletResponse response,
                                     ProveedorResponse proveedor, String errorGeneral)
            throws ServletException, IOException {

        request.setAttribute(AtributosVista.TITULO_PAGINA, "Eliminar proveedor");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "proveedores");
        request.setAttribute("proveedor", proveedor);
        request.setAttribute("errorGeneral", errorGeneral);

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}