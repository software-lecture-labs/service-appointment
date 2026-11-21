package com.dgs.servis.controller;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.dgs.servis.TestcontainersConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class MusteriApiTest {

    private static final String MUSTERILER = "/api/musteriler";

    @Autowired
    private MockMvc mvc;

    @Test
    void musteriOlusturulurVeIdIleGetirilir() throws Exception {
        String ad = rastgeleAd();
        String konum = musteriOlustur(ad);

        mvc.perform(get(konum))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ad").value(ad))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void gecersizTelefonIcin400Doner() throws Exception {
        mvc.perform(post(MUSTERILER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(musteriJson(rastgeleAd(), "telefon")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.kod").value("DOGRULAMA_HATASI"))
                .andExpect(jsonPath("$.alanHatalari.telefon").exists());
    }

    @Test
    void bilinmeyenSiralamaAlaniIcin400Doner() throws Exception {
        mvc.perform(get(MUSTERILER).param("sort", "bilinmeyenAlan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.kod").value("GECERSIZ_SIRALAMA_ALANI"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void bulunamayanMusteriIcin404Doner() throws Exception {
        mvc.perform(get(MUSTERILER + "/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"));
    }

    @Test
    void sayfalarFarkliMusterileriDoner() throws Exception {
        String ilkAd = rastgeleAd();
        String ikinciAd = rastgeleAd();
        musteriOlustur(ilkAd);
        musteriOlustur(ikinciAd);

        mvc.perform(get(MUSTERILER)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].ad").value(ilkAd))
                .andExpect(jsonPath("$.page.number").value(0));

        mvc.perform(get(MUSTERILER)
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].ad").value(ikinciAd))
                .andExpect(jsonPath("$.page.number").value(1));
    }

    private String musteriOlustur(String ad) throws Exception {
        String telefon = "05" + ThreadLocalRandom.current()
                .nextInt(100_000_000, 1_000_000_000);

        return mvc.perform(post(MUSTERILER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(musteriJson("  " + ad + "  ", telefon)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }

    private static String musteriJson(String ad, String telefon) {
        return """
                {
                  "ad": "%s",
                  "soyad": "Yılmaz",
                  "telefon": "%s",
                  "email": "   "
                }
                """.formatted(ad, telefon);
    }

    private static String rastgeleAd() {
        return "Musteri-" + UUID.randomUUID();
    }
}