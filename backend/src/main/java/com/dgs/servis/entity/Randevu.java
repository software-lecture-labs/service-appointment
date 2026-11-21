package com.dgs.servis.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "randevular")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Randevu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arac_id", nullable = false)
    private Arac arac;

    @Column(name = "randevu_zamani", nullable = false)
    private OffsetDateTime randevuZamani;

    @Column(name = "servis_talebi", nullable = false, length = 500)
    private String servisTalebi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RandevuDurumu durum = RandevuDurumu.PLANLANDI;

    public Randevu(Arac arac, OffsetDateTime randevuZamani, String servisTalebi) {
        if (arac == null) {
            throw new IllegalArgumentException("Randevu bir araca bağlı olmalıdır");
        }
        if (randevuZamani == null) {
            throw new IllegalArgumentException("Randevu zamanı zorunludur");
        }
        if (servisTalebi == null || servisTalebi.isBlank()) {
            throw new IllegalArgumentException("Servis talebi boş olamaz");
        }

        String temizTalep = servisTalebi.strip();
        if (temizTalep.length() > 500) {
            throw new IllegalArgumentException("Servis talebi en fazla 500 karakter olabilir");
        }

        this.arac = arac;
        this.randevuZamani = randevuZamani;
        this.servisTalebi = temizTalep;
    }

    public void iptalEt() {
        this.durum = RandevuDurumu.IPTAL_EDILDI;
    }
}