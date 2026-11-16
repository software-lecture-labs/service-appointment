package com.dgs.servis.service;

import com.dgs.servis.dto.SayfaYaniti;
import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;

public interface ParcaService {

	SayfaYaniti<ParcaYaniti> listele(int sayfa, int boyut);

	ParcaYaniti getir(Long id);

	ParcaYaniti olustur(ParcaOlusturIstegi istek);

	ParcaYaniti guncelle(Long id, ParcaGuncelleIstegi istek);

	ParcaYaniti stokGirisi(Long id, int adet);

	void sil(Long id);

}
