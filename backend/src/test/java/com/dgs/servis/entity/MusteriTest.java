package com.dgs.servis.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

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
        Musteri musteri = gecerliMusteriOlustur();
        musteri.setAd("   ");

        dogrulamaHatasiniKontrolEt(musteri, "ad");
    }

    @Test
    void gecersizEpostaReddedilir() {
        Musteri musteri = gecerliMusteriOlustur();
        musteri.setEmail("gecersiz-eposta");

        dogrulamaHatasiniKontrolEt(musteri, "email");
    }

    private Musteri gecerliMusteriOlustur() {
        Musteri musteri = new Musteri();
        musteri.setAd("Deniz");
        musteri.setSoyad("Yılmaz");
        musteri.setTelefon("05551234567");
        musteri.setEmail("musteri-" + UUID.randomUUID() + "@example.com");
        return musteri;
    }

    private void dogrulamaHatasiniKontrolEt(Musteri musteri, String beklenenAlan) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var hatalar = factory.getValidator().validate(musteri);

            assertEquals(1, hatalar.size());
            assertEquals(beklenenAlan, hatalar.iterator().next().getPropertyPath().toString());
        }
    }
}