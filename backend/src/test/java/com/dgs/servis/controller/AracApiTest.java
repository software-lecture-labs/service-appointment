package com.dgs.servis.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.dgs.servis.TestcontainersConfiguration;
import com.dgs.servis.entity.Musteri;
import com.dgs.servis.repository.MusteriRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AracApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MusteriRepository musteriRepository;

    @Test
void mevcutMusteriyeAracKaydedilirVeIdIleGetirilir() throws Exception {
    Long musteriId = musteriOlustur();
    int plakaNumarasi = rastgelePlakaNumarasi();
    String beklenenPlaka = "34AB" + plakaNumarasi;

    String yanit = aracGonder(musteriId, "34 ab " + plakaNumarasi)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.musteriId").value(musteriId))
            .andExpect(jsonPath("$.plaka").value(beklenenPlaka))
            .andReturn()
            .getResponse()
            .getContentAsString();

    Number aracId = JsonPath.read(yanit, "$.id");

    mvc.perform(get("/api/araclar/{id}", aracId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(aracId))
            .andExpect(jsonPath("$.musteriId").value(musteriId))
            .andExpect(jsonPath("$.plaka").value(beklenenPlaka));
}

    @Test
    void ayniPlakaFarkliBicimdeGonderilince409Doner() throws Exception {
        Long musteriId = musteriOlustur();
        int plakaNumarasi = rastgelePlakaNumarasi();

        aracGonder(musteriId, "34 ab " + plakaNumarasi)
                .andExpect(status().isCreated());

        aracGonder(musteriId, "34AB" + plakaNumarasi)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.kod").value("KAYIT_ZATEN_VAR"));
    }

    @Test
    void bulunamayanMusteriIcin404Doner() throws Exception {
        aracGonder(Long.MAX_VALUE, "34AB" + rastgelePlakaNumarasi())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"));
    }

    @Test
void bulunamayanAracIcin404Doner() throws Exception {
    mvc.perform(get("/api/araclar/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"));
}
    private Long musteriOlustur() {
        String eposta = "musteri-" + UUID.randomUUID() + "@example.com";
        return musteriRepository.save(
                new Musteri("Deniz", "Yılmaz", "05551234567", eposta)
        ).getId();
    }

    private ResultActions aracGonder(Long musteriId, String plaka) throws Exception {
        String istek = """
                {
                  "musteriId": %d,
                  "plaka": "%s",
                  "marka": "Volkswagen",
                  "model": "Golf"
                }
                """.formatted(musteriId, plaka);

        return mvc.perform(post("/api/araclar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(istek));
    }

    private static int rastgelePlakaNumarasi() {
        return ThreadLocalRandom.current().nextInt(1000, 10_000);
    }
}