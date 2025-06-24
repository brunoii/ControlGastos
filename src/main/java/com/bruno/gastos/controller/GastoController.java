package com.bruno.gastos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bruno.gastos.model.Gasto;
import com.bruno.gastos.repository.GastoRepository;
import com.bruno.gastos.service.GastoService;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {
	private final GastoRepository gastoRepo;
	private final GastoService gastoService;

    public GastoController(GastoService gastoService, GastoRepository gastoRepo) {
    	this.gastoRepo = gastoRepo;
        this.gastoService = gastoService;
    }

    @PostMapping
    public Gasto registrar(@RequestBody Gasto gasto) {
        return gastoService.registrarGasto(gasto);
    }
    
    @GetMapping("/usuario/{usuarioId}/mes/{mesId}")
    public List<Gasto> obtenerGastosPorUsuarioYMes(
            @PathVariable Long usuarioId,
            @PathVariable Long mesId) {

        return gastoService.obtenerGastosPorUsuarioYMes(usuarioId, mesId);
    }
    
    @GetMapping("/{id}")
    public Gasto obtenerPorId(@PathVariable Long id) {
        return gastoRepo.findById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void eliminarGasto(@PathVariable Long id) {
        gastoRepo.deleteById(id);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Gasto> actualizarGasto(@PathVariable Long id, @RequestBody Gasto nuevoGasto) {
        Gasto actualizado = gastoService.actualizarGasto(id, nuevoGasto);
        return ResponseEntity.ok(actualizado);
    }


}
