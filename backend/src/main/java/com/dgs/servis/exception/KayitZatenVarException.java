package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

public class KayitZatenVarException extends UygulamaException {

	public KayitZatenVarException(String mesaj) {
		super(HttpStatus.CONFLICT, "KAYIT_ZATEN_VAR", mesaj);
	}

}
