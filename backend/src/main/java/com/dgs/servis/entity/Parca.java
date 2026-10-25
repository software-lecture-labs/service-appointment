package com.dgs.servis.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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

import com.dgs.servis.exception.YetersizStokException;

/**
 * Serviste kullanılan yedek parça ve stok bilgisi.
 * Stok yalnızca {@link #stokEkle(int)} ve {@link #stokDus(int)} üzerinden değiştirilir,
 * böylece stok hiçbir zaman eksiye düşemez.
 */
@Entity
@Table(name = "parca")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parca {

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

	public Parca(String kod, String ad, int stokAdedi, BigDecimal birimFiyat) {
		if (stokAdedi < 0) {
			throw new IllegalArgumentException("Stok adedi negatif olamaz");
		}
		this.kod = kod;
		this.ad = ad;
		this.stokAdedi = stokAdedi;
		this.birimFiyat = birimFiyat;
	}

	public void bilgileriGuncelle(String ad, BigDecimal birimFiyat) {
		this.ad = ad;
		this.birimFiyat = birimFiyat;
	}

	public void stokEkle(int adet) {
		if (adet <= 0) {
			throw new IllegalArgumentException("Eklenecek adet pozitif olmalı");
		}
		this.stokAdedi += adet;
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
