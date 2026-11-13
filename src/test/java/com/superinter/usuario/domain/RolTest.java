package com.superinter.usuario.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RolTest {

    @Test
    void desdeCodigoObtieneElRolGuardadoEnLaBaseDeDatos() {
        assertThat(Rol.desdeCodigo("AUXILIAR_INVENTARIOS")).isEqualTo(Rol.AUXILIAR_INVENTARIOS);
        assertThat(Rol.AUXILIAR_INVENTARIOS.getNombreVisible()).isEqualTo("Auxiliar de inventarios");
    }

    @Test
    void desdeCodigoRechazaUnCodigoDesconocido() {
        // El código distingue mayúsculas: en la base de datos siempre se guarda en mayúsculas.
        assertThatThrownBy(() -> Rol.desdeCodigo("cajero"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rol desconocido: cajero");
    }
}
