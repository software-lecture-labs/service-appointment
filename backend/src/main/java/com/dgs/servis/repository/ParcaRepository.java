package com.dgs.servis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgs.servis.entity.Parca;

/** Listeleme için JpaRepository'nin hazır {@code findAll(Pageable)} metodu kullanılır (sayfalı). */
public interface ParcaRepository extends JpaRepository<Parca, Long> {

	boolean existsByKod(String kod);

}
