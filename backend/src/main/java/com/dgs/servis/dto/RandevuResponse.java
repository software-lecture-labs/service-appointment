package com.dgs.servis.dto;

import java.time.OffsetDateTime;

public record RandevuResponse(
        Long id,
        Long aracId,
        OffsetDateTime randevuZamani,
        String servisTalebi
) {
}