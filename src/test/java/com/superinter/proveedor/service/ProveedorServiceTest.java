package com.superinter.proveedor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.repository.ProveedorRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProveedorServiceTest {

    private static final int ID_INEXISTENTE = 999;

    private ProveedorRepositoryEnMemoria repositorio;
    private ProveedorService proveedorService;

    @BeforeEach
    void configurar() {
        repositorio = new ProveedorRepositoryEnMemoria();
        proveedorService = new ProveedorService(repositorio);
    }

    @Test
    void registrarAsignaIdYConservaLosDatos() {
        ProveedorResponse registrado = proveedorService.registrar(solicitud("Postobón"));

        assertThat(registrado.idProveedor()).isNotNull();
        assertThat(registrado.correo()).isEqualTo("ventas@ejemplo.com");
    }

    @Test
    void registrarConNombreExistenteEnOtrasMayusculasLanzaConflicto() {
        proveedorService.registrar(solicitud("Postobón"));

        assertThatThrownBy(() -> proveedorService.registrar(solicitud("POSTOBÓN")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un proveedor con el nombre POSTOBÓN");
    }

    @Test
    void buscarFiltraPorNombreParcial() {
        proveedorService.registrar(solicitud("Alpina"));
        proveedorService.registrar(solicitud("Colanta"));

        assertThat(proveedorService.buscar("alp"))
                .extracting(ProveedorResponse::nombre)
                .containsExactly("Alpina");
    }

    @Test
    void obtenerPorIdInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> proveedorService.obtenerPorId(ID_INEXISTENTE))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Proveedor con id 999 no existe");
    }

    @Test
    void actualizarPermiteConservarElMismoNombre() {
        ProveedorResponse registrado = proveedorService.registrar(solicitud("Postobón"));
        ProveedorRequest cambios = new ProveedorRequest("Postobón", "Carlos Ruiz", null, null);

        ProveedorResponse actualizado = proveedorService.actualizar(registrado.idProveedor(), cambios);

        assertThat(actualizado.contacto()).isEqualTo("Carlos Ruiz");
        assertThat(proveedorService.obtenerPorId(registrado.idProveedor()).correo()).isNull();
    }

    @Test
    void actualizarConElNombreDeOtroProveedorLanzaConflicto() {
        proveedorService.registrar(solicitud("Postobón"));
        ProveedorResponse segundo = proveedorService.registrar(solicitud("Alpina"));

        assertThatThrownBy(() -> proveedorService.actualizar(segundo.idProveedor(), solicitud("postobón")))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizarProveedorInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> proveedorService.actualizar(ID_INEXISTENTE, solicitud("Postobón")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarBorraElProveedor() {
        ProveedorResponse registrado = proveedorService.registrar(solicitud("Postobón"));

        proveedorService.eliminar(registrado.idProveedor());

        assertThat(proveedorService.buscar(null)).isEmpty();
    }

    @Test
    void eliminarProveedorInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> proveedorService.eliminar(ID_INEXISTENTE))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarProveedorConComprasLanzaConflictoYNoLoBorra() {
        ProveedorResponse registrado = proveedorService.registrar(solicitud("Postobón"));
        repositorio.registrarCompraDe(registrado.idProveedor());

        assertThatThrownBy(() -> proveedorService.eliminar(registrado.idProveedor()))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("tiene compras registradas");
        assertThat(proveedorService.buscar(null)).hasSize(1);
    }

    private ProveedorRequest solicitud(String nombre) {
        return new ProveedorRequest(nombre, "Laura Gómez", "3001234567", "ventas@ejemplo.com");
    }
}