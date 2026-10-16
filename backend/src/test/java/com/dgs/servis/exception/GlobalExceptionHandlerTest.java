package com.dgs.servis.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Her hata türünün belgelenen log seviyesinde loglandığını ve stack trace'in yalnızca beklenmeyen hatalarda
 * basıldığını doğrular. Handler doğrudan oluşturulur, Spring context veya sunucu gerekmez.
 */
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

	private static final String STACK_TRACE_SATIRI = "\tat ";

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
	private final MockHttpServletRequest istek = new MockHttpServletRequest("GET", "/api/parcalar/1");
	private final Logger handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private Level oncekiSeviye;

	@BeforeEach
	void debugLoglariAc() {
		oncekiSeviye = handlerLogger.getLevel();
		handlerLogger.setLevel(Level.DEBUG);
	}

	@AfterEach
	void seviyeyiGeriAl() {
		handlerLogger.setLevel(oncekiSeviye);
	}

	@Test
	void isKuraliReddi409_infoOlarakStackTraceOlmadanLoglanir(CapturedOutput cikti) {
		ResponseEntity<ApiError> yanit = handler.uygulamaHatasi(new YetersizStokException("FLT-001", 2, 5), istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(logSatiri(cikti, "YETERSIZ_STOK")).contains("INFO");
		assertThat(cikti.getOut()).doesNotContain(STACK_TRACE_SATIRI);
	}

	@Test
	void kayitBulunamadi404_debugOlarakLoglanir(CapturedOutput cikti) {
		ResponseEntity<ApiError> yanit = handler.uygulamaHatasi(new KayitBulunamadiException("Parça", 1), istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(logSatiri(cikti, "KAYIT_BULUNAMADI")).contains("DEBUG");
		assertThat(cikti.getOut()).doesNotContain(STACK_TRACE_SATIRI);
	}

	@Test
	void dogrulamaHatasi400_debugOlarakAlanAdlariylaLoglanir(CapturedOutput cikti) throws Exception {
		BeanPropertyBindingResult sonuc = new BeanPropertyBindingResult(new Object(), "istek");
		sonuc.addError(new FieldError("istek", "kod", "Parça kodu zorunludur"));
		MethodParameter parametre = new MethodParameter(
				GlobalExceptionHandlerTest.class.getDeclaredMethod("ornekMetot", String.class), 0);

		ResponseEntity<ApiError> yanit = handler.dogrulamaHatasi(
				new MethodArgumentNotValidException(parametre, sonuc), istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(logSatiri(cikti, "Doğrulama hatası")).contains("DEBUG").contains("kod");
	}

	@Test
	void eszamanliGuncelleme_infoOlarakLoglanir(CapturedOutput cikti) {
		ResponseEntity<ApiError> yanit = handler.eszamanliGuncelleme(
				new ObjectOptimisticLockingFailureException("Parca", 1L), istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(logSatiri(cikti, "ESZAMANLI_GUNCELLEME")).contains("INFO");
		assertThat(cikti.getOut()).doesNotContain(STACK_TRACE_SATIRI);
	}

	@Test
	void veriButunluguIhlali_warnOlarakKokNedenleStackTraceOlmadanLoglanir(CapturedOutput cikti) {
		DataIntegrityViolationException ex = new DataIntegrityViolationException("kayıt eklenemedi",
				new SQLException("duplicate key value violates unique constraint \"uk_parca_kod\""));

		ResponseEntity<ApiError> yanit = handler.veriButunlugu(ex, istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(logSatiri(cikti, "Veri bütünlüğü ihlali")).contains("WARN").contains("uk_parca_kod");
		assertThat(cikti.getOut()).doesNotContain(STACK_TRACE_SATIRI);
	}

	@Test
	void beklenmeyenHata500_errorOlarakStackTraceIleLoglanir(CapturedOutput cikti) {
		ResponseEntity<ApiError> yanit = handler.beklenmeyenHata(new IllegalStateException("beklenmedik"), istek);

		assertThat(yanit.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(logSatiri(cikti, "Beklenmeyen hata")).contains("ERROR");
		assertThat(cikti.getOut()).contains(STACK_TRACE_SATIRI);
	}

	private static String logSatiri(CapturedOutput cikti, String icerik) {
		return cikti.getOut().lines()
				.filter(satir -> satir.contains(icerik))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Log satırı bulunamadı: " + icerik + "\n" + cikti.getOut()));
	}

	@SuppressWarnings("unused")
	private void ornekMetot(String deger) {
		// Yalnızca MethodArgumentNotValidException için MethodParameter oluşturmakta kullanılır.
	}

}
