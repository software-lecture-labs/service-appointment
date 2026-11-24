package com.dgs.servis.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.dgs.servis.TestcontainersConfiguration;
import com.dgs.servis.entity.Parca;

/**
 * Entity ile Flyway şemasının gerçek PostgreSQL üzerinde birlikte doğru çalıştığını doğrular: veritabanı
 * kuralları (V101), fiyat saklama ve {@code @Version} ile eşzamanlı güncelleme koruması.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ParcaRepositoryTest {

	private static final String HAM_EKLEME =
			"INSERT INTO parca (kod, ad, stok_adedi, birim_fiyat) VALUES (?, 'Parça', 1, 1)";

	@Autowired
	private ParcaRepository repository;

	@Autowired
	private TestEntityManager em;

	@Autowired
	private JdbcTemplate jdbc;

	@ParameterizedTest(name = "kod=''{0}''")
	@ValueSource(strings = { "p-1", "P-1 ", " P-1", "Flt-9", "", " ", "ß-1" })
	void veritabani_normalOlmayanKodlariEntityDisindanYazilsaBileReddeder(String kod) {
		assertThatThrownBy(() -> jdbc.update(HAM_EKLEME, kod))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("ck_parca_kod_normal");
	}

	@ParameterizedTest(name = "kod=''{0}''")
	@ValueSource(strings = { "P-1", "FLT-001", "SS-1", "A B" })
	void veritabani_normalBicimdekiKodlariKabulEder(String kod) {
		assertThatCode(() -> jdbc.update(HAM_EKLEME, kod)).doesNotThrowAnyException();
	}

	/**
	 * Java'nın {@code toUpperCase(Locale.ROOT)} çıktısı PostgreSQL'in {@code upper()} kuralıyla çelişmemeli:
	 * ikisi farklı davranabilir (örn. {@code ß} Java'da {@code SS}, PostgreSQL'de {@code ẞ} olur).
	 */
	@ParameterizedTest(name = "girilen=''{0}''")
	@ValueSource(strings = { "ß-1", "straße", "ıi-1", "i-ı", "ǆ-1", "p-001", "  flt-9  ", "ÇŞĞÜÖİ", "çşğüöi" })
	void entityNinNormallestirdigiHerKodVeritabaniKuralindanGecer(String girilen) {
		Parca parca = new Parca(girilen, "Parça", 1, BigDecimal.TEN);

		assertThatCode(() -> repository.saveAndFlush(parca)).doesNotThrowAnyException();
	}

	@Test
	void fiyatIkiOndalikliSaklanirVeAyniOkunur() {
		Parca kayitli = repository.saveAndFlush(new Parca("p-1", "Filtre", 3, new BigDecimal("10.5")));
		em.clear();

		Parca okunan = repository.findById(kayitli.getId()).orElseThrow();

		assertThat(okunan.getBirimFiyat()).isEqualByComparingTo("10.50");
		assertThat(okunan.getBirimFiyat().scale()).isEqualTo(2);
		assertThat(okunan.getKod()).isEqualTo("P-1");
	}

	@Test
	void ayniSurumuGuncelleyenIkinciIslemReddedilirVeStokTutarliKalir() {
		Parca kayitli = repository.saveAndFlush(new Parca("P-1", "Filtre", 10, BigDecimal.TEN));
		em.clear();

		Parca birinci = repository.findById(kayitli.getId()).orElseThrow();
		em.detach(birinci);
		Parca ikinci = repository.findById(kayitli.getId()).orElseThrow();
		em.detach(ikinci);

		birinci.stokDus(3);
		repository.saveAndFlush(birinci);
		ikinci.stokDus(5);
		assertThatThrownBy(() -> repository.saveAndFlush(ikinci))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
		em.clear();

		assertThat(repository.findById(kayitli.getId()).orElseThrow().getStokAdedi()).isEqualTo(7);
	}

}
