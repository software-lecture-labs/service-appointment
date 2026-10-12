package com.dgs.servis.exception;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tüm hata yanıtlarının ortak JSON formatı. Frontend hataları bu yapıya göre işler.
 *
 * @param zaman       hatanın oluştuğu an
 * @param durum       HTTP durum kodu
 * @param kod         makinece okunabilir hata kodu (örn. YETERSIZ_STOK)
 * @param mesaj       kullanıcıya gösterilebilecek açıklama
 * @param yol         isteğin yapıldığı adres
 * @param alanHatalari doğrulama hatalarında alan adı → mesaj eşlemesi
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
		OffsetDateTime zaman,
		int durum,
		String kod,
		String mesaj,
		String yol,
		Map<String, String> alanHatalari) {

	public static ApiError of(int durum, String kod, String mesaj, String yol) {
		return new ApiError(OffsetDateTime.now(), durum, kod, mesaj, yol, Map.of());
	}

	public static ApiError of(int durum, String kod, String mesaj, String yol, Map<String, String> alanHatalari) {
		return new ApiError(OffsetDateTime.now(), durum, kod, mesaj, yol, alanHatalari);
	}

}
