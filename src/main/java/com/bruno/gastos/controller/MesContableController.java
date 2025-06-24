package com.bruno.gastos.controller;


import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bruno.gastos.model.Cuota;
import com.bruno.gastos.model.Gasto;
import com.bruno.gastos.model.Ingreso;
import com.bruno.gastos.model.MesContable;
import com.bruno.gastos.model.ResumenMensual;
import com.bruno.gastos.model.Usuario;
import com.bruno.gastos.repository.CuotaRepository;
import com.bruno.gastos.repository.GastoRepository;
import com.bruno.gastos.repository.IngresoRepository;
import com.bruno.gastos.repository.MesContableRepository;
import com.bruno.gastos.repository.ResumenMensualRepository;
import com.bruno.gastos.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/meses")
public class MesContableController {
	private final MesContableRepository repo;
	private final ResumenMensualRepository resumenRepo;
	private final UsuarioRepository usuarioRepo;
	private final IngresoRepository ingresoRepo;
	private final GastoRepository gastoRepo;
	private final CuotaRepository cuotaRepo;

    public MesContableController(MesContableRepository repo, ResumenMensualRepository resumenRepo, UsuarioRepository usuarioRepo, IngresoRepository ingresoRepo, GastoRepository gastoRepo, CuotaRepository cuotaRepo) {
        this.repo = repo;
        this.resumenRepo = resumenRepo;
        this.usuarioRepo = usuarioRepo;
        this.ingresoRepo = ingresoRepo;
        this.gastoRepo = gastoRepo;
        this.cuotaRepo = cuotaRepo;
    }

    @GetMapping
    public List<MesContable> obtenerTodos() {
        return repo.findAll();
    }
    
    @PostMapping("/cerrar")
    public ResponseEntity<String> cerrarMes(@RequestParam Long mesId, @RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId).orElseThrow();
        MesContable mes = repo.findById(mesId).orElseThrow();

        double totalIngresos = ingresoRepo.findByUsuarioIdAndMesContableId(usuarioId, mesId)
            .stream().mapToDouble(Ingreso::getMonto).sum();

        double totalGastos = gastoRepo.findByUsuarioIdAndMesContableId(usuarioId, mesId)
            .stream().filter(g -> !"CREDITO".equals(g.getMedioPago()))
            .mapToDouble(Gasto::getMontoTotal).sum();

        double totalCuotas = cuotaRepo.findByMesContableIdAndGastoUsuarioId(mesId, usuarioId)
            .stream().mapToDouble(Cuota::getMontoCuota).sum();

        double saldoFinal = totalIngresos - totalGastos - totalCuotas;

        ResumenMensual resumen = new ResumenMensual();
        resumen.setMes(mes);
        resumen.setUsuario(usuario);
        resumen.setTotalIngresos(totalIngresos);
        resumen.setTotalGastos(totalGastos);
        resumen.setTotalCuotas(totalCuotas);
        resumen.setSaldoFinal(saldoFinal);
        resumen.setFechaCierre(LocalDate.now());

        resumenRepo.save(resumen);

        return ResponseEntity.ok("Mes cerrado exitosamente.");
    }
    
    @PostMapping("/cerrar-y-crear-nuevo")
    public ResponseEntity<?> cerrarYCrearNuevoMes(@RequestParam Long mesId, @RequestParam Long usuarioId) {
        // Paso 1: cerrar mes actual
        cerrarMes(mesId, usuarioId); // podés extraerlo como método privado

        // Paso 2: obtener mes actual
        MesContable mesActual = repo.findById(mesId).orElseThrow();

        // Paso 3: calcular siguiente mes (día siguiente al fin actual, +30 días aprox.)
        LocalDate nuevoInicio = mesActual.getFechaFin().plusDays(1);
        LocalDate nuevoFin = nuevoInicio.plusDays(29);

        // Paso 4: crear nuevo MesContable
        MesContable nuevoMes = new MesContable();
        nuevoMes.setFechaInicio(nuevoInicio);
        nuevoMes.setFechaFin(nuevoFin);
        nuevoMes.setNombre("Mes " + nuevoInicio.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")) + " " + nuevoInicio.getYear());
        nuevoMes.setCerrado(false);

        repo.save(nuevoMes);

        return ResponseEntity.ok("Mes cerrado y nuevo mes creado correctamente");
    }


}
