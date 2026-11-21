package com.dgs.servis.service;

import com.dgs.servis.dto.RandevuOlusturRequest;
import com.dgs.servis.dto.RandevuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RandevuService {

    RandevuResponse olustur(RandevuOlusturRequest request);
    RandevuResponse idIleGetir(Long id);
    Page<RandevuResponse> tumunuListele(Pageable pageable);
}