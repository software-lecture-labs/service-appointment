package com.dgs.servis.service;

import com.dgs.servis.dto.RandevuOlusturRequest;
import com.dgs.servis.dto.RandevuResponse;

public interface RandevuService {

    RandevuResponse olustur(RandevuOlusturRequest request);
    RandevuResponse idIleGetir(Long id);
}