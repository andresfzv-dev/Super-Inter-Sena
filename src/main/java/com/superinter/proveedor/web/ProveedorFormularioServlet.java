package com.superinter.proveedor.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.common.web.AtributosVista;
import com.superinter.common.web.ParametrosWeb;
import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.service.ProveedorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

/**
 * Registro (/proveedores/nuevo) y edición (/proveedores/editar) de proveedores.
 * GET muestra el formulario; POST valida, guarda y redirige al listado (Post/Redirect/Get).
 *
 * <p>Todos los campos del proveedor son texto, así que {@link ProveedorRequest} se usa
 * directamente como modelo del formulario: no hay conversiones de tipo que puedan fallar.
 */
public class ProveedorFormularioServlet extends HttpServlet {

    static final String VISTA = "/WEB-INF/jsp/proveedores/formulario.jsp";
    static final String RUTA_NUEVO = "/proveedores/nuevo";
    static final String RUTA_EDITAR = "/proveedores/editar";

    private static final ProveedorRequest FORMULARIO_VACIO = new ProveedorRequest("", null, null, null);

    private final transient ProveedorService proveedorService;
    private final transient Validator validador;

    public ProveedorFormularioServlet(ProveedorService proveedorService, Validator validador) {
        this.proveedorService = proveedorService;
        this.validador = validador;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esEdicion(request)) {
            mostrarFormulario(request, response, null, FORMULARIO_VACIO, Map.of(), null);
            return;
        }

        Integer idProveedor = ParametrosWeb.enteroOpcional(request.getParameter("id"));
        if (idProveedor == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Proveedor no encontrado");
            return;
        }
        try {
            ProveedorResponse proveedor = proveedorService.obtenerPorId(idProveedor);
            mostrarFormulario(request, response, idProveedor, aSolicitud(proveedor), Map.of(), null);
        } catch (RecursoNoEncontradoException excepcion) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, excepcion.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProveedor = ParametrosWeb.enteroOpcional(request.getParameter("idProveedor"));
        if (esEdicion(request) && idProveedor == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el identificador del proveedor");
            return;
        }

        ProveedorRequest solicitud = new ProveedorRequest(
                request.getParameter("nombre"),
                request.getParameter("contacto"),
                request.getParameter("telefono"),
                request.getParameter("correo"));

        Map<String, String> errores = new LinkedHashMap<>();
        // putIfAbsent: si un campo incumple varias reglas, se muestra solo el primer mensaje.
        validador.validate(solicitud).forEach(violacion ->
                errores.putIfAbsent(violacion.getPropertyPath().toString(), violacion.getMessage()));

        if (!errores.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarFormulario(request, response, idProveedor, solicitud, errores, null);
            return;
        }

        try {
            String codigoMensaje = guardar(request, idProveedor, solicitud);
            response.sendRedirect(request.getContextPath() + ProveedorListaServlet.RUTA + "?mensaje=" + codigoMensaje);
        } catch (ConflictoException excepcion) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mostrarFormulario(request, response, idProveedor, solicitud, Map.of(), excepcion.getMessage());
        } catch (RecursoNoEncontradoException excepcion) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mostrarFormulario(request, response, idProveedor, solicitud, Map.of(), excepcion.getMessage());
        }
    }

    private String guardar(HttpServletRequest request, Integer idProveedor, ProveedorRequest solicitud) {
        if (esEdicion(request)) {
            proveedorService.actualizar(idProveedor, solicitud);
            return "actualizado";
        }
        proveedorService.registrar(solicitud);
        return "creado";
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Integer idProveedor, ProveedorRequest formulario,
                                   Map<String, String> errores, String errorGeneral)
            throws ServletException, IOException {
        boolean edicion = esEdicion(request);

        request.setAttribute(AtributosVista.TITULO_PAGINA, edicion ? "Editar proveedor" : "Registrar proveedor");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "proveedores");
        request.setAttribute("idProveedor", idProveedor);
        request.setAttribute("formulario", formulario);
        request.setAttribute("errores", errores);
        request.setAttribute("errorGeneral", errorGeneral);
        request.setAttribute("esEdicion", edicion);
        request.setAttribute("accionFormulario", request.getContextPath() + (edicion ? RUTA_EDITAR : RUTA_NUEVO));

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * El mismo servlet atiende dos rutas; la ruta con la que fue invocado define el modo.
     */
    private boolean esEdicion(HttpServletRequest request) {
        return RUTA_EDITAR.equals(request.getServletPath());
    }

    private ProveedorRequest aSolicitud(ProveedorResponse proveedor) {
        return new ProveedorRequest(
                proveedor.nombre(), proveedor.contacto(), proveedor.telefono(), proveedor.correo());
    }
}