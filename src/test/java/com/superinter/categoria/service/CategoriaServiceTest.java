package com.superinter.categoria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.dto.CategoriaResponse;
import com.superinter.categoria.repository.CategoriaRepositoryEnMemoria;
import com.superinter.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;

class CategoriaServiceTest {

    private final CategoriaService categoriaService =
            new CategoriaService(new CategoriaRepositoryEnMemoria(List.of(ABARROTES)));

    private static final Categoria ABARROTES =
            Categoria.builder().idCategoria(1).nombre("Abarrotes").build();


    @Test
    void listarTodasConvierteLasCategoriasEnRespuestas() {
        List<CategoriaResponse> respuestas = categoriaService.listarTodas();

        assertThat(respuestas).containsExactly(new CategoriaResponse(1, "Abarrotes"));
    }

    @Test
    void obtenerPorIdDevuelveLaCategoriaCuandoExiste() {
        Categoria categoria = categoriaService.obtenerPorId(1);

        assertThat(categoria.getNombre()).isEqualTo("Abarrotes");
    }

    @Test
    void obtenerPorIdLanzaRecursoNoEncontradoCuandoNoExiste() {
        assertThatThrownBy(() -> categoriaService.obtenerPorId(99))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Categoría con id 99 no existe");
    }


}