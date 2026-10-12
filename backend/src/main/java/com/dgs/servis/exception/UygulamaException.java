package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

/**
 * Uygulamaya özel tüm hataların temel sınıfı. Her alt sınıf kendi HTTP durumunu ve hata kodunu taşır,
 * böylece {@link GlobalExceptionHandler} yeni hata türleri için değiştirilmek zorunda kalmaz.
 */
public abstract class UygulamaException extends RuntimeException {

	private final HttpStatus durum;
	private final String kod;

	protected UygulamaException(HttpStatus durum, String kod, String mesaj) {
		super(mesaj);
		this.durum = durum;
		this.kod = kod;
	}

	public HttpStatus getDurum() {
		return durum;
	}

	public String getKod() {
		return kod;
	}

}
