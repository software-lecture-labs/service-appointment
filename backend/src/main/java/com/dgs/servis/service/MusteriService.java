package com.dgs.servis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dgs.servis.dto.MusteriOlusturRequest;
import com.dgs.servis.dto.MusteriResponse;

public interface MusteriService {

    MusteriResponse olustur(MusteriOlusturRequest request);

    MusteriResponse idIleGetir(Long id);

    Page<MusteriResponse> tumunuListele(Pageable pageable);
}