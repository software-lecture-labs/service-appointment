package com.dgs.servis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.dgs.servis.TestcontainersConfiguration;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.exception.KayitZatenVarException;
import com.dgs.servis.repository.ParcaRepository;
import com.dgs.servis.service.ParcaService;

/**
 * Parça servisini gerçek PostgreSQL ve Flyway şemasıyla dener. Amaç: iki istek aynı kodla aynı anda gelip
 * "kod kayıtlı mı?" kontrolünü birlikte geçse bile, veritabanındaki {@code uk_parca_kod} kısıtı yüzünden
 * yalnızca birinin kaydedilmesi.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ParcaServiceIntegrationTest {

	private static final int ESZAMANLI_ISTEK = 8;

	@Autowired
	private ParcaService servis;

	@Autowired
	private ParcaRepository parcaRepository;

	@Test
	void olustur_ayniKodlaEszamanliIstekGelirseYalnizcaBiriKaydedilir() throws Exception {
		String kod = "ES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		ParcaOlusturIstegi istek = new ParcaOlusturIstegi(kod, "Filtre", 5, new BigDecimal("100.00"));

		CountDownLatch baslangic = new CountDownLatch(1);
		ExecutorService havuz = Executors.newFixedThreadPool(ESZAMANLI_ISTEK);
		try {
			List<Future<Throwable>> sonuclar = new ArrayList<>();
			for (int i = 0; i < ESZAMANLI_ISTEK; i++) {
				sonuclar.add(havuz.submit(() -> {
					baslangic.await();
					try {
						servis.olustur(istek);
						return null;
					} catch (Throwable hata) {
						return hata;
					}
				}));
			}
			baslangic.countDown();

			int basarili = 0;
			for (Future<Throwable> sonuc : sonuclar) {
				Throwable hata = sonuc.get(30, TimeUnit.SECONDS);
				if (hata == null) {
					basarili++;
				} else {
					assertThat(hata).isInstanceOfAny(KayitZatenVarException.class,
							DataIntegrityViolationException.class);
				}
			}

			assertThat(basarili).isEqualTo(1);
			assertThat(parcaRepository.findAll()).filteredOn(p -> p.getKod().equals(kod)).hasSize(1);
		} finally {
			havuz.shutdownNow();
			parcaRepository.findAll().stream().filter(p -> p.getKod().equals(kod))
					.forEach(parcaRepository::delete);
		}
	}

}
