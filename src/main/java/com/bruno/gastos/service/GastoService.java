package com.bruno.gastos.service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bruno.gastos.model.Cuota;
import com.bruno.gastos.model.Gasto;
import com.bruno.gastos.model.MedioPago;
import com.bruno.gastos.model.MesContable;
import com.bruno.gastos.repository.CuotaRepository;
import com.bruno.gastos.repository.GastoRepository;
import com.bruno.gastos.repository.MesContableRepository;

@Service
public class GastoService {
	private final GastoRepository gastoRepo;
    private final CuotaRepository cuotaRepo;
    private final MesContableRepository mesContableRepo;

    public GastoService(GastoRepository gastoRepo, CuotaRepository cuotaRepo, MesContableRepository mesContableRepo) {
        this.gastoRepo = gastoRepo;
        this.cuotaRepo = cuotaRepo;
        this.mesContableRepo = mesContableRepo;
    }

    @Transactional
    public Gasto registrarGasto(Gasto gasto) {
        // Determinar a qué mes contable pertenece la fecha del gasto
    	LocalDate fechaContable = gasto.getMesInicioCuotas() != null
    		    ? gasto.getMesInicioCuotas()
    		    : gasto.getFechaOperacion();

    		MesContable mesContable = obtenerMesContableParaFecha(fechaContable);
        gasto.setMesContable(mesContable);

        // Guardar el gasto principal
        Gasto gastoGuardado = gastoRepo.save(gasto);

        // Si tiene cuotas, generar cuotas mensuales
        if (gasto.getCuotas() != null && gasto.getCuotas() > 0) {
            generarCuotas(gastoGuardado);
        }

        return gastoGuardado;
    }
    
    private void generarCuotas(Gasto gasto) {
        double montoPorCuota = Math.round((gasto.getMontoTotal() / gasto.getCuotas()) * 100.0) / 100.0;
        LocalDate fechaInicioCuotas = gasto.getMesInicioCuotas();
        LocalDate fechaCompra = gasto.getFechaOperacion();

        for (int i = 0; i < gasto.getCuotas(); i++) {
            LocalDate fechaCuota = fechaInicioCuotas.plusMonths(i);
            MesContable mesContable = obtenerMesContableParaFecha(fechaCuota);

            Cuota cuota = new Cuota();
            cuota.setGasto(gasto);
            cuota.setMontoCuota(montoPorCuota);
            cuota.setFechaCuota(fechaCompra); // Mostrar la fecha de compra en vez de la fecha de cobro
            cuota.setMesContable(mesContable);
            cuota.setNumeroCuota(i + 1);
            cuota.setTotalCuotas(gasto.getCuotas());

            cuotaRepo.save(cuota);
        }
    }

    
    public List<Gasto> obtenerGastosPorUsuarioYMes(Long usuarioId, Long mesId) {
        MesContable mes = mesContableRepo.findById(mesId).orElse(null);
        if (mes == null) return List.of();

        return gastoRepo.findByUsuarioIdAndMesContableId(usuarioId, mesId);
    }
    
    private MesContable obtenerMesContableParaFecha(LocalDate fecha) {
        return mesContableRepo.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(fecha, fecha)
            .orElseGet(() -> crearMesContableParaFecha(fecha));
    }
    
    private MesContable crearMesContableParaFecha(LocalDate fecha) {
        // Iniciar al primer día del mes
        LocalDate inicio = fecha.withDayOfMonth(1);
        LocalDate fin = inicio.plusMonths(1).minusDays(1);

        String nombre = "Mes " + inicio.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")) + " " + inicio.getYear();

        MesContable nuevoMes = new MesContable();
        nuevoMes.setFechaInicio(inicio);
        nuevoMes.setFechaFin(fin);
        nuevoMes.setNombre(nombre);
        nuevoMes.setCerrado(false);

        return mesContableRepo.save(nuevoMes);
    }
    
    @Transactional
    public Gasto actualizarGasto(Long id, Gasto nuevoGasto) {
        Gasto original = gastoRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Gasto no encontrado con ID: " + id));

        boolean cambioCuotas = 
            !Objects.equals(original.getCuotas(), nuevoGasto.getCuotas()) ||
            !Objects.equals(original.getMesInicioCuotas(), nuevoGasto.getMesInicioCuotas());

        // Actualizar datos principales
        original.setMontoTotal(nuevoGasto.getMontoTotal());
        original.setFechaOperacion(nuevoGasto.getFechaOperacion());
        original.setDescripcion(nuevoGasto.getDescripcion());
        original.setMedioPago(nuevoGasto.getMedioPago());
        original.setCategoria(nuevoGasto.getCategoria());
        original.setEsTercero(nuevoGasto.isEsTercero());
        original.setNombreTercero(nuevoGasto.getNombreTercero());
        original.setFueReembolsado(nuevoGasto.isFueReembolsado());
        original.setCuotas(nuevoGasto.getCuotas());
        original.setMesInicioCuotas(nuevoGasto.getMesInicioCuotas());

        // Asignar mes contable según la fecha de operación
        LocalDate fechaContable = nuevoGasto.getMesInicioCuotas() != null
        	    ? nuevoGasto.getMesInicioCuotas()
        	    : nuevoGasto.getFechaOperacion();

        	MesContable mes = obtenerMesContableParaFecha(fechaContable);
        original.setMesContable(mes);

        // Guardar el gasto actualizado primero
        Gasto gastoGuardado = gastoRepo.save(original);

        // Eliminar cuotas previas y generar nuevas si cambió el plan de cuotas
        if (cambioCuotas && gastoGuardado.getCuotas() != null && gastoGuardado.getCuotas() > 0) {
            cuotaRepo.deleteByGastoId(gastoGuardado.getId());
            generarCuotas(gastoGuardado);
        }

        return gastoGuardado;
    }




}
