package com.dgs.servis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.entity.Parca;
import com.dgs.servis.exception.GecersizSiralamaAlaniException;
import com.dgs.servis.exception.KayitBulunamadiException;
import com.dgs.servis.exception.KayitZatenVarException;
import com.dgs.servis.exception.StokLimitiAsildiException;
import com.dgs.servis.mapper.ParcaMapper;
import com.dgs.servis.repository.ParcaRepository;

/**
 * Servis, Spring ve veritabanı olmadan test edilir: bağımlılıklar constructor ile verildiği için
 * repository sahte nesneyle değiştirilebilir.
 */
@ExtendWith(MockitoExtension.class)
class ParcaServiceImplTest {

	@Mock
	private ParcaRepository parcaRepository;

	private ParcaServiceImpl parcaService;

	@BeforeEach
	void kur() {
		parcaService = new ParcaServiceImpl(parcaRepository, new ParcaMapper());
	}

	private static Parca parca(String kod, int stok) {
		return new Parca(kod, "Yağ filtresi", stok, new BigDecimal("250.00"));
	}

	@ParameterizedTest(name = "girilen kod=''{0}''")
	@ValueSource(strings = { "FLT-001", "flt-001", " FLT-001 ", "Flt-001" })
	void olustur_kodHangiHarfBicimiyleYazilirsaYazilsinKayitliysaHataFirlatirVeKaydetmez(String girilen) {
		when(parcaRepository.existsByKod("FLT-001")).thenReturn(true);
		ParcaOlusturIstegi istek = new ParcaOlusturIstegi(girilen, "Yağ filtresi", 10, new BigDecimal("250.00"));

		assertThatThrownBy(() -> parcaService.olustur(istek))
				.isInstanceOf(KayitZatenVarException.class)
				.hasMessageContaining("FLT-001");
		verify(parcaRepository, never()).save(any());
	}

	@Test
	void olustur_kodVeAdKirpilipKodBuyukHarfeCevrilerekKaydedilir() {
		when(parcaRepository.existsByKod("FLT-001")).thenReturn(false);
		when(parcaRepository.save(any(Parca.class))).thenAnswer(cagri -> cagri.getArgument(0));
		ParcaOlusturIstegi istek = new ParcaOlusturIstegi(" flt-001 ", " Yağ filtresi ", 10, new BigDecimal("250.00"));

		ParcaYaniti yanit = parcaService.olustur(istek);

		assertThat(yanit.kod()).isEqualTo("FLT-001");
		assertThat(yanit.ad()).isEqualTo("Yağ filtresi");
		assertThat(yanit.stokAdedi()).isEqualTo(10);
	}

	@Test
	void getir_kayitYoksaKayitBulunamadiFirlatir() {
		when(parcaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> parcaService.getir(99L))
				.isInstanceOf(KayitBulunamadiException.class)
				.hasMessageContaining("99");
	}

	@Test
	void guncelle_adVeFiyatiDegistirirStokaDokunmaz() {
		Parca parca = parca("FLT-001", 7);
		when(parcaRepository.findById(1L)).thenReturn(Optional.of(parca));

		ParcaYaniti yanit = parcaService.guncelle(1L,
				new ParcaGuncelleIstegi("Orijinal yağ filtresi", new BigDecimal("300.00")));

		assertThat(yanit.ad()).isEqualTo("Orijinal yağ filtresi");
		assertThat(yanit.birimFiyat()).isEqualByComparingTo("300.00");
		assertThat(yanit.stokAdedi()).isEqualTo(7);
	}

	@Test
	void stokGirisi_stokuArtirir() {
		when(parcaRepository.findById(1L)).thenReturn(Optional.of(parca("FLT-001", 7)));

		ParcaYaniti yanit = parcaService.stokGirisi(1L, 5);

		assertThat(yanit.stokAdedi()).isEqualTo(12);
	}

	@Test
	void stokGirisi_ustSinirAsilirsaHataYayilirVeStokDegismez() {
		Parca parca = parca("FLT-001", Integer.MAX_VALUE - 2);
		when(parcaRepository.findById(1L)).thenReturn(Optional.of(parca));

		assertThatThrownBy(() -> parcaService.stokGirisi(1L, 3)).isInstanceOf(StokLimitiAsildiException.class);
		assertThat(parca.getStokAdedi()).isEqualTo(Integer.MAX_VALUE - 2);
	}

	@Test
	void listele_istenenSayfayiAyniPageableIleSorgularVeYanitaCevirir() {
		Pageable istenen = PageRequest.of(1, 2, Sort.by("kod"));
		when(parcaRepository.findAll(istenen))
				.thenReturn(new PageImpl<>(List.of(parca("C-3", 1)), istenen, 3));

		Page<ParcaYaniti> yanit = parcaService.listele(istenen);

		assertThat(yanit.getContent()).extracting(ParcaYaniti::kod).containsExactly("C-3");
		assertThat(yanit.getNumber()).isEqualTo(1);
		assertThat(yanit.getSize()).isEqualTo(2);
		assertThat(yanit.getTotalElements()).isEqualTo(3);
		assertThat(yanit.getTotalPages()).isEqualTo(2);
	}

	@ParameterizedTest(name = "siralama={0}")
	@ValueSource(strings = { "id", "kod", "ad", "birimFiyat", "stokAdedi" })
	void listele_izinliSiralamaAlanlariniKabulEder(String alan) {
		Pageable istenen = PageRequest.of(0, 20, Sort.by(alan));
		when(parcaRepository.findAll(istenen)).thenReturn(new PageImpl<>(List.of(), istenen, 0));

		assertThat(parcaService.listele(istenen).getContent()).isEmpty();
	}

	@ParameterizedTest(name = "siralama={0}")
	@ValueSource(strings = { "version", "KOD", "kod; drop table parca", "birim_fiyat" })
	void listele_izinsizSiralamaAlaniniReddederVeSorgulamaz(String alan) {
		Pageable istenen = PageRequest.of(0, 20, Sort.by(alan));

		assertThatThrownBy(() -> parcaService.listele(istenen)).isInstanceOf(GecersizSiralamaAlaniException.class);
		verify(parcaRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	void listele_izinliVeIzinsizAlanBirlikteIstenirseReddeder() {
		Pageable istenen = PageRequest.of(0, 20, Sort.by("kod", "version"));

		assertThatThrownBy(() -> parcaService.listele(istenen)).isInstanceOf(GecersizSiralamaAlaniException.class);
		verify(parcaRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	void sil_kayitVarsaSilerVeBaskaKayitaDokunmaz() {
		Parca parca = parca("A-1", 5);
		when(parcaRepository.findById(1L)).thenReturn(Optional.of(parca));

		parcaService.sil(1L);

		verify(parcaRepository).delete(parca);
	}

	@Test
	void sil_kayitYoksaKayitBulunamadiFirlatirVeHicSilmez() {
		when(parcaRepository.findById(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> parcaService.sil(9L)).isInstanceOf(KayitBulunamadiException.class);
		verify(parcaRepository, never()).delete(any(Parca.class));
	}

}
