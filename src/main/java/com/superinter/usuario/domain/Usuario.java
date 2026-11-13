package com.superinter.usuario.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Usuario del sistema (entidad USUARIO del modelo lógico; RF-17, RNF-01).
 *
 * <p>Es inmutable: los cambios se hacen creando una nueva instancia con {@code toBuilder()}.
 * El correo es el nombre de usuario para iniciar sesión y siempre se guarda en minúsculas.
 *
 * <p>Solo guarda la contraseña cifrada con BCrypt (RNF-03); el texto plano nunca llega a
 * esta clase. No se genera {@code toString()} para que el hash no termine en un log por
 * accidente.
 */
@Getter
@Builder(toBuilder = true)
public class Usuario {

    private final Integer idUsuario;
    private final String nombre;
    private final String correo;
    private final String contrasenaCifrada;
    private final Rol rol;
}
