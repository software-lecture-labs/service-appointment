package com.dgs.servis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MusteriOlusturRequest(
        @NotBlank(message = "Ad boş olamaz") @Size(max = 100) String ad,

        @NotBlank(message = "Soyad boş olamaz") @Size(max = 100) String soyad,

        @NotBlank(message = "Telefon boş olamaz") @Pattern(regexp = "[+]?[0-9]{10,15}", message = "Telefon 10-15 rakamdan oluşmalı ve + ile başlayabilir") @Size(max = 20) String telefon,

        @Email(message = "Geçerli bir e-posta adresi giriniz") @Size(max = 255) String email) {
    public MusteriOlusturRequest {
        ad = ad == null ? null : ad.strip();
        soyad = soyad == null ? null : soyad.strip();
        telefon = telefon == null ? null : telefon.strip();
        email = email == null || email.isBlank() ? null : email.strip();
    }
}

