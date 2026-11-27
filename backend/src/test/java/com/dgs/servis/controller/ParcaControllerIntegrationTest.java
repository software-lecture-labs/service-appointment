package com.dgs.servis.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.dgs.servis.TestcontainersConfiguration;
import com.dgs.servis.dto.parca.ParcaSinirlari;
import com.dgs.servis.repository.ParcaRepository;

/**
 * Parça API'sini gerçek PostgreSQL (Testcontainers) ve Flyway şeması üzerinde uçtan uca test eder.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ParcaControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ParcaRepository parcaRepository;

	@BeforeEach
	void temizle() {
		parcaRepository.deleteAll();
	}

	private ResultActions parcaEkle(String kod, int stok) throws Exception {
		return mockMvc.perform(post("/api/parcalar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"kod": "%s", "ad": "Fren balatası", "stokAdedi": %d, "birimFiyat": 1250.50}
						""".formatted(kod, stok)));
	}

	@Test
	void parcaEklenirVeListelenir() throws Exception {
		parcaEkle("BLT-100", 4)
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.kod").value("BLT-100"))
				.andExpect(jsonPath("$.birimFiyat").value(1250.50));

		mockMvc.perform(get("/api/parcalar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].stokAdedi").value(4))
				.andExpect(jsonPath("$.page.totalElements").value(1));
	}

	@Test
	void ayniKodIkinciKezEklenirse409Doner() throws Exception {
		parcaEkle("BLT-100", 4).andExpect(status().isCreated());

		parcaEkle("BLT-100", 1)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.kod").value("KAYIT_ZATEN_VAR"));
	}

	@Test
	void gecersizIstek400VeAlanHatalariniDoner() throws Exception {
		mockMvc.perform(post("/api/parcalar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"kod": "", "ad": "Fren balatası", "stokAdedi": -2, "birimFiyat": 10}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("DOGRULAMA_HATASI"))
				.andExpect(jsonPath("$.alanHatalari.kod").exists())
				.andExpect(jsonPath("$.alanHatalari.stokAdedi").exists());
	}

	@Test
	void olmayanParca404Doner() throws Exception {
		mockMvc.perform(get("/api/parcalar/{id}", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.kod").value("KAYIT_BULUNAMADI"))
				.andExpect(jsonPath("$.yol").value("/api/parcalar/999999"));
	}

	@Test
	void stokGirisiVeGuncellemeYapilir() throws Exception {
		String id = JsonPath.read(
				parcaEkle("BLT-100", 4).andReturn().getResponse().getContentAsString(), "$.id").toString();

		mockMvc.perform(post("/api/parcalar/{id}/stok-girisi", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"adet\": 6}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stokAdedi").value(10));

		mockMvc.perform(put("/api/parcalar/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"ad\": \"Ön fren balatası\", \"birimFiyat\": 1400}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ad").value("Ön fren balatası"))
				.andExpect(jsonPath("$.stokAdedi").value(10));
	}

	@Test
	void parcaSilinir() throws Exception {
		String id = JsonPath.read(
				parcaEkle("BLT-100", 4).andReturn().getResponse().getContentAsString(), "$.id").toString();

		mockMvc.perform(delete("/api/parcalar/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/parcalar/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void ayniKodBuyukKucukHarfFarkliYazilsaBile409Doner() throws Exception {
		parcaEkle("blt-100", 4)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.kod").value("BLT-100"));

		parcaEkle("BLT-100", 1)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.kod").value("KAYIT_ZATEN_VAR"));
	}

	@ParameterizedTest(name = "adet={0}")
	@ValueSource(ints = { 0, -1, -50, Integer.MIN_VALUE })
	void stokGirisindeGecersizAdet400Doner(int adet) throws Exception {
		String id = parcaEkleVeIdAl("BLT-100", 4);

		mockMvc.perform(post("/api/parcalar/{id}/stok-girisi", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"adet\": %d}".formatted(adet)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("DOGRULAMA_HATASI"))
				.andExpect(jsonPath("$.alanHatalari.adet").exists());

		mockMvc.perform(get("/api/parcalar/{id}", id))
				.andExpect(jsonPath("$.stokAdedi").value(4));
	}

	@Test
	void stokGirisindeUstSinirAsilirsa400DonerVeAnlasilirMesajVerir() throws Exception {
		String id = parcaEkleVeIdAl("BLT-100", 4);

		mockMvc.perform(post("/api/parcalar/{id}/stok-girisi", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"adet\": %d}".formatted(ParcaSinirlari.ADET_UST_SINIRI + 1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.alanHatalari.adet").value(containsString("1.000.000")));
	}

	@Test
	void parcaOlusturmadaNegatifStokVeAsiriStok400Doner() throws Exception {
		mockMvc.perform(post("/api/parcalar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"kod": "BLT-1", "ad": "Balata", "stokAdedi": -1, "birimFiyat": 10}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.alanHatalari.stokAdedi").exists());

		mockMvc.perform(post("/api/parcalar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"kod": "BLT-1", "ad": "Balata", "stokAdedi": 2000000000, "birimFiyat": 10}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.alanHatalari.stokAdedi").value(containsString("1.000.000")));
	}

	@Test
	void listeSayfalanirVeKodaGoreSiralanir() throws Exception {
		parcaEkle("C-3", 1).andExpect(status().isCreated());
		parcaEkle("A-1", 1).andExpect(status().isCreated());
		parcaEkle("B-2", 1).andExpect(status().isCreated());

		mockMvc.perform(get("/api/parcalar").param("page", "0").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[0].kod").value("A-1"))
				.andExpect(jsonPath("$.content[1].kod").value("B-2"))
				.andExpect(jsonPath("$.page.totalElements").value(3))
				.andExpect(jsonPath("$.page.totalPages").value(2));

		mockMvc.perform(get("/api/parcalar").param("page", "1").param("size", "2"))
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].kod").value("C-3"));
	}

	@Test
	void sayfaBoyutuEnFazla100OlurVeAsanDegerKirpilir() throws Exception {
		parcaEkle("A-1", 1).andExpect(status().isCreated());

		mockMvc.perform(get("/api/parcalar").param("size", "5000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.size").value(100));
	}

	@Test
	void istenirseBaskaIzinliAlanaGoreSiralanir() throws Exception {
		parcaEkle("A-1", 5).andExpect(status().isCreated());
		parcaEkle("B-2", 1).andExpect(status().isCreated());

		mockMvc.perform(get("/api/parcalar").param("sort", "stokAdedi,asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].kod").value("B-2"));
	}

	@ParameterizedTest(name = "sort={0}")
	@ValueSource(strings = { "version", "KOD", "birim_fiyat", "x;drop table parca" })
	void izinsizSiralamaAlani400Doner(String sort) throws Exception {
		mockMvc.perform(get("/api/parcalar").param("sort", sort))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("GECERSIZ_SIRALAMA_ALANI"));
	}

	private String parcaEkleVeIdAl(String kod, int stok) throws Exception {
		return JsonPath.read(parcaEkle(kod, stok).andReturn().getResponse().getContentAsString(), "$.id")
				.toString();
	}

}
