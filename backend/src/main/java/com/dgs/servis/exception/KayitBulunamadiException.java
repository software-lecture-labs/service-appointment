package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

public class KayitBulunamadiException extends UygulamaException {

	public KayitBulunamadiException(String kayitTuru, Object id) {
		super(HttpStatus.NOT_FOUND, "KAYIT_BULUNAMADI", "%s bulunamadı: %s".formatted(kayitTuru, id));
	}

}
