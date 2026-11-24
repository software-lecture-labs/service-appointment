package com.dgs.servis.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tüm controller'lardan fırlayan hataları {@link ApiError} formatına çevirir.
 *
 * <p>Log seviyesi "birinin bir şey yapması gerekiyor mu?" sorusuna göre seçilir:
 * <ul>
 * <li><b>ERROR</b> – beklenmeyen hata (500), hemen incelenmeli. Stack trace yalnızca burada basılır.</li>
 * <li><b>WARN</b> – veri bütünlüğü ihlali. Servis kontrollerinin kaçırdığı bir durum (eşzamanlı istek ya da eksik
 * doğrulama), sistem toparladı ama sebebi incelenmeli.</li>
 * <li><b>INFO</b> – iş kuralı gereği reddedilen istekler (409: yetersiz stok, kayıt zaten var, eşzamanlı
 * güncelleme). Önemli iş olayıdır, müdahale gerektirmez.</li>
 * <li><b>DEBUG</b> – istemcinin hatalı isteği (400 doğrulama veya bozuk istek, 404 kayıt/adres bulunamadı,
 * 405/415 desteklenmeyen yöntem/içerik türü). Normal akışın parçasıdır.</li>
 * </ul>
 * Beklenen hatalarda stack trace bilgi katmadığı halde log hacmini büyüttüğü için basılmaz.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UygulamaException.class)
	public ResponseEntity<ApiError> uygulamaHatasi(UygulamaException ex, HttpServletRequest istek) {
		if (ex.getDurum() == HttpStatus.CONFLICT) {
			log.info("İstek reddedildi [{}] {}: {}", ex.getKod(), istek.getRequestURI(), ex.getMessage());
		}
		else {
			log.debug("İstek reddedildi [{}] {}: {}", ex.getKod(), istek.getRequestURI(), ex.getMessage());
		}
		return yanit(ex.getDurum(), ex.getKod(), ex.getMessage(), istek);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> dogrulamaHatasi(MethodArgumentNotValidException ex, HttpServletRequest istek) {
		Map<String, String> alanHatalari = new LinkedHashMap<>();
		for (FieldError hata : ex.getBindingResult().getFieldErrors()) {
			alanHatalari.putIfAbsent(hata.getField(), hata.getDefaultMessage());
		}
		log.debug("Doğrulama hatası {}: {}", istek.getRequestURI(), alanHatalari.keySet());
		ApiError govde = ApiError.of(HttpStatus.BAD_REQUEST.value(), "DOGRULAMA_HATASI",
				"İstekteki bazı alanlar geçersiz", istek.getRequestURI(), alanHatalari);
		return ResponseEntity.badRequest().body(govde);
	}

	/**
	 * Bozuk JSON veya sayı beklenen yere harf yazılması gibi, isteğin kendisinden kaynaklanan hatalar.
	 * Bunlar sunucu hatası değildir; yakalanmazsa genel işleyiciye düşüp 500 ve ERROR log'u üretirdi.
	 */
	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiError> okunamayanIstek(Exception ex, HttpServletRequest istek) {
		log.debug("Okunamayan istek {}: {}", istek.getRequestURI(), ex.getClass().getSimpleName());
		String mesaj = ex instanceof MethodArgumentTypeMismatchException tip
				? "'%s' parametresinin değeri geçersiz".formatted(tip.getName())
				: "İstek gövdesi okunamadı, JSON biçimini ve alan türlerini kontrol edin";
		return yanit(HttpStatus.BAD_REQUEST, "GECERSIZ_ISTEK", mesaj, istek);
	}

	/**
	 * Spring'in kendi 4xx hataları (eksik parametre, desteklenmeyen yöntem veya içerik türü, olmayan adres).
	 * Hepsi {@link ErrorResponse} arayüzünü uygular ve kendi HTTP durumunu taşır; durumu koruyup yalnızca
	 * {@link ApiError} biçimine çevirilir.
	 */
	@ExceptionHandler({ ServletRequestBindingException.class, HandlerMethodValidationException.class,
			HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class,
			HttpMediaTypeNotAcceptableException.class, NoResourceFoundException.class })
	public ResponseEntity<ApiError> springIstekHatasi(Exception ex, HttpServletRequest istek) {
		HttpStatus durum = HttpStatus.valueOf(((ErrorResponse) ex).getStatusCode().value());
		log.debug("İstek reddedildi [{}] {}: {}", durum.value(), istek.getRequestURI(), ex.getClass().getSimpleName());
		return switch (durum) {
			case NOT_FOUND -> yanit(durum, "ADRES_BULUNAMADI", "Böyle bir adres yok", istek);
			case METHOD_NOT_ALLOWED -> yanit(durum, "YONTEM_DESTEKLENMIYOR",
					"Bu adres bu HTTP yöntemini desteklemiyor", istek);
			case UNSUPPORTED_MEDIA_TYPE, NOT_ACCEPTABLE -> yanit(durum, "ICERIK_TURU_DESTEKLENMIYOR",
					"İçerik türü desteklenmiyor, application/json kullanın", istek);
			default -> yanit(durum, "GECERSIZ_ISTEK", "İstek eksik veya hatalı", istek);
		};
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ApiError> eszamanliGuncelleme(ObjectOptimisticLockingFailureException ex,
			HttpServletRequest istek) {
		log.info("İstek reddedildi [ESZAMANLI_GUNCELLEME] {}: {}", istek.getRequestURI(),
				ex.getPersistentClassName());
		return yanit(HttpStatus.CONFLICT, "ESZAMANLI_GUNCELLEME",
				"Kayıt başka bir işlem tarafından değiştirildi, lütfen tekrar deneyin", istek);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> veriButunlugu(DataIntegrityViolationException ex, HttpServletRequest istek) {
		log.warn("Veri bütünlüğü ihlali {}: {}", istek.getRequestURI(), ex.getMostSpecificCause().getMessage());
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
