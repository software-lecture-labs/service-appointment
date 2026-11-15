package com.dgs.servis.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import com.dgs.servis.exception.StokLimitiAsildiException;
import com.dgs.servis.exception.YetersizStokException;

/**
 * Serviste kullanılan yedek parça ve stok bilgisi.
 * Stok yalnızca {@link #stokEkle(int)} ve {@link #stokDus(int)} üzerinden değiştirilir,
 * böylece stok hiçbir zaman eksiye düşemez, üst sınırda da taşıp eksiye dönmez.
 *
 * <p>Geçersiz adetler ({@link IllegalArgumentException}) istemcinin hatası değil, programlama hatası
 * sayılır: kullanıcıdan gelen değerler önce DTO doğrulamasından (örn. {@code @Positive}) geçer, bu kontroller
 * son savunma hattıdır. Buraya ulaşılırsa 500 ve ERROR log'u doğru sonuçtur.
 */
@Entity
@Table(name = "parca")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parca {

	/** Veritabanındaki kolon uzunluklarıyla aynı olmalı (V100__parca.sql). */
	private static final int KOD_UZUNLUGU = 50;
	private static final int AD_UZUNLUGU = 150;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String kod;

	@Column(nullable = false, length = 150)
	private String ad;

	@Column(name = "stok_adedi", nullable = false)
	private int stokAdedi;

	@Column(name = "birim_fiyat", nullable = false, precision = 12, scale = 2)
	private BigDecimal birimFiyat;

	/** Aynı parçayı eşzamanlı güncelleyen işlemleri yakalamak için (optimistic locking). */
	@Version
	private long version;

	@CreationTimestamp
	@Column(name = "olusturma_zamani", nullable = false, updatable = false)
	private OffsetDateTime olusturmaZamani;

	/**
	 * Parça kodu baştaki/sondaki boşluklardan arındırılıp büyük harfe çevrilerek saklanır, böylece
	 * {@code p-001} ile {@code P-001} aynı parçanın iki kaydı olarak tutulmaz.
	 */
	public Parca(String kod, String ad, int stokAdedi, BigDecimal birimFiyat) {
		if (stokAdedi < 0) {
			throw new IllegalArgumentException("Stok adedi negatif olamaz");
		}
		this.kod = gecerliKod(kod);
		this.ad = gecerliAd(ad);
		this.stokAdedi = stokAdedi;
		this.birimFiyat = gecerliFiyat(birimFiyat);
	}

	/** Önce iki değer de doğrulanır, sonra atanır: biri geçersizse hiçbir alan değişmez. */
	public void bilgileriGuncelle(String ad, BigDecimal birimFiyat) {
		String yeniAd = gecerliAd(ad);
		BigDecimal yeniFiyat = gecerliFiyat(birimFiyat);
		this.ad = yeniAd;
		this.birimFiyat = yeniFiyat;
	}

	/**
	 * Büyük harfe çevirme uzunluğu değiştirebilir (örn. {@code ß} → {@code SS}), bu yüzden uzunluk
	 * kontrolü çevirmeden sonra, saklanacak değer üzerinde yapılır.
	 */
	private static String gecerliKod(String kod) {
		Objects.requireNonNull(kod, "Parça kodu boş olamaz");
		return metniDogrula(kod.strip().toUpperCase(Locale.ROOT), "Parça kodu", KOD_UZUNLUGU);
	}

	private static String gecerliAd(String ad) {
		Objects.requireNonNull(ad, "Parça adı boş olamaz");
		return metniDogrula(ad.strip(), "Parça adı", AD_UZUNLUGU);
	}

	/** Boş veya veritabanı kolon uzunluğunu aşan metinleri reddeder. */
	private static String metniDogrula(String temiz, String alanAdi, int enUzun) {
		if (temiz.isEmpty()) {
			throw new IllegalArgumentException(alanAdi + " boş olamaz");
		}
		if (temiz.length() > enUzun) {
			throw new IllegalArgumentException("%s en fazla %d karakter olabilir".formatted(alanAdi, enUzun));
		}
		return temiz;
	}

	private static BigDecimal gecerliFiyat(BigDecimal fiyat) {
		Objects.requireNonNull(fiyat, "Birim fiyat boş olamaz");
		if (fiyat.signum() < 0) {
			throw new IllegalArgumentException("Birim fiyat negatif olamaz");
		}
		return fiyat;
	}

	/**
	 * Stoğa adet ekler. Toplam {@code int} sınırını aşacaksa sessizce eksiye dönmek yerine
	 * {@link StokLimitiAsildiException} fırlatır ve stok değişmez.
	 */
	public void stokEkle(int adet) {
		if (adet <= 0) {
			throw new IllegalArgumentException("Eklenecek adet pozitif olmalı");
		}
		try {
			this.stokAdedi = Math.addExact(this.stokAdedi, adet);
		}
		catch (ArithmeticException tasma) {
			throw new StokLimitiAsildiException(kod, stokAdedi, adet);
		}
	}

	public void stokDus(int adet) {
		if (adet <= 0) {
			throw new IllegalArgumentException("Düşülecek adet pozitif olmalı");
		}
		if (adet > stokAdedi) {
			throw new YetersizStokException(kod, stokAdedi, adet);
		}
		this.stokAdedi -= adet;
	}

}
