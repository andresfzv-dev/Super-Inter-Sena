package com.superinter.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración contra la base de datos real.
 * Requiere haber ejecutado database/data.sql (categorías iniciales).
 */
@SpringBootTest
class JdbcCategoriaRepositoryTest {

    private static final int ID_INEXISTENTE = 999_999;

    private final CategoriaRepository categoriaRepository;

    @Autowired
    JdbcCategoriaRepositoryTest(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Test
    void listarTodasDevuelveLasCategoriasOrdenadasPorNombre() {
        List<Categoria> categorias = categoriaRepository.listarTodas();

        assertThat(categorias)
                .extracting(Categoria::getNombre)
                .containsExactly("Abarrotes", "Aseo", "Bebidas", "Frutas y verduras", "Lácteos");
    }

    @Test
    void buscarPorIdDevuelveLaCategoriaCuandoExiste() {
        Categoria primera = categoriaRepository.listarTodas().getFirst();

        Optional<Categoria> encontrada = categoriaRepository.buscarPorId(primera.getIdCategoria());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre()).isEqualTo(primera.getNombre());
    }

    @Test
    void buscarPorIdDevuelveVacioCuandoNoExiste() {
        assertThat(categoriaRepository.buscarPorId(ID_INEXISTENTE)).isEmpty();
    }
}