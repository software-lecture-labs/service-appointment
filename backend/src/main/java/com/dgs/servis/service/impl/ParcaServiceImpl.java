package com.dgs.servis.service.impl;

import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.entity.Parca;
import com.dgs.servis.exception.GecersizSiralamaAlaniException;
import com.dgs.servis.exception.KayitBulunamadiException;
import com.dgs.servis.exception.KayitZatenVarException;
import com.dgs.servis.mapper.ParcaMapper;
import com.dgs.servis.repository.ParcaRepository;
import com.dgs.servis.service.ParcaService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParcaServiceImpl implements ParcaService {

	private static final String KAYIT_TURU = "Parça";
	private static final Set<String> IZINLI_SIRALAMA_ALANLARI = Set.of("id", "kod", "ad", "birimFiyat", "stokAdedi");

	private final ParcaRepository parcaRepository;
	private final ParcaMapper parcaMapper;

	@Override
	public Page<ParcaYaniti> listele(Pageable pageable) {
		for (var siralama : pageable.getSort()) {
			if (!IZINLI_SIRALAMA_ALANLARI.contains(siralama.getProperty())) {
				throw new GecersizSiralamaAlaniException(
						"Sıralama alanı id, kod, ad, birimFiyat veya stokAdedi olmalıdır.");
			}
		}
		return parcaRepository.findAll(pageable).map(parcaMapper::toYanit);
	}

	@Override
	public ParcaYaniti getir(Long id) {
		return parcaMapper.toYanit(bul(id));
	}

	@Override
	@Transactional
	public ParcaYaniti olustur(ParcaOlusturIstegi istek) {
		String kod = Parca.kodNormallestir(istek.kod());
		if (parcaRepository.existsByKod(kod)) {
			throw new KayitZatenVarException("%s kodlu parça zaten kayıtlı".formatted(kod));
		}
		Parca kaydedilen = parcaRepository.save(parcaMapper.toEntity(istek));
		return parcaMapper.toYanit(kaydedilen);
	}

	@Override
	@Transactional
	public ParcaYaniti guncelle(Long id, ParcaGuncelleIstegi istek) {
		Parca parca = bul(id);
		parca.bilgileriGuncelle(istek.ad(), istek.birimFiyat());
		return parcaMapper.toYanit(parca);
	}

	@Override
	@Transactional
	public ParcaYaniti stokGirisi(Long id, int adet) {
		Parca parca = bul(id);
		parca.stokEkle(adet);
		return parcaMapper.toYanit(parca);
	}

	@Override
	@Transactional
	public void sil(Long id) {
		parcaRepository.delete(bul(id));
	}

	private Parca bul(Long id) {
		return parcaRepository.findById(id)
				.orElseThrow(() -> new KayitBulunamadiException(KAYIT_TURU, id));
	}

}
