package com.superinter.proveedor.web;

import java.io.IOException;
import java.util.Map;

import com.superinter.common.web.AtributosVista;
import com.superinter.proveedor.service.ProveedorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Muestra el listado de proveedores con búsqueda por nombre (GET /proveedores).
 */
public class ProveedorListaServlet extends HttpServlet {

    static final String RUTA = "/proveedores";
    static final String VISTA = "/WEB-INF/jsp/proveedores/lista.jsp";

    /**
     * Mensajes que se muestran tras un redirect exitoso. La URL solo transporta un código
     * (?mensaje=creado), nunca el texto, para que no se pueda inyectar contenido en la página.
     */
    private static final Map<String, String> MENSAJES_EXITO = Map.of(
            "creado", "Proveedor registrado correctamente.",
            "actualizado", "Proveedor actualizado correctamente.",
            "eliminado", "Proveedor eliminado correctamente.");

    private final transient ProveedorService proveedorService;

    public ProveedorListaServlet(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("nombre");

        request.setAttribute(AtributosVista.TITULO_PAGINA, "Proveedores");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "proveedores");
        request.setAttribute("proveedores", proveedorService.buscar(nombre));
        request.setAttribute("nombreBuscado", nombre);
        request.setAttribute("mensajeExito", obtenerMensajeExito(request.getParameter("mensaje")));

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private String obtenerMensajeExito(String codigoMensaje) {
        // Map.of(...) lanza NullPointerException si se consulta una clave nula.
        return codigoMensaje == null ? null : MENSAJES_EXITO.get(codigoMensaje);
    }
}