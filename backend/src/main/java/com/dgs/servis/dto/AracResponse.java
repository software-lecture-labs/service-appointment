package com.dgs.servis.dto;

public record AracResponse(
        Long id,
        Long musteriId,
        String plaka,
        String marka,
        String model
) {}
