package com.dgs.servis.dto.parca;

import java.math.BigDecimal;

public record ParcaYaniti(
		Long id,
		String kod,
		String ad,
		int stokAdedi,
		BigDecimal birimFiyat) {
}
