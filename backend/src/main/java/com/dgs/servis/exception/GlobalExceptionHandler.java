package com.dgs.servis.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tüm controller'lardan fırlayan hataları {@link ApiError} formatına çevirir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UygulamaException.class)
	public ResponseEntity<ApiError> uygulamaHatasi(UygulamaException ex, HttpServletRequest istek) {
		return yanit(ex.getDurum(), ex.getKod(), ex.getMessage(), istek);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> dogrulamaHatasi(MethodArgumentNotValidException ex, HttpServletRequest istek) {
		Map<String, String> alanHatalari = new LinkedHashMap<>();
		for (FieldError hata : ex.getBindingResult().getFieldErrors()) {
			alanHatalari.putIfAbsent(hata.getField(), hata.getDefaultMessage());
		}
		ApiError govde = ApiError.of(HttpStatus.BAD_REQUEST.value(), "DOGRULAMA_HATASI",
				"İstekteki bazı alanlar geçersiz", istek.getRequestURI(), alanHatalari);
		return ResponseEntity.badRequest().body(govde);
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ApiError> eszamanliGuncelleme(ObjectOptimisticLockingFailureException ex,
			HttpServletRequest istek) {
		return yanit(HttpStatus.CONFLICT, "ESZAMANLI_GUNCELLEME",
				"Kayıt başka bir işlem tarafından değiştirildi, lütfen tekrar deneyin", istek);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> veriButunlugu(DataIntegrityViolationException ex, HttpServletRequest istek) {
		log.warn("Veri bütünlüğü ihlali: {}", istek.getRequestURI(), ex);
		return yanit(HttpStatus.CONFLICT, "VERI_BUTUNLUGU_IHLALI",
				"İşlem mevcut kayıtlarla çakışıyor (benzersiz alan veya ilişkili kayıt)", istek);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> beklenmeyenHata(Exception ex, HttpServletRequest istek) {
		log.error("Beklenmeyen hata: {}", istek.getRequestURI(), ex);
		return yanit(HttpStatus.INTERNAL_SERVER_ERROR, "SUNUCU_HATASI", "Beklenmeyen bir hata oluştu", istek);
	}

	private static ResponseEntity<ApiError> yanit(HttpStatus durum, String kod, String mesaj,
			HttpServletRequest istek) {
		return ResponseEntity.status(durum).body(ApiError.of(durum.value(), kod, mesaj, istek.getRequestURI()));
	}

}
