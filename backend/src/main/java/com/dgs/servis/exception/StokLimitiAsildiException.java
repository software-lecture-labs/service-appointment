package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

/** Stok eklemesi, stoğun tutulabileceği en büyük değeri aşacaksa fırlatılır. */
public class StokLimitiAsildiException extends UygulamaException {

	public StokLimitiAsildiException(String parcaKodu, int mevcut, int eklenecek) {
		super(HttpStatus.CONFLICT, "STOK_LIMITI_ASILDI",
				"%s kodlu parçaya %d adet eklenemez, stok üst sınırı aşılır (mevcut: %d, üst sınır: %d)"
						.formatted(parcaKodu, eklenecek, mevcut, Integer.MAX_VALUE));
	}

}
