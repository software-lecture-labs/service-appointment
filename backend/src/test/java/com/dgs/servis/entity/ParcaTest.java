package com.dgs.servis.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
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

	/** Sınır değer (-0,01) + rastgele üretilen negatif fiyatlar. */
	static Stream<BigDecimal> negatifFiyatlar() {
		Stream<BigDecimal> sinir = Stream.of(new BigDecimal("-0.01"));
		Stream<BigDecimal> rastgeleler = Stream.generate(
				() -> BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(-100_000, 0), 2))
				.limit(RASTGELE_DENEME_SAYISI);
		return Stream.concat(sinir, rastgeleler);
	}

	@ParameterizedTest(name = "fiyat={0}")
	@MethodSource("negatifFiyatlar")
	void olusturma_negatifFiyatiReddeder(BigDecimal fiyat) {
		assertThatThrownBy(() -> new Parca("P-1", "Filtre", 1, fiyat))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("negatif");
	}

	@ParameterizedTest(name = "fiyat={0}")
	@ValueSource(strings = { "0", "0.00", "0.01", "999999999.99" })
	void olusturma_sifirVeYukseklFiyatiKabulEder(String fiyat) {
		Parca parca = new Parca("P-1", "Filtre", 1, new BigDecimal(fiyat));

		assertThat(parca.getBirimFiyat()).isEqualByComparingTo(fiyat);
	}

	@Test
	void olusturma_bosDegerleriReddeder() {
		assertThatThrownBy(() -> new Parca(null, "Filtre", 1, BigDecimal.TEN))
				.isInstanceOf(NullPointerException.class).hasMessageContaining("Parça kodu");
		assertThatThrownBy(() -> new Parca("P-1", null, 1, BigDecimal.TEN))
				.isInstanceOf(NullPointerException.class).hasMessageContaining("Parça adı");
		assertThatThrownBy(() -> new Parca("P-1", "Filtre", 1, null))
				.isInstanceOf(NullPointerException.class).hasMessageContaining("Birim fiyat");
	}

	@ParameterizedTest(name = "deger=''{0}''")
	@ValueSource(strings = { "", " ", "   ", "\t", "\n" })
	void olusturma_bosVeyaSadeceBoslukMetniReddeder(String deger) {
		assertThatThrownBy(() -> new Parca(deger, "Filtre", 1, BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Parça kodu");
		assertThatThrownBy(() -> new Parca("P-1", deger, 1, BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Parça adı");
	}

	@ParameterizedTest(name = "girilen=''{0}'' kaydedilen=''{1}''")
	@CsvSource(quoteCharacter = '"', value = {
			"p-001, P-001",
			"\"  fLt-9  \", FLT-9",
			"P-001, P-001",
			"p-i01, P-I01",
			"ıp-1, IP-1" })
	void olusturma_kodunuKirpipBuyukHarfeCevirir(String girilen, String beklenen) {
		Parca parca = new Parca(girilen, "Filtre", 1, BigDecimal.TEN);

		assertThat(parca.getKod()).isEqualTo(beklenen);
	}

	/** Her çalışmada rastgele büyük/küçük harf karışımı kodlar üretir. */
	static Stream<String> rastgeleKarisikHarfliKodlar() {
		String harfler = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-";
		return Stream.generate(() -> {
			StringBuilder kod = new StringBuilder();
			for (int i = 0; i < rastgele(1, 40); i++) {
				kod.append(harfler.charAt(ThreadLocalRandom.current().nextInt(harfler.length())));
			}
			return kod.toString();
		}).limit(RASTGELE_DENEME_SAYISI);
	}

	@ParameterizedTest(name = "kod={0}")
	@MethodSource("rastgeleKarisikHarfliKodlar")
	void olusturma_herKodIcinAyniKaydiUretir(String kod) {
		Parca kucuk = new Parca(kod.toLowerCase(Locale.ROOT), "Filtre", 1, BigDecimal.TEN);
		Parca buyuk = new Parca(kod.toUpperCase(Locale.ROOT), "Filtre", 1, BigDecimal.TEN);

		assertThat(kucuk.getKod()).isEqualTo(buyuk.getKod()).isEqualTo(kod.toUpperCase(Locale.ROOT));
	}

	@Test
	void olusturma_buyukHarfeCevirmeUzunluguArtiriyorsaSaklananDegerUzerindenKontrolEder() {
		String kod = "ß".repeat(26);

		assertThat(kod).hasSize(26);
		assertThatThrownBy(() -> new Parca(kod, "Filtre", 1, BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("50");
	}

	@Test
	void olusturma_veritabaniUzunluklarinaUyar() {
		assertThat(new Parca("K".repeat(50), "A".repeat(150), 1, BigDecimal.TEN).getKod()).hasSize(50);

		assertThatThrownBy(() -> new Parca("K".repeat(51), "Filtre", 1, BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("50");
		assertThatThrownBy(() -> new Parca("P-1", "A".repeat(151), 1, BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("150");
	}

	@ParameterizedTest(name = "fiyat={0}")
	@MethodSource("negatifFiyatlar")
	void bilgileriGuncelle_negatifFiyatiReddederVeEskiDegerKalir(BigDecimal fiyat) {
		Parca parca = new Parca("P-1", "Filtre", 5, new BigDecimal("100.00"));

		assertThatThrownBy(() -> parca.bilgileriGuncelle("Yeni ad", fiyat))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(parca.getAd()).isEqualTo("Filtre");
		assertThat(parca.getBirimFiyat()).isEqualByComparingTo("100.00");
	}

	@Test
	void bilgileriGuncelle_gecerliDegerlerdeAdVeFiyatiDegistirirStokVeKodaDokunmaz() {
		Parca parca = new Parca("P-1", "Filtre", 5, new BigDecimal("100.00"));

		parca.bilgileriGuncelle("  Yağ filtresi  ", new BigDecimal("150.50"));

		assertThat(parca.getAd()).isEqualTo("Yağ filtresi");
		assertThat(parca.getBirimFiyat()).isEqualByComparingTo("150.50");
		assertThat(parca.getKod()).isEqualTo("P-1");
		assertThat(parca.getStokAdedi()).isEqualTo(5);
	}

	@Test
	void bilgileriGuncelle_bosVeyaNullAdiReddeder() {
		Parca parca = new Parca("P-1", "Filtre", 5, BigDecimal.TEN);

		assertThatThrownBy(() -> parca.bilgileriGuncelle("  ", BigDecimal.TEN))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> parca.bilgileriGuncelle(null, BigDecimal.TEN))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> parca.bilgileriGuncelle("Ad", null))
				.isInstanceOf(NullPointerException.class);
		assertThat(parca.getAd()).isEqualTo("Filtre");
	}

}
