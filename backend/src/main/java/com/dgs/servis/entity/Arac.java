package com.dgs.servis.entity;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "araclar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Arac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String plaka;

    @Column(nullable = false, length = 100)
    private String marka;

    @Column(nullable = false, length = 100)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "musteri_id", nullable = false)
    private Musteri musteri;

    public Arac(String plaka, String marka, String model, Musteri musteri) {
        if (musteri == null) {
            throw new IllegalArgumentException("Araç bir müşteriye bağlı olmalıdır");
        }

        String duzenlenmisPlaka = plaka == null ? null
                : plaka.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);

        this.plaka = zorunluAlan(duzenlenmisPlaka, "Plaka", 15);
        this.marka = zorunluAlan(marka, "Marka", 100);
        this.model = zorunluAlan(model, "Model", 100);
        this.musteri = musteri;
    }

    private static String zorunluAlan(String deger, String alanAdi, int enFazlaUzunluk) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }

        String temizDeger = deger.strip();
        if (temizDeger.length() > enFazlaUzunluk) {
            throw new IllegalArgumentException(
                    alanAdi + " en fazla " + enFazlaUzunluk + " karakter olabilir");
        }
        return temizDeger;
    }
}