package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

public class YetersizStokException extends UygulamaException {

	public YetersizStokException(String parcaKodu, int mevcut, int istenen) {
		super(HttpStatus.CONFLICT, "YETERSIZ_STOK",
				"%s kodlu parça için stok yetersiz (mevcut: %d, istenen: %d)".formatted(parcaKodu, mevcut, istenen));
	}

}
