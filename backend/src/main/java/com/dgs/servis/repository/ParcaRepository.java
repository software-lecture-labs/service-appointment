package com.dgs.servis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgs.servis.entity.Parca;

public interface ParcaRepository extends JpaRepository<Parca, Long> {

	boolean existsByKod(String kod);

	List<Parca> findAllByOrderByKodAsc();

}
