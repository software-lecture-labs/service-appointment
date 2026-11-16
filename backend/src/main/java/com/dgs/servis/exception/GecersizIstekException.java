package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

/** İstemcinin gönderdiği bir değer iş kurallarına uymuyorsa fırlatılır (örn. geçersiz sayfa boyutu). */
public class GecersizIstekException extends UygulamaException {

	public GecersizIstekException(String mesaj) {
		super(HttpStatus.BAD_REQUEST, "GECERSIZ_ISTEK", mesaj);
	}

}
