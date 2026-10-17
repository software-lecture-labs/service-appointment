
package com.dgs.servis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MusteriOlusturRequest(

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 100)
    String ad,

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 100)
    String soyad,

    @NotBlank(message = "Telefon boş olamaz")
    @Size(max = 20)
    String telefon,

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255)
    String email

) {}
