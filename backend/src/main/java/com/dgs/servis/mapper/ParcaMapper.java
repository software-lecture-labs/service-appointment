package com.dgs.servis.mapper;

import org.springframework.stereotype.Component;

import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.entity.Parca;

@Component
public class ParcaMapper {

	public Parca toEntity(ParcaOlusturIstegi istek) {
		return new Parca(istek.kod(), istek.ad(), istek.stokAdedi(), istek.birimFiyat());
	}

	public ParcaYaniti toYanit(Parca parca) {
		return new ParcaYaniti(parca.getId(), parca.getKod(), parca.getAd(), parca.getStokAdedi(),
				parca.getBirimFiyat());
	}

}
