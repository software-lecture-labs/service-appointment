package com.dgs.servis.dto.parca;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StokGirisIstegi(
		@NotNull(message = "Adet zorunludur")
		@Positive(message = "Adet pozitif olmalıdır")
		@Max(value = ParcaSinirlari.ADET_UST_SINIRI, message = "Tek seferde en fazla 1.000.000 adet girilebilir")
		Integer adet) {
}
