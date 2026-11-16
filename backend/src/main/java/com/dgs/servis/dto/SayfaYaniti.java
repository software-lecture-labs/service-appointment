package com.dgs.servis.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Sayfalı liste yanıtı. Spring Data'nın {@code Page} nesnesi doğrudan JSON'a çevrilmez (iç yapısı
 * sürümler arasında değişebilir), bu yüzden API'nin sabit bir sözleşmesi olan bu kayıt kullanılır.
 */
public record SayfaYaniti<T>(
		List<T> icerik,
		int sayfa,
		int boyut,
		long toplamKayit,
		int toplamSayfa) {

	public static <S, T> SayfaYaniti<T> of(Page<S> sayfa, Function<S, T> donusum) {
		return new SayfaYaniti<>(
				sayfa.getContent().stream().map(donusum).toList(),
				sayfa.getNumber(),
				sayfa.getSize(),
				sayfa.getTotalElements(),
				sayfa.getTotalPages());
	}

}
