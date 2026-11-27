package com.dgs.servis.controller;

import java.net.URI;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgs.servis.dto.parca.ParcaGuncelleIstegi;
import com.dgs.servis.dto.parca.ParcaOlusturIstegi;
import com.dgs.servis.dto.parca.ParcaYaniti;
import com.dgs.servis.dto.parca.StokGirisIstegi;
import com.dgs.servis.service.ParcaService;

@RestController
@RequestMapping("/api/parcalar")
@RequiredArgsConstructor
@Tag(name = "Parçalar", description = "Yedek parça kataloğu ve stok yönetimi")
public class ParcaController {

	private final ParcaService parcaService;

	@GetMapping
	@Operation(summary = "Parçaları sayfalı listeler",
			description = "Varsayılan olarak koda göre sıralıdır. page 0'dan başlar, size en fazla 100'dür. "
					+ "sort yalnızca id, kod, ad, birimFiyat veya stokAdedi olabilir, aksi halde 400 döner.")
	public PagedModel<ParcaYaniti> listele(@PageableDefault(size = 20, sort = "kod") Pageable pageable) {
		return new PagedModel<>(parcaService.listele(pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Tek bir parçayı getirir")
	public ParcaYaniti getir(@PathVariable Long id) {
		return parcaService.getir(id);
	}

	@PostMapping
	@Operation(summary = "Yeni parça ekler", description = "Parça kodu benzersiz olmalıdır, aynı kod tekrar eklenirse 409 döner.")
	public ResponseEntity<ParcaYaniti> olustur(@Valid @RequestBody ParcaOlusturIstegi istek) {
		ParcaYaniti olusan = parcaService.olustur(istek);
		return ResponseEntity.created(URI.create("/api/parcalar/" + olusan.id())).body(olusan);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Parçanın adını ve birim fiyatını günceller")
	public ParcaYaniti guncelle(@PathVariable Long id, @Valid @RequestBody ParcaGuncelleIstegi istek) {
		return parcaService.guncelle(id, istek);
	}

	@PostMapping("/{id}/stok-girisi")
	@Operation(summary = "Parça stoğuna giriş yapar", description = "Depoya gelen parçalar için stok adedini artırır.")
	public ParcaYaniti stokGirisi(@PathVariable Long id, @Valid @RequestBody StokGirisIstegi istek) {
		return parcaService.stokGirisi(id, istek.adet());
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Parçayı siler")
	public ResponseEntity<Void> sil(@PathVariable Long id) {
		parcaService.sil(id);
		return ResponseEntity.noContent().build();
	}

}
