package com.dgs.servis.exception;

import org.springframework.http.HttpStatus;

public class GecersizSiralamaAlaniException extends UygulamaException {

    public GecersizSiralamaAlaniException() {
        super(HttpStatus.BAD_REQUEST, "GECERSIZ_SIRALAMA_ALANI",
                "Sıralama alanı id, ad, soyad, telefon veya email olmalıdır.");
    }
}