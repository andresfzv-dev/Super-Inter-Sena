package com.superinter.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de la aplicación a respuestas HTTP con formato ProblemDetail (RFC 9457).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MENSAJE_VALIDACION = "Uno o más campos tienen valores inválidos";
    private static final String MENSAJE_ERROR_INTERNO = "Ocurrió un error interno. Intente nuevamente más tarde";
    private static final String MENSAJE_CAMPO_INVALIDO = "Valor inválido";
    private static final String MENSAJE_TOKEN_REQUERIDO =
            "Se requiere un token de acceso. Inicie sesión y envíelo en la cabecera Authorization: Bearer <token>";
    private static final String MENSAJE_TOKEN_INVALIDO =
            "El token de acceso no es válido o está vencido. Inicie sesión nuevamente";
    private static final String MENSAJE_ACCESO_DENEGADO = "Su rol no tiene permiso para realizar esta operación";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return crearProblema(HttpStatus.NOT_FOUND, "Recurso no encontrado", excepcion.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail manejarConflicto(ConflictoException excepcion) {
        return crearProblema(HttpStatus.CONFLICT, "Conflicto con el estado actual", excepcion.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail manejarCredencialesInvalidas(CredencialesInvalidasException excepcion) {
        return crearProblema(HttpStatus.UNAUTHORIZED, "Error en la autenticación", excepcion.getMessage());
    }

    /**
     * Petición sin token o con un token inválido o vencido a un recurso protegido. Llega aquí
     * desde {@code RespuestaErrorSeguridad}, para que la API tenga un único formato de error.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail manejarNoAutenticado(AuthenticationException excepcion) {
        String detalle = excepcion instanceof InvalidBearerTokenException
                ? MENSAJE_TOKEN_INVALIDO
                : MENSAJE_TOKEN_REQUERIDO;
        return crearProblema(HttpStatus.UNAUTHORIZED, "No autenticado", detalle);
    }

    /**
     * Usuario autenticado cuyo rol no tiene permiso sobre el recurso. Hoy ningún endpoint
     * restringe por rol; queda listo para cuando se protejan los demás módulos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail manejarAccesoDenegado(AccessDeniedException excepcion) {
        return crearProblema(HttpStatus.FORBIDDEN, "Acceso denegado", MENSAJE_ACCESO_DENEGADO);
    }

    @ExceptionHandler(AccesoDatosException.class)
    public ProblemDetail manejarAccesoDatos(AccesoDatosException excepcion) {
        log.error("Error de acceso a datos: {}", excepcion.getMessage(), excepcion);
        return crearProblema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", MENSAJE_ERROR_INTERNO);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarErrorInesperado(Exception excepcion) {
        log.error("Error inesperado", excepcion);
        return crearProblema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", MENSAJE_ERROR_INTERNO);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excepcion,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problema = excepcion.getBody();
        problema.setTitle("Datos inválidos");
        problema.setDetail(MENSAJE_VALIDACION);
        problema.setProperty("errores", obtenerErroresPorCampo(excepcion));
        return handleExceptionInternal(excepcion, problema, headers, status, request);
    }

    private Map<String, String> obtenerErroresPorCampo(MethodArgumentNotValidException excepcion) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(
                    error.getField(),
                    Objects.requireNonNullElse(error.getDefaultMessage(), MENSAJE_CAMPO_INVALIDO));
        }
        return errores;
    }

    private ProblemDetail crearProblema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}