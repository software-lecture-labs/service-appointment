package com.dgs.servis.dto.parca;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ParcaOlusturIstegi(
		@NotBlank(message = "Parça kodu zorunludur")
		@Size(max = 50, message = "Parça kodu en fazla 50 karakter olabilir")
		String kod,

		@NotBlank(message = "Parça adı zorunludur")
		@Size(max = 150, message = "Parça adı en fazla 150 karakter olabilir")
		String ad,

		@NotNull(message = "Stok adedi zorunludur")
		@PositiveOrZero(message = "Stok adedi negatif olamaz")
		@Max(value = ParcaSinirlari.ADET_UST_SINIRI, message = "Stok adedi en fazla 1.000.000 olabilir")
		Integer stokAdedi,

		@NotNull(message = "Birim fiyat zorunludur")
		@DecimalMin(value = "0.00", message = "Birim fiyat negatif olamaz")
		@Digits(integer = 10, fraction = 2, message = "Birim fiyat en fazla 2 ondalık basamak içerebilir")
		BigDecimal birimFiyat) {
}
