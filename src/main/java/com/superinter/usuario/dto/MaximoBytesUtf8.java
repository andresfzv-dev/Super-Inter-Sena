package com.superinter.usuario.dto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Limita un texto por su tamaño en bytes UTF-8 y no por su número de caracteres.
 *
 * <p>Se necesita para la contraseña: BCrypt solo procesa los primeros 72 bytes y
 * {@code BCryptPasswordEncoder} lanza una excepción si recibe más. {@code @Size} no basta
 * porque cuenta caracteres, y letras como la "ñ" o las vocales con tilde ocupan 2 bytes:
 * 40 letras "ñ" son 40 caracteres, pero 80 bytes.
 */
@Documented
@Constraint(validatedBy = MaximoBytesUtf8.Validador.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaximoBytesUtf8 {

    int value();

    String message() default "El texto supera el tamaño máximo permitido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<MaximoBytesUtf8, String> {

        private int maximoBytes;

        @Override
        public void initialize(MaximoBytesUtf8 anotacion) {
            this.maximoBytes = anotacion.value();
        }

        /** Un valor nulo es válido aquí; la obligatoriedad la decide {@code @NotBlank}. */
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext contexto) {
            return valor == null || valor.getBytes(StandardCharsets.UTF_8).length <= maximoBytes;
        }
    }
}
