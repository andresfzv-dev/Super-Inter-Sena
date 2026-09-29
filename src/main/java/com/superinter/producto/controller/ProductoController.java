package com.superinter.producto.controller;

import java.net.URI;
import java.util.List;

import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * API REST de gestión de productos: inserción, consulta, actualización y eliminación.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> registrar(@Valid @RequestBody ProductoRequest solicitud) {
        ProductoResponse registrado = productoService.registrar(solicitud);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{idProducto}")
                .buildAndExpand(registrado.idProducto())
                .toUri();

        return ResponseEntity.created(ubicacion).body(registrado);
    }

    @GetMapping
    public List<ProductoResponse> listarTodos() {
        return productoService.listarTodos();
    }

    @GetMapping("/{idProducto}")
    public ProductoResponse obtenerPorId(@PathVariable int idProducto) {
        return productoService.obtenerPorId(idProducto);
    }

    @PutMapping("/{idProducto}")
    public ProductoResponse actualizar(@PathVariable int idProducto,
                                       @Valid @RequestBody ProductoRequest solicitud) {
        return productoService.actualizar(idProducto, solicitud);
    }

    @DeleteMapping("/{idProducto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int idProducto) {
        productoService.eliminar(idProducto);
    }
}