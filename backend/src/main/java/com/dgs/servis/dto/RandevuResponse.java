package com.dgs.servis.dto;

import java.time.OffsetDateTime;
import com.dgs.servis.entity.RandevuDurumu;

public record RandevuResponse(
        Long id,
        Long aracId,
        OffsetDateTime randevuZamani,
        String servisTalebi,
        RandevuDurumu durum) {
}
