package com.bruno.gastos.repository;

import com.bruno.gastos.model.Cuota;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {
	List<Cuota> findByMesContableIdAndGastoUsuarioId(Long mesId, Long usuarioId);
	void deleteByGastoId(Long gastoId);
}
