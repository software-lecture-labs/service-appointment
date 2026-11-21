package com.dgs.servis.dto;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AracOlusturRequest(
        @NotNull(message = "Müşteri ID'si zorunludur")
        @Positive(message = "Müşteri ID'si pozitif olmalıdır")
        Long musteriId,

        @NotBlank(message = "Plaka boş olamaz")
        @Size(max = 15, message = "Plaka en fazla 15 karakter olabilir")
        @Pattern(regexp = "[A-Z0-9]+", message = "Plaka yalnızca harf ve rakam içerebilir")
        String plaka,

        @NotBlank(message = "Marka boş olamaz")
        @Size(max = 100, message = "Marka en fazla 100 karakter olabilir")
        String marka,

        @NotBlank(message = "Model boş olamaz")
        @Size(max = 100, message = "Model en fazla 100 karakter olabilir")
        String model
) {
    public AracOlusturRequest {
        plaka = plaka == null ? null
                : plaka.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        marka = marka == null ? null : marka.strip();
        model = model == null ? null : model.strip();
    }
}