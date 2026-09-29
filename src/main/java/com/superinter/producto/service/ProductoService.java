package com.superinter.producto.service;

import java.util.List;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.service.CategoriaService;
import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.producto.domain.Producto;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de la gestión de productos (CRUD) y sus reglas de negocio.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final String RECURSO = "Producto";

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public ProductoResponse registrar(ProductoRequest solicitud) {
        if (productoRepository.existePorCodigo(solicitud.codigo())) {
            throw codigoDuplicado(solicitud.codigo());
        }
        Categoria categoria = categoriaService.obtenerPorId(solicitud.idCategoria());

        Producto guardado = productoRepository.guardar(solicitud.aProducto(categoria));
        return ProductoResponse.desde(guardado);
    }

    public List<ProductoResponse> listarTodos() {
        return productoRepository.listarTodos().stream()
                .map(ProductoResponse::desde)
                .toList();
    }

    public ProductoResponse obtenerPorId(int idProducto) {
        return ProductoResponse.desde(buscarExistente(idProducto));
    }

    public ProductoResponse actualizar(int idProducto, ProductoRequest solicitud) {
        buscarExistente(idProducto);

        if (productoRepository.existePorCodigoEnOtroProducto(solicitud.codigo(), idProducto)) {
            throw codigoDuplicado(solicitud.codigo());
        }
        Categoria categoria = categoriaService.obtenerPorId(solicitud.idCategoria());

        Producto producto = solicitud.aProducto(categoria).toBuilder()
                .idProducto(idProducto)
                .build();

        if (!productoRepository.actualizar(producto)) {
            throw new RecursoNoEncontradoException(RECURSO, idProducto);
        }
        return ProductoResponse.desde(producto);
    }

    public void eliminar(int idProducto) {
        if (!productoRepository.eliminarPorId(idProducto)) {
            throw new RecursoNoEncontradoException(RECURSO, idProducto);
        }
    }

    private Producto buscarExistente(int idProducto) {
        return productoRepository.buscarPorId(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException(RECURSO, idProducto));
    }

    private ConflictoException codigoDuplicado(String codigo) {
        return new ConflictoException("Ya existe un producto con el código " + codigo);
    }
}