package com.dgs.servis.entity;

import java.util.UUID;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusteriTest {

    @Test
    void gecerliMusteriDogrulamadanGecer() {
        Musteri musteri = gecerliMusteriOlustur();

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(musteri).isEmpty());
        }
    }

    @Test
    void sadeceBoslukIcerenAdReddedilir() {
        assertThrows(IllegalArgumentException.class,
                () -> new Musteri("   ", "Yılmaz", "05551234567", null));
    }

    @Test
    void gecersizEpostaReddedilir() {
        Musteri musteri = new Musteri("Deniz", "Yılmaz", "05551234567", "gecersiz-eposta");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var hatalar = factory.getValidator().validate(musteri);

            assertEquals(1, hatalar.size());
            assertEquals("email", hatalar.iterator().next().getPropertyPath().toString());
        }
    }

    private Musteri gecerliMusteriOlustur() {
        return new Musteri("Deniz", "Yılmaz", "05551234567",
                "musteri-" + UUID.randomUUID() + "@example.com");
    }
}