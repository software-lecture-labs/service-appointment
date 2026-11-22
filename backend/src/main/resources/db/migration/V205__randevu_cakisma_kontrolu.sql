CREATE UNIQUE INDEX uq_randevular_aktif_arac_zaman
    ON randevular (arac_id, randevu_zamani)
    WHERE durum = 'PLANLANDI';