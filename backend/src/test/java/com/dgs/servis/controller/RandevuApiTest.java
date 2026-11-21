package com.dgs.servis.controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.dgs.servis.TestcontainersConfiguration;
import com.dgs.servis.entity.Arac;
import com.dgs.servis.entity.Musteri;
import com.dgs.servis.repository.AracRepository;
import com.dgs.servis.repository.MusteriRepository;
import com.jayway.jsonpath.JsonPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class RandevuApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MusteriRepository musteriRepository;

    @Autowired
    private AracRepository aracRepository;

    @Test
    void mevcutAracaRandevuOlusturulur() throws Exception {
        Long aracId = aracOlustur();
        OffsetDateTime yarin = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        randevuGonder(aracId, yarin)
                .andExpect(status().isCreated())
                .andExpect(result -> {
    Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    assertEquals("/api/randevular/" + id, result.getResponse().getHeader("Location"));
})
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.aracId").value(aracId))
                .andExpect(jsonPath("$.servisTalebi").value("Periyodik bakım"));
    }

    @Test
    void gecmisTariheRandevuAlinamaz() throws Exception {
        Long aracId = aracOlustur();
        OffsetDateTime dun = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);

        randevuGonder(aracId, dun)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.kod").value("DOGRULAMA_HATASI"))
                .andExpect(jsonPath("$.alanHatalari.randevuZamani").exists());
    }

    @Test
    void bulunamayanAracIcin404Doner() throws Exception {
        OffsetDateTime yarin = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        randevuGonder(Long.MAX_VALUE, yarin)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"));
    }

    @Test
void kaydedilenRandevuIdIleGetirilir() throws Exception {
    Long aracId = aracOlustur();
    OffsetDateTime yarin = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

    String yanit = randevuGonder(aracId, yarin)
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Number randevuId = JsonPath.read(yanit, "$.id");

    mvc.perform(get("/api/randevular/{id}", randevuId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(randevuId))
            .andExpect(jsonPath("$.aracId").value(aracId))
            .andExpect(jsonPath("$.servisTalebi").value("Periyodik bakım"));
}

@Test
void bulunamayanRandevuIcin404Doner() throws Exception {
    mvc.perform(get("/api/randevular/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"));
}

@Test
void randevularTarihSirasiylaSayfalanir() throws Exception {
    Long ilkAracId = aracOlustur();
    Long ikinciAracId = aracOlustur();
    OffsetDateTime ilkTarih = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    OffsetDateTime ikinciTarih = ilkTarih.plusDays(1);

    randevuGonder(ikinciAracId, ikinciTarih).andExpect(status().isCreated());
    randevuGonder(ilkAracId, ilkTarih).andExpect(status().isCreated());

    mvc.perform(get("/api/randevular")
                    .param("page", "0")
                    .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].aracId").value(ilkAracId))
            .andExpect(jsonPath("$.page.number").value(0));

    mvc.perform(get("/api/randevular")
                    .param("page", "1")
                    .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].aracId").value(ikinciAracId))
            .andExpect(jsonPath("$.page.number").value(1));
}

@Test
void gecersizSiralamaAlaniIcin400Doner() throws Exception {
    mvc.perform(get("/api/randevular").param("sort", "bilinmeyenAlan"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.kod").value("GECERSIZ_SIRALAMA_ALANI"));
}
    private Long aracOlustur() {
        String benzersizDeger = UUID.randomUUID().toString();
        Musteri musteri = musteriRepository.save(new Musteri(
                "Deniz", "Yılmaz", "05551234567", benzersizDeger + "@example.com"
        ));

        Arac arac = new Arac(
                "34AB" + benzersizDeger.substring(0, 8), "Volkswagen", "Golf", musteri
        );
        return aracRepository.save(arac).getId();
    }

    private ResultActions randevuGonder(Long aracId, OffsetDateTime zaman) throws Exception {
        String istek = """
                {
                  "aracId": %d,
                  "randevuZamani": "%s",
                  "servisTalebi": "  Periyodik bakım  "
                }
                """.formatted(aracId, zaman);

        return mvc.perform(post("/api/randevular")
                .contentType(MediaType.APPLICATION_JSON)
                .content(istek));
    }
}
