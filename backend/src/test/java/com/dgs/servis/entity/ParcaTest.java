package com.dgs.servis.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.dgs.servis.exception.StokLimitiAsildiException;
import com.dgs.servis.exception.YetersizStokException;

/**
 * Parça stok kuralları belirli örnek değerlere değil, her geçerli değere uyması gereken genel kurallardır.
 * Bu yüzden testler sınır değerlerle birlikte her çalışmada yeniden üretilen rastgele değerlerle çalışır.
 * Başarısız olan bir durumun değerleri test adında görünür, böylece hata tekrar üretilebilir.
 */
class ParcaTest {

	private static final int RASTGELE_DENEME_SAYISI = 20;
	private static final int UST_SINIR = 10_000;

	private static Parca rastgeleParca(int stok) {
		String kod = "P-" + UUID.randomUUID().toString().substring(0, 8);
		BigDecimal fiyat = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(0, 100_000), 2);
		return new Parca(kod, "Parça " + kod, stok, fiyat);
	}

	private static int rastgele(int enAz, int enCok) {
		return ThreadLocalRandom.current().nextInt(enAz, enCok + 1);
	}

	/** Sınır değerler + istenen adedin her zaman stoktan fazla olduğu rastgele çiftler. */
	static Stream<Arguments> stokuAsanIstekler() {
		Stream<Arguments> sinirlar = Stream.of(
				Arguments.of(0, 1),
				Arguments.of(1, 2),
				Arguments.of(UST_SINIR, UST_SINIR + 1));
		Stream<Arguments> rastgeleler = Stream.generate(() -> {
			int stok = rastgele(0, UST_SINIR);
			return Arguments.of(stok, stok + rastgele(1, UST_SINIR));
		}).limit(RASTGELE_DENEME_SAYISI);
		return Stream.concat(sinirlar, rastgeleler);
	}

	/** Sınır değerler + istenen adedin stoğu aşmadığı rastgele çiftler. */
	static Stream<Arguments> stokuAsmayanIstekler() {
		Stream<Arguments> sinirlar = Stream.of(
				Arguments.of(1, 1),
				Arguments.of(UST_SINIR, UST_SINIR),
				Arguments.of(UST_SINIR, 1));
		Stream<Arguments> rastgeleler = Stream.generate(() -> {
			int stok = rastgele(1, UST_SINIR);
			return Arguments.of(stok, rastgele(1, stok));
		}).limit(RASTGELE_DENEME_SAYISI);
		return Stream.concat(sinirlar, rastgeleler);
	}

	/** Herhangi bir başlangıç stoğu ve herhangi bir pozitif ekleme adedi. */
	static Stream<Arguments> stokVeEklenecekAdet() {
		return Stream.generate(() -> Arguments.of(rastgele(0, UST_SINIR), rastgele(1, UST_SINIR)))
				.limit(RASTGELE_DENEME_SAYISI);
	}

	@ParameterizedTest(name = "stok={0}, istenen={1}")
	@MethodSource("stokuAsanIstekler")
	void stokDus_stokYetersizseHataFirlatirVeStokDegismez(int stok, int istenen) {
		Parca parca = rastgeleParca(stok);

		assertThatThrownBy(() -> parca.stokDus(istenen))
				.isInstanceOf(YetersizStokException.class)
				.hasMessageContaining(parca.getKod())
				.hasMessageContaining("mevcut: " + stok)
				.hasMessageContaining("istenen: " + istenen);
		assertThat(parca.getStokAdedi()).isEqualTo(stok);
	}

	@ParameterizedTest(name = "stok={0}, istenen={1}")
	@MethodSource("stokuAsmayanIstekler")
	void stokDus_yeterliStokVarsaIstenenKadarAzaltir(int stok, int istenen) {
		Parca parca = rastgeleParca(stok);

		parca.stokDus(istenen);

		assertThat(parca.getStokAdedi()).isEqualTo(stok - istenen).isNotNegative();
	}

	@ParameterizedTest(name = "stok={0}, eklenen={1}")
	@MethodSource("stokVeEklenecekAdet")
	void stokEkle_stoguEklenenKadarArtirir(int stok, int eklenen) {
		Parca parca = rastgeleParca(stok);

		parca.stokEkle(eklenen);

		assertThat(parca.getStokAdedi()).isEqualTo(stok + eklenen);
	}

	@ParameterizedTest(name = "stok={0}, eklenen={1}")
	@CsvSource({
			"2147483647, 1",
			"2147483600, 100",
			"2147483643, 5",
			"1, 2147483647" })
	void stokEkle_ustSinirAsilirsaHataFirlatirVeStokDegismez(int stok, int eklenen) {
		Parca parca = rastgeleParca(stok);

		assertThatThrownBy(() -> parca.stokEkle(eklenen))
				.isInstanceOf(StokLimitiAsildiException.class)
				.hasMessageContaining(parca.getKod());
		assertThat(parca.getStokAdedi()).isEqualTo(stok);
	}

	@ParameterizedTest(name = "stok={0}, eklenen={1}")
	@CsvSource({
			"0, 2147483647",
			"2147483646, 1",
			"2147483600, 47" })
	void stokEkle_tamUstSinirdaTasmadanKabulEder(int stok, int eklenen) {
		Parca parca = rastgeleParca(stok);

		parca.stokEkle(eklenen);

		assertThat(parca.getStokAdedi()).isEqualTo(Integer.MAX_VALUE);
	}

	@ParameterizedTest(name = "adet={0}")
	@ValueSource(ints = { 0, -1, -50, Integer.MIN_VALUE })
	void stokEkleVeDus_pozitifOlmayanAdetiReddederVeStokDegismez(int adet) {
		int stok = rastgele(0, UST_SINIR);
		Parca parca = rastgeleParca(stok);

		assertThatThrownBy(() -> parca.stokEkle(adet)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> parca.stokDus(adet)).isInstanceOf(IllegalArgumentException.class);
		assertThat(parca.getStokAdedi()).isEqualTo(stok);
	}

	@ParameterizedTest(name = "stok={0}")
	@ValueSource(ints = { -1, -50, Integer.MIN_VALUE })
	void olusturma_negatifStokuReddeder(int stok) {
		assertThatThrownBy(() -> rastgeleParca(stok)).isInstanceOf(IllegalArgumentException.class);
	}

}
