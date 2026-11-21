package com.dgs.servis.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RandevuOlusturRequest(
        @NotNull(message = "Araç ID'si zorunludur")
        @Positive(message = "Araç ID'si pozitif olmalıdır")
        Long aracId,

        @NotNull(message = "Randevu zamanı zorunludur")
        @Future(message = "Randevu zamanı gelecekte olmalıdır")
        OffsetDateTime randevuZamani,

        @NotBlank(message = "Servis talebi boş olamaz")
        @Size(max = 500, message = "Servis talebi en fazla 500 karakter olabilir")
        String servisTalebi
) {
    public RandevuOlusturRequest {
        servisTalebi = servisTalebi == null ? null : servisTalebi.strip();
    }
}