package com.dgs.servis.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dgs.servis.TestcontainersConfiguration;

/**
 * İstemcinin hatalı isteklerinin (bozuk JSON, yanlış türde parametre, olmayan adres...) gerçek Spring MVC
 * zincirinde 500 yerine uygun 4xx ile ve {@link ApiError} biçiminde yanıtlandığını doğrular.
 * Gerçek controller'lardan bağımsız kalması için yalnızca testte tanımlı küçük bir controller kullanılır.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, IstemciHatalariIntegrationTest.OrnekController.class })
class IstemciHatalariIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@RestController
	static class OrnekController {

		record Girdi(@NotBlank String ad, int adet) {
		}

		@PostMapping("/test-hata/girdi")
		String girdi(@Valid @RequestBody Girdi girdi) {
			return girdi.ad();
		}

		@GetMapping("/test-hata/sayfa")
		String sayfa(@RequestParam int sayfa) {
			return "ok" + sayfa;
		}

		@GetMapping("/test-hata/{id}")
		String getir(@PathVariable Long id) {
			return "ok" + id;
		}

	}

	@ParameterizedTest(name = "govde=''{0}''")
	@ValueSource(strings = { "{", "{\"ad\": ", "bu json degil", "[]", "{\"ad\": \"x\", \"adet\": \"abc\"}", "" })
	void bozukGovde400DonerVe500Degil(String govde) throws Exception {
		mockMvc.perform(post("/test-hata/girdi").contentType(MediaType.APPLICATION_JSON).content(govde))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("GECERSIZ_ISTEK"))
				.andExpect(jsonPath("$.yol").value("/test-hata/girdi"));
	}

	@Test
	void alanDogrulamasiHalaAlanHatalariylaDoner() throws Exception {
		mockMvc.perform(post("/test-hata/girdi").contentType(MediaType.APPLICATION_JSON)
				.content("{\"ad\": \"\", \"adet\": 1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("DOGRULAMA_HATASI"))
				.andExpect(jsonPath("$.alanHatalari.ad").exists());
	}

	@ParameterizedTest(name = "id=''{0}''")
	@ValueSource(strings = { "abc", "1.5", "99999999999999999999" })
	void yanlisTurdekiYolDegiskeni400Doner(String id) throws Exception {
		mockMvc.perform(get("/test-hata/" + id))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("GECERSIZ_ISTEK"))
				.andExpect(jsonPath("$.mesaj").value("'id' parametresinin değeri geçersiz"));
	}

	@Test
	void yanlisTurdekiSorguParametresi400Doner() throws Exception {
		mockMvc.perform(get("/test-hata/sayfa").param("sayfa", "x"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("GECERSIZ_ISTEK"));
	}

	@Test
	void eksikZorunluParametre400Doner() throws Exception {
		mockMvc.perform(get("/test-hata/sayfa"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.kod").value("GECERSIZ_ISTEK"));
	}

	@Test
	void desteklenmeyenYontem405Doner() throws Exception {
		mockMvc.perform(post("/test-hata/sayfa"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.kod").value("YONTEM_DESTEKLENMIYOR"));
	}

	@Test
	void desteklenmeyenIcerikTuru415Doner() throws Exception {
		mockMvc.perform(post("/test-hata/girdi").contentType(MediaType.TEXT_PLAIN).content("ad"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.kod").value("ICERIK_TURU_DESTEKLENMIYOR"));
	}

	@ParameterizedTest(name = "yol=''{0}''")
	@ValueSource(strings = { "/olmayan-adres", "/api/olmayan/adres", "/test-hata/1/fazla" })
	void olmayanAdres404DonerVe500Degil(String yol) throws Exception {
		mockMvc.perform(get(yol))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.kod").value("ADRES_BULUNAMADI"))
				.andExpect(jsonPath("$.yol").value(yol));
	}

	@Test
	void gecerliIstekEtkilenmez() throws Exception {
		mockMvc.perform(get("/test-hata/7")).andExpect(status().isOk());
		mockMvc.perform(post("/test-hata/girdi").contentType(MediaType.APPLICATION_JSON)
				.content("{\"ad\": \"x\", \"adet\": 1}")).andExpect(status().isOk());
	}

}
