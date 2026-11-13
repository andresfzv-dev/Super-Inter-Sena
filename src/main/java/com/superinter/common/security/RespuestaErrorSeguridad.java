package com.superinter.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Respuesta de Spring Security cuando falta la autenticación (401) o el rol no tiene
 * permiso (403).
 *
 * <p>Los errores de seguridad ocurren en los filtros, antes de llegar a un controlador, así
 * que {@code @RestControllerAdvice} no los ve. Sin esta clase, Spring Security respondería
 * con un cuerpo vacío. Aquí se entregan al {@link HandlerExceptionResolver} de Spring MVC
 * para que {@code GlobalExceptionHandler} los convierta en ProblemDetail con el mismo
 * formato JSON que el resto de la API.
 */
@Component
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolutorExcepciones;

    // Hay varios HandlerExceptionResolver en el contexto; este es el compuesto que usa el
    // DispatcherServlet y que incluye los @ExceptionHandler de GlobalExceptionHandler.
    public RespuestaErrorSeguridad(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolutorExcepciones) {
        this.resolutorExcepciones = resolutorExcepciones;
    }

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
                         AuthenticationException excepcion) {
        // RFC 6750: una respuesta 401 a un recurso protegido con Bearer debe indicar el
        // esquema de autenticación esperado y, si el token falló, el motivo.
        String desafio = excepcion instanceof InvalidBearerTokenException
                ? "Bearer error=\"invalid_token\""
                : "Bearer";
        respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, desafio);
        resolutorExcepciones.resolveException(peticion, respuesta, null, excepcion);
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
                       AccessDeniedException excepcion) {
        resolutorExcepciones.resolveException(peticion, respuesta, null, excepcion);
    }
}
