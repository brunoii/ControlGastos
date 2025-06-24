package com.bruno.gastos.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bruno.gastos.model.MesContable;

public interface MesContableRepository extends JpaRepository<MesContable, Long> {
	Optional<MesContable> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fechaInicio, LocalDate fechaFin);
}
