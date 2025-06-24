package com.bruno.gastos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bruno.gastos.model.Ingreso;
import com.bruno.gastos.repository.IngresoRepository;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {
	private final IngresoRepository ingresoRepo;

    public IngresoController(IngresoRepository ingresoRepo) {
        this.ingresoRepo = ingresoRepo;
    }

    @GetMapping("/usuario/{usuarioId}/mes/{mesId}")
    public List<Ingreso> obtenerIngresosPorUsuarioYMes(
            @PathVariable Long usuarioId,
            @PathVariable Long mesId) {
        return ingresoRepo.findByUsuarioIdAndMesContableId(usuarioId, mesId);
    }

    @PostMapping
    public Ingreso registrarIngreso(@RequestBody Ingreso ingreso) {
        return ingresoRepo.save(ingreso);
    }
    
    @GetMapping("/{id}")
    public Ingreso obtenerIngreso(@PathVariable Long id) {
        return ingresoRepo.findById(id).orElseThrow();
    }

    @PutMapping("/{id}")
    public Ingreso actualizarIngreso(@PathVariable Long id, @RequestBody Ingreso ingreso) {
        ingreso.setId(id);
        return ingresoRepo.save(ingreso);
    }

    @DeleteMapping("/{id}")
    public void eliminarIngreso(@PathVariable Long id) {
        ingresoRepo.deleteById(id);
    }
}
