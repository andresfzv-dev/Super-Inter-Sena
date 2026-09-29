package com.superinter.categoria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.dto.CategoriaResponse;
import com.superinter.categoria.repository.CategoriaRepository;
import com.superinter.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;

class CategoriaServiceTest {

    private static final Categoria ABARROTES =
            Categoria.builder().idCategoria(1).nombre("Abarrotes").build();

    private final CategoriaService categoriaService =
            new CategoriaService(new CategoriaRepositoryFalso(List.of(ABARROTES)));

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

    /**
     * Implementación en memoria del repositorio para probar el servicio sin base de datos.
     */
    private record CategoriaRepositoryFalso(List<Categoria> categorias) implements CategoriaRepository {

        @Override
        public List<Categoria> listarTodas() {
            return categorias;
        }

        @Override
        public Optional<Categoria> buscarPorId(int idCategoria) {
            return categorias.stream()
                    .filter(categoria -> categoria.getIdCategoria() == idCategoria)
                    .findFirst();
        }
    }
}