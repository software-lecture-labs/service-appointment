package com.dgs.servis.service;

import com.dgs.servis.dto.AracOlusturRequest;
import com.dgs.servis.dto.AracResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AracService {

    AracResponse olustur(AracOlusturRequest request);
    AracResponse idIleGetir(Long id);
    Page<AracResponse> tumunuListele(Pageable pageable);
}
