package com.bruno.gastos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bruno.gastos.model.Cuota;
import com.bruno.gastos.repository.CuotaRepository;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {
	private final CuotaRepository cuotaRepo;

    public CuotaController(CuotaRepository cuotaRepo) {
        this.cuotaRepo = cuotaRepo;
    }

    @GetMapping("/mes/{mesId}/usuario/{usuarioId}")
    public List<Cuota> obtenerCuotasPorMesYUsuario(
            @PathVariable Long mesId,
            @PathVariable Long usuarioId) {

        return cuotaRepo.findByMesContableIdAndGastoUsuarioId(mesId, usuarioId);
    }
}
