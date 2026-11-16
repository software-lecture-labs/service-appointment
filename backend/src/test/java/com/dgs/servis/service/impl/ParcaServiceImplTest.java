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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.dgs.servis.dto.SayfaYaniti;
import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.entity.Parca;
import com.dgs.servis.exception.GecersizIstekException;
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
	void listele_koduSiralayarakIstenenSayfayiGetirirVeYanitaCevirir() {
		Pageable istenen = PageRequest.of(1, 2, Sort.by("kod"));
		when(parcaRepository.findAll(istenen))
				.thenReturn(new PageImpl<>(List.of(parca("C-3", 1)), istenen, 3));

		SayfaYaniti<ParcaYaniti> yanit = parcaService.listele(1, 2);

		assertThat(yanit.icerik()).extracting(ParcaYaniti::kod).containsExactly("C-3");
		assertThat(yanit.sayfa()).isEqualTo(1);
		assertThat(yanit.boyut()).isEqualTo(2);
		assertThat(yanit.toplamKayit()).isEqualTo(3);
		assertThat(yanit.toplamSayfa()).isEqualTo(2);
		ArgumentCaptor<Pageable> yakalanan = ArgumentCaptor.forClass(Pageable.class);
		verify(parcaRepository).findAll(yakalanan.capture());
		assertThat(yakalanan.getValue().getSort().getOrderFor("kod")).isNotNull();
	}

	@ParameterizedTest(name = "sayfa={0}, boyut={1}")
	@CsvSource({ "-1, 20", "-100, 20", "0, 0", "0, -1", "0, 101", "0, 2147483647" })
	void listele_gecersizSayfaVeBoyutuReddeder(int sayfa, int boyut) {
		assertThatThrownBy(() -> parcaService.listele(sayfa, boyut)).isInstanceOf(GecersizIstekException.class);
		verify(parcaRepository, never()).findAll(any(Pageable.class));
	}

	@ParameterizedTest(name = "boyut={0}")
	@ValueSource(ints = { 1, 20, 100 })
	void listele_sinirdakiGecerliBoyutlariKabulEder(int boyut) {
		Pageable istenen = PageRequest.of(0, boyut, Sort.by("kod"));
		when(parcaRepository.findAll(istenen)).thenReturn(new PageImpl<>(List.of(), istenen, 0));

		SayfaYaniti<ParcaYaniti> yanit = parcaService.listele(0, boyut);

		assertThat(yanit.icerik()).isEmpty();
		assertThat(yanit.boyut()).isEqualTo(boyut);
	}

}
