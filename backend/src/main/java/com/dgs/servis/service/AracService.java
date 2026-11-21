package com.dgs.servis.service;

import com.dgs.servis.dto.AracOlusturRequest;
import com.dgs.servis.dto.AracResponse;

public interface AracService {

    AracResponse olustur(AracOlusturRequest request);
    AracResponse idIleGetir(Long id);
}
