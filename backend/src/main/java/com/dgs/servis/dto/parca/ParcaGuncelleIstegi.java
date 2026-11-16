package com.dgs.servis.dto.parca;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Parça kodu ve stok bu istekle değişmez: kod benzersiz anahtardır, stok yalnızca stok girişiyle artar. */
public record ParcaGuncelleIstegi(
		@NotBlank(message = "Parça adı zorunludur")
		@Size(max = 150, message = "Parça adı en fazla 150 karakter olabilir")
		String ad,

		@NotNull(message = "Birim fiyat zorunludur")
		@DecimalMin(value = "0.00", message = "Birim fiyat negatif olamaz")
		@Digits(integer = 10, fraction = 2, message = "Birim fiyat en fazla 2 ondalık basamak içerebilir")
		BigDecimal birimFiyat) {
}
