package com.dgs.servis.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgs.servis.dto.SayfaYaniti;
import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.entity.Parca;
import com.dgs.servis.exception.GecersizIstekException;
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
	private static final int EN_BUYUK_SAYFA_BOYUTU = 100;

	private final ParcaRepository parcaRepository;
	private final ParcaMapper parcaMapper;

	@Override
	public SayfaYaniti<ParcaYaniti> listele(int sayfa, int boyut) {
		if (sayfa < 0) {
			throw new GecersizIstekException("Sayfa numarası negatif olamaz");
		}
		if (boyut < 1 || boyut > EN_BUYUK_SAYFA_BOYUTU) {
			throw new GecersizIstekException("Sayfa boyutu 1 ile %d arasında olmalı".formatted(EN_BUYUK_SAYFA_BOYUTU));
		}
		Page<Parca> sonuc = parcaRepository.findAll(PageRequest.of(sayfa, boyut, Sort.by("kod")));
		return SayfaYaniti.of(sonuc, parcaMapper::toYanit);
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
