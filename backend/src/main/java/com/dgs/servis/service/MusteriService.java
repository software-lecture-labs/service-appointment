
package com.dgs.servis.service;

import java.util.List;

import com.dgs.servis.dto.MusteriOlusturRequest;
import com.dgs.servis.dto.MusteriResponse;

public interface MusteriService {

    MusteriResponse olustur(MusteriOlusturRequest request);

    MusteriResponse idIleGetir(Long id);

    List<MusteriResponse> tumunuListele();

}

