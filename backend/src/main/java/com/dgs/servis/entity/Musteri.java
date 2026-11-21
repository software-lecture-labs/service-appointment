
package com.dgs.servis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "musteriler")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Musteri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String soyad;

    @NotBlank(message = "Telefon boş olamaz")
    @Pattern(regexp = "[+]?[0-9]{10,15}", message = "Telefon 10-15 rakamdan oluşmalı ve + ile başlayabilir")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String telefon;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255)
    @Column(length = 255)

    private String email;
    @CreationTimestamp
    @Column(name = "olusturma_zamani", nullable = false, updatable = false)
    private OffsetDateTime olusturmaZamani;

    public Musteri(String ad, String soyad, String telefon, String email) {
        this.ad = zorunluAlan(ad, "Ad");
        this.soyad = zorunluAlan(soyad, "Soyad");
        this.telefon = zorunluAlan(telefon, "Telefon");
        this.email = email == null || email.isBlank() ? null : email.strip();
    }

    private static String zorunluAlan(String deger, String alanAdi) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }
        return deger.strip();
    }
}

