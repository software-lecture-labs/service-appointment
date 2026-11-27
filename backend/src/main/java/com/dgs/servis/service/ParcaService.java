package com.dgs.servis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;

public interface ParcaService {

	Page<ParcaYaniti> listele(Pageable pageable);

	ParcaYaniti getir(Long id);

	ParcaYaniti olustur(ParcaOlusturIstegi istek);

	ParcaYaniti guncelle(Long id, ParcaGuncelleIstegi istek);

	ParcaYaniti stokGirisi(Long id, int adet);

	void sil(Long id);

}
