package com.bruno.gastos.repository;

import com.bruno.gastos.model.Gasto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
	List<Gasto> findByUsuarioIdAndFechaOperacionBetween(Long usuarioId, LocalDate desde, LocalDate hasta);
	List<Gasto> findByUsuarioIdAndMesContableId(Long usuarioId, Long mesId);
	boolean existsByCupon(String cupon);
}
