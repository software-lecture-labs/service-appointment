package com.dgs.servis.repository;

import com.dgs.servis.entity.Arac;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AracRepository extends JpaRepository<Arac, Long> {

    boolean existsByPlaka(String plaka);
}