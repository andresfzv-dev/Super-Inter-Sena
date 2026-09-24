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

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return crearProblema(HttpStatus.NOT_FOUND, "Recurso no encontrado", excepcion.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail manejarConflicto(ConflictoException excepcion) {
        return crearProblema(HttpStatus.CONFLICT, "Conflicto con el estado actual", excepcion.getMessage());
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